package com.todolist.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.todolist.api.dto.LoginRequest
import com.todolist.api.dto.RefreshTokenRequest
import com.todolist.api.dto.RegisterRequest
import com.todolist.api.entity.RefreshToken
import com.todolist.api.entity.User
import com.todolist.api.repository.UserRepository
import com.todolist.api.service.CustomUserDetailsService
import com.todolist.api.service.JwtService
import com.todolist.api.service.RefreshTokenService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var authenticationManager: AuthenticationManager

    @MockBean
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var passwordEncoder: PasswordEncoder

    @MockBean
    private lateinit var jwtService: JwtService

    @MockBean
    private lateinit var refreshTokenService: RefreshTokenService

    @MockBean
    private lateinit var userDetailsService: CustomUserDetailsService

    @Test
    fun `should register user successfully`() {
        val request = RegisterRequest("testuser", "test@example.com", "password123")

        `when`(userRepository.existsByUsername(request.username)).thenReturn(false)
        `when`(userRepository.existsByEmail(request.email)).thenReturn(false)
        `when`(passwordEncoder.encode(request.password)).thenReturn("encodedPassword")

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.message").value("User registered successfully"))
    }

    @Test
    fun `should fail to register with existing username`() {
        val request = RegisterRequest("testuser", "test@example.com", "password123")

        `when`(userRepository.existsByUsername(request.username)).thenReturn(true)

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Username already exists"))
    }

    @Test
    fun `should login successfully`() {
        val request = LoginRequest("testuser", "password123")
        val user = User(1L, "testuser", "test@example.com", "encodedPassword")
        val userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(user.username)
            .password(user.password)
            .authorities(emptyList())
            .build()
        val refreshToken = RefreshToken(
            1L,
            "refresh-token-123",
            user,
            LocalDateTime.now().plusDays(30)
        )

        `when`(authenticationManager.authenticate(UsernamePasswordAuthenticationToken(request.username, request.password)))
            .thenReturn(null)
        `when`(userDetailsService.loadUserByUsername(request.username)).thenReturn(userDetails)
        `when`(userRepository.findByUsername(request.username)).thenReturn(Optional.of(user))
        `when`(jwtService.generateToken(userDetails)).thenReturn("access-token-123")
        `when`(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("access-token-123"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
    }

    @Test
    fun `should refresh token successfully`() {
        val request = RefreshTokenRequest("refresh-token-123")
        val user = User(1L, "testuser", "test@example.com", "encodedPassword")
        val userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(user.username)
            .password(user.password)
            .authorities(emptyList())
            .build()
        val refreshToken = RefreshToken(
            1L,
            "refresh-token-123",
            user,
            LocalDateTime.now().plusDays(30)
        )

        `when`(refreshTokenService.findByToken(request.refreshToken)).thenReturn(Optional.of(refreshToken))
        `when`(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken)
        `when`(userDetailsService.loadUserByUsername(user.username)).thenReturn(userDetails)
        `when`(jwtService.generateToken(userDetails)).thenReturn("new-access-token-123")

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("new-access-token-123"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"))
    }

    @Test
    fun `should logout successfully`() {
        val request = RefreshTokenRequest("refresh-token-123")
        val user = User(1L, "testuser", "test@example.com", "encodedPassword")
        val refreshToken = RefreshToken(
            1L,
            "refresh-token-123",
            user,
            LocalDateTime.now().plusDays(30)
        )

        `when`(refreshTokenService.findByToken(request.refreshToken)).thenReturn(Optional.of(refreshToken))

        mockMvc.perform(
            post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Logged out successfully"))
    }
}
