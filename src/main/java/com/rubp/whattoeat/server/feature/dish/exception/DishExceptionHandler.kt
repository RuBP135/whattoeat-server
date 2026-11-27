package com.rubp.whattoeat.server.feature.dish.exception

import com.rubp.whattoeat.server.core.error.ErrorResponse
import com.rubp.whattoeat.server.core.error.ErrorResponseFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(0)
class DishExceptionHandler(
    private val errorResponseFactory: ErrorResponseFactory
) {
    @ExceptionHandler(DishAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDishNameAlreadyExists(): ErrorResponse {
        return errorResponseFactory.create("DISH_NAME_EXISTS", "dish.name_exists")
    }

    @ExceptionHandler(DishNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleDishNotFound(): ErrorResponse {
        return errorResponseFactory.create("DISH_NOT_FOUND", "dish.not_found")
    }
}
