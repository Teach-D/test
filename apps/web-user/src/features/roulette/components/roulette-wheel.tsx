import { type ReactElement, useEffect, useRef, useState } from 'react';

interface RouletteWheelProps {
  isSpinning: boolean;
  result: number | null;
  onSpinComplete: () => void;
}

const SEGMENTS = [100, 200, 300, 400, 500, 600, 700, 800, 900, 1000];
const COLORS = [
  '#ef4444', // red-500
  '#f59e0b', // amber-500
  '#eab308', // yellow-500
  '#84cc16', // lime-500
  '#22c55e', // green-500
  '#14b8a6', // teal-500
  '#06b6d4', // cyan-500
  '#3b82f6', // blue-500
  '#6366f1', // indigo-500
  '#a855f7', // purple-500
];

export function RouletteWheel({
  isSpinning,
  result,
  onSpinComplete,
}: RouletteWheelProps): ReactElement {
  const wheelRef = useRef<HTMLDivElement>(null);
  const [rotation, setRotation] = useState(0);

  useEffect(() => {
    if (isSpinning && result !== null) {
      // 결과 값에 따른 최종 각도 계산
      const segmentIndex = SEGMENTS.indexOf(result);
      if (segmentIndex === -1) return;

      const segmentAngle = 360 / SEGMENTS.length;
      // 3~5바퀴 회전 + 목표 세그먼트 각도
      const fullRotations = 360 * (3 + Math.random() * 2);
      const targetAngle = segmentIndex * segmentAngle;
      // 세그먼트 중앙을 가리키도록 보정 (포인터가 위쪽에 있으므로)
      const finalRotation = fullRotations + (360 - targetAngle) - segmentAngle / 2;

      setRotation(finalRotation);

      // 애니메이션 완료 후 콜백 호출
      const timer = setTimeout(() => {
        onSpinComplete();
      }, 3000);

      return () => clearTimeout(timer);
    }
  }, [isSpinning, result, onSpinComplete]);

  return (
    <div className="relative flex items-center justify-center">
      {/* 포인터 (위쪽) */}
      <div className="absolute left-1/2 top-0 z-20 -translate-x-1/2 -translate-y-2">
        <div className="h-0 w-0 border-l-8 border-r-8 border-t-16 border-l-transparent border-r-transparent border-t-red-600"></div>
      </div>

      {/* 룰렛 휠 */}
      <div className="relative h-72 w-72">
        <div
          ref={wheelRef}
          className="relative h-full w-full rounded-full shadow-2xl transition-transform duration-[3000ms] ease-out"
          style={{
            transform: `rotate(${rotation}deg)`,
            transitionTimingFunction: isSpinning
              ? 'cubic-bezier(0.17, 0.67, 0.12, 0.99)'
              : 'ease-out',
          }}
        >
          {/* 중앙 원 */}
          <div className="absolute left-1/2 top-1/2 z-10 h-12 w-12 -translate-x-1/2 -translate-y-1/2 rounded-full border-4 border-white bg-gradient-to-br from-yellow-400 to-yellow-600 shadow-lg"></div>

          {/* 세그먼트들 */}
          <svg className="h-full w-full" viewBox="0 0 200 200">
            {SEGMENTS.map((value, index) => {
              const segmentAngle = 360 / SEGMENTS.length;
              const startAngle = index * segmentAngle - 90; // -90도부터 시작 (12시 방향)
              const endAngle = startAngle + segmentAngle;

              const startRad = (startAngle * Math.PI) / 180;
              const endRad = (endAngle * Math.PI) / 180;

              const x1 = 100 + 100 * Math.cos(startRad);
              const y1 = 100 + 100 * Math.sin(startRad);
              const x2 = 100 + 100 * Math.cos(endRad);
              const y2 = 100 + 100 * Math.sin(endRad);

              const pathData = `M 100 100 L ${x1} ${y1} A 100 100 0 0 1 ${x2} ${y2} Z`;

              // 텍스트 위치 (세그먼트 중앙)
              const midAngle = startAngle + segmentAngle / 2;
              const midRad = (midAngle * Math.PI) / 180;
              const textX = 100 + 65 * Math.cos(midRad);
              const textY = 100 + 65 * Math.sin(midRad);

              return (
                <g key={value}>
                  <path d={pathData} fill={COLORS[index]} stroke="white" strokeWidth="2" />
                  <text
                    x={textX}
                    y={textY}
                    fill="white"
                    fontSize="14"
                    fontWeight="bold"
                    textAnchor="middle"
                    dominantBaseline="middle"
                    transform={`rotate(${midAngle + 90}, ${textX}, ${textY})`}
                  >
                    {value}
                  </text>
                </g>
              );
            })}
          </svg>
        </div>
      </div>
    </div>
  );
}
