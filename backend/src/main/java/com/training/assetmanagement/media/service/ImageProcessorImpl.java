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
import org.springframework.http.MediaType;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageProcessorImpl implements ImageProcessor {

    private final ObjectStorage objectStorage;

    // Fat jar Spring Boot: Quét lại plugin bằng class loader của ứng dụng để thấy reader WebP
    @PostConstruct
    void registerImageReaders() {
        ImageIO.scanForPlugins();
        // Giải mã trong bộ nhớ, không ghi tệp tạm
        ImageIO.setUseCache(false);
    }

    @Override
    public StoredImage process(MultipartFile file) {
        byte[] content = readContent(file);
        ImageFormat format = ImageFormat.detect(content)
            .orElseThrow(() -> new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE));
        // Xoay theo EXIF rồi mới bỏ metadata - bỏ trước thì ảnh chụp điện thoại nằm ngang
        BufferedImage normalized = normalize(decode(content, format), readOrientation(content));

        // Khóa UUID mới mỗi lần tải, không bao giờ dùng lại
        String objectKey = UUID.randomUUID().toString();
        List<String> uploaded = new ArrayList<>();
        try {
            for (ImageVariant variant : ImageVariant.values()) {
                String key = variant.keyOf(objectKey);
                byte[] jpeg = encodeJpeg(fit(normalized, variant.getMaxEdge()));
                objectStorage.put(key, new ByteArrayInputStream(jpeg), jpeg.length, MediaType.IMAGE_JPEG_VALUE);
                uploaded.add(key);
            }
        } catch (RuntimeException ex) {
            uploaded.forEach(objectStorage::removeQuietly);
            throw ex;
        }
        return new StoredImage(objectKey);
    }

    @Override
    public void discard(StoredImage image) {
        for (ImageVariant variant : ImageVariant.values()) {
            objectStorage.removeQuietly(variant.keyOf(image.objectKey()));
        }
    }

    // ≤ 5 MB; tệp rỗng coi như sai kiểu
    private static byte[] readContent(MultipartFile file) {
        if (file.getSize() > ImageLimits.MAX_FILE_BYTES) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        try {
            byte[] content = file.getBytes();
            if (content.length == 0) {
                throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE);
            }
            return content;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE, "Cannot read upload: " + ex.getMessage());
        }
    }

    // Đọc kích thước TRƯỚC khi giải mã toàn bộ - chặn ảnh làm tràn bộ nhớ
    private static BufferedImage decode(byte[] content, ImageFormat format) {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(format.getReaderName());
        if (!readers.hasNext()) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE, "No ImageIO reader for " + format);
        }
        ImageReader reader = readers.next();
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            reader.setInput(input, true, true);
            if ((long) reader.getWidth(0) * reader.getHeight(0) > ImageLimits.MAX_PIXELS) {
                throw new BusinessException(ErrorCode.IMAGE_DIMENSIONS_TOO_LARGE);
            }
            return reader.read(0);
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            // Tệp hỏng / chỉ trông như ảnh
            throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE, "Cannot decode image: " + ex.getMessage());
        } finally {
            reader.dispose();
        }
    }

    // Không có / hỏng EXIF -> không xoay
    private static ExifOrientation readOrientation(byte[] content) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(content));
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                return ExifOrientation.fromTag(directory.getInt(ExifIFD0Directory.TAG_ORIENTATION));
            }
        } catch (ImageProcessingException | IOException | MetadataException ex) {
            return ExifOrientation.NORMAL;
        }
        return ExifOrientation.NORMAL;
    }

    // Xoay theo EXIF + làm phẳng nền trong suốt trên nền trắng; ảnh đích không mang metadata
    private static BufferedImage normalize(BufferedImage source, ExifOrientation orientation) {
        int width = source.getWidth();
        int height = source.getHeight();
        boolean swap = orientation.swapsDimensions();
        BufferedImage target = new BufferedImage(swap ? height : width, swap ? width : height,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, target.getWidth(), target.getHeight());
            graphics.drawImage(source, orientation.transform(width, height), null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    // Thu cạnh dài về maxEdge, không phóng to
    private static BufferedImage fit(BufferedImage source, int maxEdge) {
        int longEdge = Math.max(source.getWidth(), source.getHeight());
        if (longEdge <= maxEdge) {
            return source;
        }
        double scale = (double) maxEdge / longEdge;
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
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

    // JPEG 0.85, không metadata - mọi ảnh lưu ra đều là image/jpeg
    private static byte[] encodeJpeg(BufferedImage image) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName(ImageFormat.JPEG.getReaderName()).next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(ImageLimits.JPEG_QUALITY);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(stream);
            writer.write(null, new IIOImage(image, null, null), param);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Cannot encode JPEG: " + ex.getMessage());
        } finally {
            writer.dispose();
        }
        return output.toByteArray();
    }
}
