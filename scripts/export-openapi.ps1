# Xuất hợp đồng OpenAPI từ backend đang chạy ra contracts/openapi.json
$ErrorActionPreference = 'Stop'

# Thư mục gốc repo = cha của scripts/
$root = Split-Path -Parent $PSScriptRoot
$apiDocsUrl = if ($env:API_DOCS_URL) { $env:API_DOCS_URL } else { 'http://localhost:8080/api/v3/api-docs' }
$outFile = Join-Path $root 'contracts/openapi.json'

# Git không lưu thư mục rỗng nên contracts/ có thể chưa tồn tại
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $outFile) | Out-Null

# -UseBasicParsing: chạy được trên cả Windows PowerShell 5.1 lẫn PowerShell 7
Invoke-WebRequest -Uri $apiDocsUrl -OutFile $outFile -UseBasicParsing
Write-Host "Exported OpenAPI contract to $outFile"