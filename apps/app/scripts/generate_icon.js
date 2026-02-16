/**
 * 포인트 룰렛 앱 아이콘 생성 스크립트
 * Node.js 내장 모듈(zlib)만 사용해 PNG 파일을 직접 생성한다.
 *
 * 아이콘 디자인:
 * - 배경: 인디고 (#4F46E5)
 * - 룰렛 휠: 6개의 섹터 (교대 색상)
 * - 중앙: 흰색 원 + "P" 글자 (포인트 심볼)
 */

const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const SIZE = 1024;

// RGBA 픽셀 버퍼 생성 (모두 투명으로 초기화)
const pixels = Buffer.alloc(SIZE * SIZE * 4, 0);

/**
 * 픽셀 좌표에 RGBA 색상을 설정한다.
 * @param {number} x - X 좌표
 * @param {number} y - Y 좌표
 * @param {number} r - Red (0~255)
 * @param {number} g - Green (0~255)
 * @param {number} b - Blue (0~255)
 * @param {number} a - Alpha (0~255)
 */
function setPixel(x, y, r, g, b, a = 255) {
  if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return;
  const offset = (y * SIZE + x) * 4;
  pixels[offset] = r;
  pixels[offset + 1] = g;
  pixels[offset + 2] = b;
  pixels[offset + 3] = a;
}

/**
 * 두 색상을 알파 블렌딩한다. (안티앨리어싱용)
 */
function blendPixel(x, y, r, g, b, alpha) {
  if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return;
  const offset = (y * SIZE + x) * 4;
  const srcA = alpha / 255;
  const dstA = pixels[offset + 3] / 255;
  const outA = srcA + dstA * (1 - srcA);
  if (outA === 0) return;
  pixels[offset] = Math.round((r * srcA + pixels[offset] * dstA * (1 - srcA)) / outA);
  pixels[offset + 1] = Math.round((g * srcA + pixels[offset + 1] * dstA * (1 - srcA)) / outA);
  pixels[offset + 2] = Math.round((b * srcA + pixels[offset + 2] * dstA * (1 - srcA)) / outA);
  pixels[offset + 3] = Math.round(outA * 255);
}

/**
 * 원 내부를 채운다. (안티앨리어싱 적용)
 */
function fillCircle(cx, cy, radius, r, g, b) {
  const r2 = radius * radius;
  const x0 = Math.max(0, Math.floor(cx - radius - 1));
  const x1 = Math.min(SIZE - 1, Math.ceil(cx + radius + 1));
  const y0 = Math.max(0, Math.floor(cy - radius - 1));
  const y1 = Math.min(SIZE - 1, Math.ceil(cy + radius + 1));

  for (let y = y0; y <= y1; y++) {
    for (let x = x0; x <= x1; x++) {
      const dx = x - cx;
      const dy = y - cy;
      const dist2 = dx * dx + dy * dy;
      const dist = Math.sqrt(dist2);

      if (dist <= radius - 1) {
        setPixel(x, y, r, g, b, 255);
      } else if (dist <= radius + 1) {
        // 안티앨리어싱: 경계 근처 픽셀에 알파 적용
        const alpha = Math.round((radius + 1 - dist) * 127);
        blendPixel(x, y, r, g, b, alpha);
      }
    }
  }
}

/**
 * 원형 섹터(부채꼴)를 채운다.
 * @param {number} cx - 중심 X
 * @param {number} cy - 중심 Y
 * @param {number} innerR - 내부 반지름 (0이면 중심부터)
 * @param {number} outerR - 외부 반지름
 * @param {number} startAngle - 시작 각도 (라디안)
 * @param {number} endAngle - 끝 각도 (라디안)
 * @param {number} r, g, b - 색상
 */
function fillSector(cx, cy, innerR, outerR, startAngle, endAngle, r, g, b) {
  const x0 = Math.max(0, Math.floor(cx - outerR - 1));
  const x1 = Math.min(SIZE - 1, Math.ceil(cx + outerR + 1));
  const y0 = Math.max(0, Math.floor(cy - outerR - 1));
  const y1 = Math.min(SIZE - 1, Math.ceil(cy + outerR + 1));

  for (let y = y0; y <= y1; y++) {
    for (let x = x0; x <= x1; x++) {
      const dx = x - cx;
      const dy = y - cy;
      const dist = Math.sqrt(dx * dx + dy * dy);

      if (dist < innerR || dist > outerR) continue;

      // 각도 계산 (-π ~ π)
      let angle = Math.atan2(dy, dx);

      // 각도 정규화: startAngle ~ endAngle 범위 내인지 확인
      let normalizedAngle = angle;
      while (normalizedAngle < startAngle) normalizedAngle += Math.PI * 2;
      while (normalizedAngle > startAngle + Math.PI * 2) normalizedAngle -= Math.PI * 2;

      if (normalizedAngle >= startAngle && normalizedAngle <= endAngle) {
        setPixel(x, y, r, g, b, 255);
      }
    }
  }
}

/**
 * 원형 테두리(링)를 그린다.
 */
function drawCircleRing(cx, cy, innerR, outerR, r, g, b) {
  fillSector(cx, cy, innerR, outerR, -Math.PI, Math.PI, r, g, b);
}

/**
 * 섹터 구분선(선)을 그린다.
 */
function drawLine(x0, y0, x1, y1, r, g, b, thickness = 3) {
  const dx = x1 - x0;
  const dy = y1 - y0;
  const len = Math.sqrt(dx * dx + dy * dy);
  const steps = Math.ceil(len * 2);

  for (let i = 0; i <= steps; i++) {
    const t = i / steps;
    const px = x0 + dx * t;
    const py = y0 + dy * t;

    for (let ox = -thickness; ox <= thickness; ox++) {
      for (let oy = -thickness; oy <= thickness; oy++) {
        if (ox * ox + oy * oy <= thickness * thickness) {
          setPixel(Math.round(px + ox), Math.round(py + oy), r, g, b, 255);
        }
      }
    }
  }
}

// ============================================================
// 아이콘 그리기 시작
// ============================================================

const centerX = SIZE / 2;
const centerY = SIZE / 2;

// 1. 배경: 인디고 (#4F46E5) 전체 채우기
for (let y = 0; y < SIZE; y++) {
  for (let x = 0; x < SIZE; x++) {
    setPixel(x, y, 0x4F, 0x46, 0xE5, 255);
  }
}

// 2. 룰렛 휠 외곽 원 (흰색 테두리)
fillCircle(centerX, centerY, 420, 255, 255, 255);

// 3. 룰렛 휠 6개 섹터 (교대 색상)
const sectorColors = [
  [0xFF, 0xD7, 0x00], // 골드 (노란색)
  [0xFF, 0x45, 0x00], // 오렌지레드
  [0xFF, 0xD7, 0x00], // 골드
  [0xFF, 0x45, 0x00], // 오렌지레드
  [0xFF, 0xD7, 0x00], // 골드
  [0xFF, 0x45, 0x00], // 오렌지레드
];

const sectorCount = 6;
const angleStep = (Math.PI * 2) / sectorCount;

for (let i = 0; i < sectorCount; i++) {
  const startAngle = -Math.PI / 2 + i * angleStep;
  const endAngle = startAngle + angleStep;
  const [r, g, b] = sectorColors[i];
  fillSector(centerX, centerY, 0, 415, startAngle, endAngle, r, g, b);
}

// 4. 섹터 구분선 (흰색)
for (let i = 0; i < sectorCount; i++) {
  const angle = -Math.PI / 2 + i * angleStep;
  const lineEndX = centerX + Math.cos(angle) * 415;
  const lineEndY = centerY + Math.sin(angle) * 415;
  drawLine(centerX, centerY, lineEndX, lineEndY, 255, 255, 255, 4);
}

// 5. 내부 장식 링 (인디고)
drawCircleRing(centerX, centerY, 180, 210, 0x4F, 0x46, 0xE5);

// 6. 중앙 원 배경 (인디고)
fillCircle(centerX, centerY, 180, 0x4F, 0x46, 0xE5);

// 7. 중앙 흰색 원 (메인)
fillCircle(centerX, centerY, 160, 255, 255, 255);

// 8. 중앙에 "P" 글자 픽셀 아트로 그리기 (포인트 심볼)
// P 글자 픽셀맵 (32x40 크기 기준, 인디고 색)
function drawLetterP(cx, cy, scale, r, g, b) {
  // P 글자 픽셀 맵 (1=채움, 0=비움)
  const letterP = [
    [1, 1, 1, 1, 1, 0],
    [1, 0, 0, 0, 1, 1],
    [1, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 1, 1],
    [1, 1, 1, 1, 1, 0],
    [1, 0, 0, 0, 0, 0],
    [1, 0, 0, 0, 0, 0],
    [1, 0, 0, 0, 0, 0],
  ];

  const rows = letterP.length;
  const cols = letterP[0].length;
  const totalW = cols * scale;
  const totalH = rows * scale;
  const startX = Math.floor(cx - totalW / 2);
  const startY = Math.floor(cy - totalH / 2);

  for (let row = 0; row < rows; row++) {
    for (let col = 0; col < cols; col++) {
      if (letterP[row][col] === 1) {
        for (let py = 0; py < scale; py++) {
          for (let px = 0; px < scale; px++) {
            setPixel(startX + col * scale + px, startY + row * scale + py, r, g, b, 255);
          }
        }
      }
    }
  }
}

drawLetterP(centerX, centerY, 18, 0x4F, 0x46, 0xE5);

// 9. 화살표 포인터 (상단, 흰색 삼각형)
function fillTriangle(x0, y0, x1, y1, x2, y2, r, g, b) {
  const minX = Math.max(0, Math.min(x0, x1, x2));
  const maxX = Math.min(SIZE - 1, Math.max(x0, x1, x2));
  const minY = Math.max(0, Math.min(y0, y1, y2));
  const maxY = Math.min(SIZE - 1, Math.max(y0, y1, y2));

  for (let y = minY; y <= maxY; y++) {
    for (let x = minX; x <= maxX; x++) {
      // 무게중심 좌표로 삼각형 내부 판별
      const d1 = (x - x1) * (y0 - y1) - (x0 - x1) * (y - y1);
      const d2 = (x - x2) * (y1 - y2) - (x1 - x2) * (y - y2);
      const d3 = (x - x0) * (y2 - y0) - (x2 - x0) * (y - y0);
      const hasNeg = d1 < 0 || d2 < 0 || d3 < 0;
      const hasPos = d1 > 0 || d2 > 0 || d3 > 0;
      if (!(hasNeg && hasPos)) {
        setPixel(x, y, r, g, b, 255);
      }
    }
  }
}

// 상단 포인터 삼각형
fillTriangle(
  centerX, centerY - 470,       // 꼭짓점 (위)
  centerX - 35, centerY - 430,  // 왼쪽 아래
  centerX + 35, centerY - 430,  // 오른쪽 아래
  255, 255, 255
);

// ============================================================
// PNG 인코딩 및 파일 저장
// ============================================================

/**
 * PNG 청크를 생성한다.
 * @param {string} type - 청크 타입 (4바이트 ASCII)
 * @param {Buffer} data - 청크 데이터
 * @returns {Buffer} - 완성된 청크 바이트
 */
function createPngChunk(type, data) {
  const length = Buffer.alloc(4);
  length.writeUInt32BE(data.length, 0);

  const typeBuffer = Buffer.from(type, 'ascii');
  const crcData = Buffer.concat([typeBuffer, data]);

  // CRC32 계산
  const crc = crc32(crcData);
  const crcBuffer = Buffer.alloc(4);
  crcBuffer.writeUInt32BE(crc >>> 0, 0);

  return Buffer.concat([length, typeBuffer, data, crcBuffer]);
}

/**
 * CRC32를 계산한다.
 */
function crc32(buf) {
  let crc = 0xFFFFFFFF;
  for (let i = 0; i < buf.length; i++) {
    crc ^= buf[i];
    for (let j = 0; j < 8; j++) {
      crc = (crc & 1) ? (0xEDB88320 ^ (crc >>> 1)) : (crc >>> 1);
    }
  }
  return (crc ^ 0xFFFFFFFF) >>> 0;
}

/**
 * PNG 파일을 생성한다.
 * @param {Buffer} pixelData - RGBA 픽셀 데이터
 * @param {number} width
 * @param {number} height
 * @returns {Buffer} - PNG 파일 바이너리
 */
function encodePng(pixelData, width, height) {
  // PNG 시그니처
  const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);

  // IHDR 청크: 이미지 헤더
  const ihdrData = Buffer.alloc(13);
  ihdrData.writeUInt32BE(width, 0);
  ihdrData.writeUInt32BE(height, 4);
  ihdrData[8] = 8;  // bit depth
  ihdrData[9] = 2;  // color type: RGB (알파 없이 RGB로 저장)
  ihdrData[10] = 0; // compression
  ihdrData[11] = 0; // filter
  ihdrData[12] = 0; // interlace

  // 이미지 데이터: 각 행 앞에 필터 바이트(0) 추가, RGB만 추출
  const rawRows = [];
  for (let y = 0; y < height; y++) {
    const row = Buffer.alloc(1 + width * 3);
    row[0] = 0; // 필터 타입: None
    for (let x = 0; x < width; x++) {
      const srcOffset = (y * width + x) * 4;
      const dstOffset = 1 + x * 3;
      row[dstOffset] = pixelData[srcOffset];     // R
      row[dstOffset + 1] = pixelData[srcOffset + 1]; // G
      row[dstOffset + 2] = pixelData[srcOffset + 2]; // B
    }
    rawRows.push(row);
  }

  const rawData = Buffer.concat(rawRows);

  // zlib 압축
  const compressed = zlib.deflateSync(rawData, { level: 6 });

  // IDAT 청크: 이미지 데이터
  const idatChunk = createPngChunk('IDAT', compressed);
  const ihdrChunk = createPngChunk('IHDR', ihdrData);
  const iendChunk = createPngChunk('IEND', Buffer.alloc(0));

  return Buffer.concat([signature, ihdrChunk, idatChunk, iendChunk]);
}

// 아이콘 PNG 생성 및 저장
const pngBuffer = encodePng(pixels, SIZE, SIZE);
const assetsDir = path.join(__dirname, '..', 'assets');

const iconPath = path.join(assetsDir, 'icon.png');
const iconFgPath = path.join(assetsDir, 'icon_foreground.png');

fs.writeFileSync(iconPath, pngBuffer);
console.log(`아이콘 저장 완료: ${iconPath} (${(pngBuffer.length / 1024).toFixed(1)} KB)`);

// 전경 이미지: 배경 없이 룰렛 심볼만 (배경 투명)
// flutter_launcher_icons adaptive_icon_foreground는 투명 PNG 권장
// 여기서는 동일 이미지를 전경으로도 사용 (배경 색상은 설정에서 별도 지정)
fs.writeFileSync(iconFgPath, pngBuffer);
console.log(`전경 아이콘 저장 완료: ${iconFgPath}`);

console.log('\n아이콘 생성 완료! flutter pub run flutter_launcher_icons 를 실행하세요.');
