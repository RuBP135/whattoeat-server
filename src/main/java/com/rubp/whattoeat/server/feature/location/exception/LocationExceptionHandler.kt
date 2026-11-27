package com.rubp.whattoeat.server.feature.location.exception

import com.rubp.whattoeat.server.core.error.ErrorResponse
import com.rubp.whattoeat.server.core.error.ErrorResponseFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(0)
class LocationExceptionHandler(
    private val errorResponseFactory: ErrorResponseFactory
) {
    @ExceptionHandler(CampusAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleCampusNameAlreadyExists(): ErrorResponse {
        return errorResponseFactory.create("CAMPUS_NAME_EXISTS", "location.campus.name_exists")
    }

    @ExceptionHandler(CampusNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleCampusNotFound(): ErrorResponse {
        return errorResponseFactory.create("CAMPUS_NOT_FOUND", "location.campus.not_found")
    }
}
