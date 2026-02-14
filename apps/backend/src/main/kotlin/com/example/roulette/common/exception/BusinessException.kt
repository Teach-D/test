package com.example.roulette.common.exception

class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)
