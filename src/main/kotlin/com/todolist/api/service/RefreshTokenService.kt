package com.todolist.api.service

import com.todolist.api.entity.RefreshToken
import com.todolist.api.entity.User
import com.todolist.api.repository.RefreshTokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Value("\${jwt.refresh-expiration}")
    private var refreshTokenDurationMs: Long = 2592000000 // 30 days default

    fun createRefreshToken(user: User): RefreshToken {
        val refreshToken = RefreshToken(
            token = UUID.randomUUID().toString(),
            user = user,
            expiryDate = LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000)
        )
        return refreshTokenRepository.save(refreshToken)
    }

    fun findByToken(token: String): Optional<RefreshToken> {
        return refreshTokenRepository.findByToken(token)
    }

    fun verifyExpiration(token: RefreshToken): RefreshToken {
        if (token.expiryDate.isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token)
            throw RuntimeException("Refresh token expired. Please login again.")
        }
        return token
    }

    @Transactional
    fun deleteByUser(user: User): Int {
        return refreshTokenRepository.deleteByUser(user)
    }
}
