package com.training.assetmanagement.media.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.training.assetmanagement.media.domain.*;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageProcessorImpl implements ImageProcessor {

    private static final Logger log = LoggerFactory.getLogger(ImageProcessorImpl.class);

    // Chỉ lấy khung đầu (WebP động, PNG nhiều khung)
    private static final int FIRST_IMAGE = 0;
    // Thu mỗi bước tối đa 1/2 - giảm răng cưa khi thu mạnh
    private static final int DOWNSCALE_STEP = 2;
    // Nền làm phẳng cho PNG / WebP trong suốt
    private static final Color BACKGROUND = Color.WHITE;

    private final ObjectStorage objectStorage;

    @PostConstruct
    void registerPlugins() {
        // Fat jar: Plugin WebP chỉ được thấy sau khi quét lại classpath
        ImageIO.scanForPlugins();
        // Giải mã trong bộ nhớ, không ghi tệp tạm
        ImageIO.setUseCache(false);
    }

    @Override
    public StoredImage process(MultipartFile file) {
        byte[] content = readContent(file);
        ImageFormat format = ImageFormat.detect(content)
            .orElseThrow(() -> new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE, "Unknown file signature"));

        // Xoay theo EXIF TRƯỚC khi bỏ metadata
        BufferedImage normalized = normalize(decode(content, format), readOrientation(content));
        BufferedImage original = fit(normalized, ImageVariant.ORIGINAL.getMaxEdge());
        BufferedImage thumbnail = fit(original, ImageVariant.THUMBNAIL.getMaxEdge());

        // Mã hóa xong cả hai bản rồi mới tải - lỗi mã hóa không để lại đối tượng nào
        Map<ImageVariant, byte[]> encoded = new EnumMap<>(ImageVariant.class);
        encoded.put(ImageVariant.ORIGINAL, encodeJpeg(original));
        encoded.put(ImageVariant.THUMBNAIL, encodeJpeg(thumbnail));

        // Khóa UUID mới mỗi lần tải, không bao giờ dùng lại
        String objectKey = UUID.randomUUID().toString();
        upload(objectKey, encoded);
        log.debug("Image stored format={} size={}x{}", format, original.getWidth(), original.getHeight());
        return new StoredImage(objectKey);
    }

    @Override
    public void discard(StoredImage image) {
        for (ImageVariant variant : ImageVariant.values()) {
            objectStorage.removeQuietly(variant.keyOf(image.objectKey()));
        }
    }

    private byte[] readContent(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE, "Empty upload");
        }
        if (file.getSize() > ImageLimits.MAX_FILE_BYTES) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE, "Upload size " + file.getSize());
        }
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Cannot read upload: " + ex.getMessage());
        }
    }

    // Đọc kích thước TRƯỚC khi giải mã toàn bộ - chặn ảnh quá nhiều điểm ảnh
    private BufferedImage decode(byte[] content, ImageFormat format) {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(format.getReaderName());
        if (!readers.hasNext()) {
            log.error("No ImageIO reader registered for format={}", format);
            throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE, "No reader for " + format);
        }
        ImageReader reader = readers.next();
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            reader.setInput(input, true, true);
            long pixels = (long) reader.getWidth(FIRST_IMAGE) * reader.getHeight(FIRST_IMAGE);
            if (pixels > ImageLimits.MAX_PIXELS) {
                throw new BusinessException(ErrorCode.IMAGE_DIMENSIONS_TOO_LARGE, "Pixel count " + pixels);
            }
            return reader.read(FIRST_IMAGE);
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            // Chữ ký đúng nhưng tệp hỏng
            throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE, "Cannot decode " + format + ": " + ex.getMessage());
        } finally {
            reader.dispose();
        }
    }

    // EXIF hỏng / thiếu -> không xoay, không chặn tải ảnh
    private ExifOrientation readOrientation(byte[] content) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(content));
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory == null || !directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                return ExifOrientation.NORMAL;
            }
            return ExifOrientation.fromTag(directory.getInt(ExifIFD0Directory.TAG_ORIENTATION));
        } catch (ImageProcessingException | IOException | MetadataException | RuntimeException ex) {
            log.debug("EXIF orientation unreadable, using NORMAL: {}", ex.getMessage());
            return ExifOrientation.NORMAL;
        }
    }

    // Xoay theo EXIF, làm phẳng lên nền trắng -> RGB không kênh alpha
    private BufferedImage normalize(BufferedImage source, ExifOrientation orientation) {
        int width = source.getWidth();
        int height = source.getHeight();
        int targetWidth = orientation.swapsDimensions() ? height : width;
        int targetHeight = orientation.swapsDimensions() ? width : height;
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setColor(BACKGROUND);
            graphics.fillRect(0, 0, targetWidth, targetHeight);
            graphics.drawImage(source, orientation.transform(width, height), null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    // Thu cạnh dài về maxEdge, không phóng to
    private BufferedImage fit(BufferedImage source, int maxEdge) {
        int longEdge = Math.max(source.getWidth(), source.getHeight());
        if (longEdge <= maxEdge) {
            return source;
        }
        double ratio = (double) maxEdge / longEdge;
        int targetWidth = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int targetHeight = Math.max(1, (int) Math.round(source.getHeight() * ratio));
        BufferedImage current = source;
        int width = source.getWidth();
        int height = source.getHeight();
        while (width > targetWidth || height > targetHeight) {
            width = Math.max(targetWidth, width / DOWNSCALE_STEP);
            height = Math.max(targetHeight, height / DOWNSCALE_STEP);
            current = scale(current, width, height);
        }
        return current;
    }

    private BufferedImage scale(BufferedImage source, int width, int height) {
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    // Không truyền metadata -> bỏ toàn bộ EXIF (GPS, thiết bị)
    private byte[] encodeJpeg(BufferedImage image) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(ImageFormat.JPEG.getReaderName());
        if (!writers.hasNext()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "No JPEG writer registered");
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(ImageLimits.JPEG_QUALITY);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(stream);
            writer.write(null, new IIOImage(image, null, null), param);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "JPEG encode failed: " + ex.getMessage());
        } finally {
            writer.dispose();
        }
        return output.toByteArray();
    }

    // Một bản tải lỗi -> gỡ những bản đã tải rồi ném lại
    private void upload(String objectKey, Map<ImageVariant, byte[]> encoded) {
        List<String> uploaded = new ArrayList<>();
        try {
            for (Map.Entry<ImageVariant, byte[]> entry : encoded.entrySet()) {
                String key = entry.getKey().keyOf(objectKey);
                byte[] bytes = entry.getValue();
                objectStorage.put(key, new ByteArrayInputStream(bytes), bytes.length, ImageMimeType.JPEG.getValue());
                uploaded.add(key);
            }
        } catch (RuntimeException ex) {
            uploaded.forEach(objectStorage::removeQuietly);
            throw ex;
        }
    }
}
