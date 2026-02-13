package com.todolist.api.exception

class RefreshTokenExpiredException(message: String) : RuntimeException(message)

class RefreshTokenNotFoundException(message: String) : RuntimeException(message)
