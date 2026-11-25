package com.rubp.whattoeat.server.core.web.error;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@NullMarked
@RestControllerAdvice
@Order
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ErrorResponseFactory errorResponseFactory;

    public GlobalExceptionHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpectedFailure(Exception exception) {
        log.error("Unexpected exception", exception);
        return errorResponseFactory.create(
                "INTERNAL_SERVER_ERROR",
                "error.internal_server_error"
        );
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return handleExceptionInternal(
                exception,
                errorResponseFactory.create("INVALID_REQUEST", "error.request.invalid_parameter"),
                headers,
                status,
                request
        );
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return handleExceptionInternal(
                exception,
                errorResponseFactory.create("INVALID_REQUEST", "error.request.invalid_body"),
                headers,
                status,
                request
        );
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        if (statusCode.is5xxServerError()) {
            log.error("Failed to process request", exception);
            body = errorResponseFactory.create("INTERNAL_SERVER_ERROR", "error.internal_server_error");
        } else if (!(body instanceof ErrorResponse)) {
            HttpStatus status = HttpStatus.resolve(statusCode.value());
            String code = status == null ? "REQUEST_FAILED" : status.name();
            if (statusCode.value() == HttpStatus.BAD_REQUEST.value()) {
                code = "INVALID_REQUEST";
            }
            body = switch (statusCode.value()) {
                case 400 -> errorResponseFactory.create(code, "error.request.invalid_parameter");
                case 404 -> errorResponseFactory.create(code, "error.http.not_found");
                case 405 -> errorResponseFactory.create(code, "error.http.method_not_allowed");
                case 406 -> errorResponseFactory.create(code, "error.http.not_acceptable");
                case 415 -> errorResponseFactory.create(code, "error.http.unsupported_media_type");
                default -> errorResponseFactory.create(code, "error.http.request_failed");
            };
        }

        return super.handleExceptionInternal(exception, body, headers, statusCode, request);
    }
}
