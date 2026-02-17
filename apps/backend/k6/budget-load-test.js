import http from 'k6/http';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const success = new Counter('roulette_success');
const budgetExceeded = new Counter('roulette_budget_exceeded');

export const options = {
    scenarios: {
        budget_rush: {
            executor: 'shared-iterations',
            vus: 50,          // 50명 동시
            iterations: 50,   // 각 1회씩
            maxDuration: '60s',
        },
    },
};

export default function () {
    // 유저별 로그인 → 토큰 발급
    const loginRes = http.post(
        `${BASE_URL}/api/auth/login`,
        JSON.stringify({ nickname: `k6_user_${__VU}_${Date.now()}` }),
        { headers: { 'Content-Type': 'application/json' } },
    );
    const body = JSON.parse(loginRes.body);
    const token = body.data ? body.data.accessToken : null;

    if (!token) {
        console.log(`[VU ${__VU}] 로그인 실패: ${loginRes.status} ${loginRes.body}`);
        return;
    }

    // 룰렛 요청
    const res = http.post(`${BASE_URL}/api/roulette/spin`, null, {
        headers: { Authorization: `Bearer ${token}` },
    });

    if (res.status === 200) {
        success.add(1);
        console.log(`[VU ${__VU}] 성공: ${JSON.parse(res.body).data.point}p`);
    } else {
        budgetExceeded.add(1);
        const errBody = JSON.parse(res.body);
        console.log(`[VU ${__VU}] 거절: ${errBody.errorCode || res.status}`);
    }
}
