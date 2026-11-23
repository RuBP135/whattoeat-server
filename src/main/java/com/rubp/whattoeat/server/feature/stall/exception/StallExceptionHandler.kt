package com.rubp.whattoeat.server.feature.stall.exception

import com.rubp.whattoeat.server.core.web.error.ErrorResponse
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(0)
class StallExceptionHandler {
    @ExceptionHandler(StallNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleStallNotFound(): ErrorResponse {
        return ErrorResponse("STALL_NOT_FOUND", "档口不存在")
    }

    @ExceptionHandler(StallCategoryAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleStallCategoryNameAlreadyExists(): ErrorResponse {
        return ErrorResponse("STALL_CATEGORY_NAME_EXISTS", "分类已存在")
    }

    @ExceptionHandler(StallCategoryNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleStallCategoryNotFound(): ErrorResponse {
        return ErrorResponse("STALL_CATEGORY_NOT_FOUND", "分类不存在")
    }
}
