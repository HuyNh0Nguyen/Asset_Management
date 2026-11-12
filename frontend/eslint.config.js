// @ts-check
const eslint = require('@eslint/js');
const { defineConfig, globalIgnores } = require('eslint/config');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');

// Thông báo dùng chung cho luật import
const MSG_FEATURES = 'Chỉ app.routes.ts được tham chiếu features (qua import() động).';
const MSG_CROSS_FEATURE = 'Không import chéo feature — thứ dùng chung đưa lên shared.';
const MSG_SHARED_CORE = 'shared chỉ được dùng @core/i18n, không dùng auth / http / layout.';
const MSG_SHARED_API = 'shared chỉ dùng model (kiểu, enum) của @api, không gọi service.';

const FEATURES_BAN = { group: ['@features/*', '**/features/**'], message: MSG_FEATURES };

// Mỗi khối dưới đây ghi đè trọn danh sách của khối trước cho cùng tệp, nên phải lặp FEATURES_BAN
const restrictImports = (patterns) => ({
  '@typescript-eslint/no-restricted-imports': ['error', { patterns }],
});

module.exports = defineConfig([
  // Mã sinh tự động và thư mục build không lint
  globalIgnores(['src/app/api/**', 'dist/**', '.angular/**']),
  {
    files: ['**/*.ts'],
    extends: [
      eslint.configs.recommended,
      tseslint.configs.recommended,
      tseslint.configs.stylistic,
      angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      '@angular-eslint/directive-selector': [
        'error',
        {
          type: 'attribute',
          prefix: 'app',
          style: 'camelCase',
        },
      ],
      '@angular-eslint/component-selector': [
        'error',
        {
          type: 'element',
          prefix: 'app',
          style: 'kebab-case',
        },
      ],
      ...restrictImports([FEATURES_BAN]),
    },
  },
  {
    // features/X: không sang features/Y — cấu trúc feature tối đa 2 cấp (pages/<tên>/tệp)
    files: ['src/app/features/**/*.ts'],
    rules: restrictImports([
      { group: ['@features/*'], message: MSG_FEATURES },
      { group: ['../../../*'], message: MSG_CROSS_FEATURE },
    ]),
  },
  {
    files: ['src/app/shared/**/*.ts'],
    rules: restrictImports([
      FEATURES_BAN,
      { group: ['@core/auth/*', '@core/http/*', '@core/layout/*', '@core/routing/*'], message: MSG_SHARED_CORE },
      { group: ['@api/services', '@api/services/*', '@api/fn/*', '@api/functions', '@api/api'], message: MSG_SHARED_API },
    ]),
  },
  {
    // Ngoại lệ duy nhất: app.routes.ts nạp feature
    files: ['src/app/app.routes.ts'],
    rules: { '@typescript-eslint/no-restricted-imports': 'off' },
  },
  {
    files: ['**/*.html'],
    extends: [angular.configs.templateRecommended, angular.configs.templateAccessibility],
    rules: {},
  },
]);
