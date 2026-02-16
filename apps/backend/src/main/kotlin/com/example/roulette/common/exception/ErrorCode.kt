package com.example.roulette.common.exception

enum class ErrorCode(
    val status: Int,
    val message: String,
) {
    // 인증
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "권한이 없습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),

    // 회원
    MEMBER_NOT_FOUND(404, "회원을 찾을 수 없습니다."),

    // 룰렛
    ROULETTE_ALREADY_PLAYED(400, "오늘 이미 룰렛을 돌렸습니다."),
    ROULETTE_NOT_FOUND(404, "룰렛 참여 기록을 찾을 수 없습니다."),
    ROULETTE_ALREADY_CANCELLED(400, "이미 취소된 룰렛 참여입니다."),

    // 예산
    BUDGET_EXCEEDED(400, "오늘 일일 예산이 소진되었습니다."),
    BUDGET_NOT_FOUND(404, "오늘의 예산 설정을 찾을 수 없습니다."),

    // 포인트
    POINT_NOT_ENOUGH(400, "포인트가 부족합니다."),
    POINT_NOT_FOUND(404, "포인트를 찾을 수 없습니다."),

    // 상품
    PRODUCT_NOT_FOUND(404, "상품을 찾을 수 없습니다."),
    PRODUCT_OUT_OF_STOCK(400, "상품 재고가 부족합니다."),
    PRODUCT_NOT_ACTIVE(400, "판매 중이 아닌 상품입니다."),
    PRODUCT_HAS_ORDERS(400, "주문 내역이 있는 상품은 삭제할 수 없습니다. 비활성화를 이용해주세요."),

    // 주문
    ORDER_NOT_FOUND(404, "주문을 찾을 수 없습니다."),
    ORDER_ALREADY_CANCELLED(400, "이미 취소된 주문입니다."),
}
