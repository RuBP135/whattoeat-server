package com.rubp.whattoeat.server.feature.stall.exception

import com.rubp.whattoeat.server.core.error.ErrorResponse
import com.rubp.whattoeat.server.core.error.ErrorResponseFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(0)
class StallExceptionHandler(
    private val errorResponseFactory: ErrorResponseFactory
) {
    @ExceptionHandler(StallNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleStallNotFound(): ErrorResponse {
        return errorResponseFactory.create("STALL_NOT_FOUND", "stall.not_found")
    }

    @ExceptionHandler(StallCategoryAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleStallCategoryNameAlreadyExists(): ErrorResponse {
        return errorResponseFactory.create("STALL_CATEGORY_NAME_EXISTS", "stall.category.name_exists")
    }

    @ExceptionHandler(StallCategoryNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleStallCategoryNotFound(): ErrorResponse {
        return errorResponseFactory.create("STALL_CATEGORY_NOT_FOUND", "stall.category.not_found")
    }

    @ExceptionHandler(StallCategoryUnavailableException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleStallCategoryUnavailable(): ErrorResponse {
        return errorResponseFactory.create("STALL_CATEGORY_UNAVAILABLE", "stall.category.unavailable")
    }

    @ExceptionHandler(LocationUnavailableException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleLocationUnavailable(): ErrorResponse {
        return errorResponseFactory.create("LOCATION_UNAVAILABLE", "stall.location_unavailable")
    }

    @ExceptionHandler(StallSlotOccupiedException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleStallSlotOccupied(): ErrorResponse {
        return errorResponseFactory.create("STALL_SLOT_OCCUPIED", "stall.slot_occupied")
    }
}
