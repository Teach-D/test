package com.example.roulette.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    /** JWT 토큰 생성 */
    fun createToken(memberId: Long, role: String): String {
        val now = Date()
        return Jwts.builder()
            .subject(memberId.toString())
            .claim("role", role)
            .issuedAt(now)
            .expiration(Date(now.time + expiration))
            .signWith(key)
            .compact()
    }

    /** 토큰에서 회원 ID 추출 */
    fun getMemberId(token: String): Long {
        return getClaims(token).subject.toLong()
    }

    /** 토큰에서 역할 추출 */
    fun getRole(token: String): String {
        return getClaims(token).get("role", String::class.java)
    }

    /** 토큰 유효성 검증 */
    fun isValid(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getClaims(token: String) =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
}
