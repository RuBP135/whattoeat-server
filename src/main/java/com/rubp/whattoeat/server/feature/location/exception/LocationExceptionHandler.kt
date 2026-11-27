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

    @ExceptionHandler(CampusInUseException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleCampusInUse(): ErrorResponse {
        return errorResponseFactory.create("CAMPUS_IN_USE", "location.campus.in_use")
    }

    @ExceptionHandler(BuildingAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleBuildingNameAlreadyExists(): ErrorResponse {
        return errorResponseFactory.create("BUILDING_NAME_EXISTS", "location.building.name_exists")
    }

    @ExceptionHandler(BuildingNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleBuildingNotFound(): ErrorResponse {
        return errorResponseFactory.create("BUILDING_NOT_FOUND", "location.building.not_found")
    }

    @ExceptionHandler(BuildingInUseException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleBuildingInUse(): ErrorResponse {
        return errorResponseFactory.create("BUILDING_IN_USE", "location.building.in_use")
    }

    @ExceptionHandler(InvalidBuildingOrderException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidBuildingOrder(): ErrorResponse {
        return errorResponseFactory.create("INVALID_REQUEST", "error.request.invalid_parameter")
    }

    @ExceptionHandler(LocationAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleLocationNameAlreadyExists(): ErrorResponse {
        return errorResponseFactory.create("LOCATION_NAME_EXISTS", "location.name_exists")
    }

    @ExceptionHandler(LocationNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleLocationNotFound(): ErrorResponse {
        return errorResponseFactory.create("LOCATION_NOT_FOUND", "location.not_found")
    }

    @ExceptionHandler(LocationInUseException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleLocationInUse(): ErrorResponse {
        return errorResponseFactory.create("LOCATION_IN_USE", "location.in_use")
    }

    @ExceptionHandler(InvalidLocationOrderException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidLocationOrder(): ErrorResponse {
        return errorResponseFactory.create("INVALID_REQUEST", "error.request.invalid_parameter")
    }
}
