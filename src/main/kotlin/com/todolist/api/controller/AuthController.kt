package com.todolist.api.controller

import com.todolist.api.dto.*
import com.todolist.api.entity.User
import com.todolist.api.exception.RefreshTokenNotFoundException
import com.todolist.api.repository.UserRepository
import com.todolist.api.service.CustomUserDetailsService
import com.todolist.api.service.JwtService
import com.todolist.api.service.RefreshTokenService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val userDetailsService: CustomUserDetailsService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<MessageResponse> {
        if (userRepository.existsByUsername(request.username)) {
            return ResponseEntity.badRequest().body(MessageResponse("Username already exists"))
        }

        if (userRepository.existsByEmail(request.email)) {
            return ResponseEntity.badRequest().body(MessageResponse("Email already exists"))
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )

        userRepository.save(user)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(MessageResponse("User registered successfully"))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        val userDetails = userDetailsService.loadUserByUsername(request.username)
        val user = userRepository.findByUsername(request.username)
            .orElseThrow { IllegalStateException("User not found after successful authentication") }

        val accessToken = jwtService.generateToken(userDetails)
        val refreshToken = refreshTokenService.createRefreshToken(user)

        return ResponseEntity.ok(
            AuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken.token
            )
        )
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        return refreshTokenService.findByToken(request.refreshToken)
            .map { refreshTokenService.verifyExpiration(it) }
            .map { refreshToken ->
                val user = refreshToken.user
                val userDetails = userDetailsService.loadUserByUsername(user.username)
                val accessToken = jwtService.generateToken(userDetails)
                
                ResponseEntity.ok(
                    AuthResponse(
                        accessToken = accessToken,
                        refreshToken = refreshToken.token
                    )
                )
            }
            .orElseThrow { RefreshTokenNotFoundException("Refresh token not found") }
    }

    @PostMapping("/logout")
    fun logout(@RequestBody request: RefreshTokenRequest): ResponseEntity<MessageResponse> {
        refreshTokenService.findByToken(request.refreshToken)
            .ifPresent { refreshTokenService.deleteByUser(it.user) }
        
        return ResponseEntity.ok(MessageResponse("Logged out successfully"))
    }
}
