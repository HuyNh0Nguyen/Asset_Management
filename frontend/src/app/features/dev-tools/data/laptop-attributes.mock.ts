import { AttributeDataType, AttributeDefinitionResponse, CatalogStatus } from "@api/models";
import { AttributeValues } from "@shared/forms/dynamic-attribute-form/attribute-values.model";

// TODO: Dữ liệu giả - thay bằng API thật ở phase 3
export const LAPTOP_ATTRIBUTE_DEFINITIONS: AttributeDefinitionResponse[] = [
    { id: '4f179c08-c388-4f9a-9daf-f5993e16a0ed', code: 'cpu', label: 'CPU', dataType: AttributeDataType.TEXT, required: true, options: [], deadlineTracked: false, displayOrder: 1, status: CatalogStatus.ACTIVE },
    { id: '980aa281-3475-4efe-9a83-38843bebcaad', code: 'ram', label: 'RAM', dataType: AttributeDataType.NUMBER, required: true, measureUnit: 'GB', options: [], deadlineTracked: false, displayOrder: 2, status: CatalogStatus.ACTIVE },
    { id: '690c9130-a453-4948-91ec-2017978cd1f6', code: 'storage', label: 'Ổ cứng', dataType: AttributeDataType.NUMBER, required: false, measureUnit: 'GB', options: [], deadlineTracked: false, displayOrder: 3, status: CatalogStatus.ACTIVE },
    {
        id: '71c25f44-7ca0-4434-a266-48e2e6445857',
        code: 'os',
        label: 'Hệ điều hành',
        dataType: AttributeDataType.SELECT,
        required: false,
        options: [
            { code: 'WINDOWS', label: 'Windows' },
            { code: 'MACOS', label: 'macOS' },
            { code: 'LINUX', label: 'Linux' },
        ],
        deadlineTracked: false,
        displayOrder: 4,
        status: CatalogStatus.ACTIVE,
    },
    { id: '8fce8ce8-3418-4b03-b274-bdf73d60312f', code: 'support_expiry', label: 'Hạn hỗ trợ', dataType: AttributeDataType.DATE, required: false, options: [], deadlineTracked: true, displayOrder: 5, status: CatalogStatus.ACTIVE },
    { id: '1c6a0285-747a-499f-b2b9-947c0b83616b', code: 'has_dock', label: 'Kèm dock', dataType: AttributeDataType.BOOLEAN, required: true, options: [], deadlineTracked: false, displayOrder: 6, status: CatalogStatus.ACTIVE },
    { id: '68114170-d120-4ddb-a3f2-12c51a9c778d', code: 'screen', label: 'Màn hình', dataType: AttributeDataType.NUMBER, required: false, measureUnit: 'inch', options: [], deadlineTracked: false, displayOrder: 7, status: CatalogStatus.INACTIVE },
];

// Giá trị của một laptop đang có - "screen" đã ngừng dùng nhưng vẫn giữ giá trị
export const LAPTOP_ATTRIBUTE_VALUES: AttributeValues = {
    cpu: 'Intel Core i7-1365U',
    ram: 16,
    os: 'WINDOWS',
    screen: 14,
};