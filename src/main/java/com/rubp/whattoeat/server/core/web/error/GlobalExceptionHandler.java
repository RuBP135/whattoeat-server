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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpectedFailure(Exception exception) {
        log.error("Unexpected exception", exception);
        return new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "服务器内部错误"
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
                new ErrorResponse("INVALID_REQUEST", "请求参数无效"),
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
                new ErrorResponse("INVALID_REQUEST", "请求体格式无效"),
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
            body = new ErrorResponse("INTERNAL_SERVER_ERROR", "服务器内部错误");
        } else if (!(body instanceof ErrorResponse)) {
            HttpStatus status = HttpStatus.resolve(statusCode.value());
            String code = status == null ? "REQUEST_FAILED" : status.name();
            if (statusCode.value() == HttpStatus.BAD_REQUEST.value()) {
                code = "INVALID_REQUEST";
            }
            String message = switch (statusCode.value()) {
                case 400 -> "请求参数无效";
                case 404 -> "请求的资源不存在";
                case 405 -> "请求方法不支持";
                case 406 -> "无法提供请求所需的响应格式";
                case 415 -> "请求体格式不支持";
                default -> "请求处理失败";
            };
            body = new ErrorResponse(code, message);
        }

        return super.handleExceptionInternal(exception, body, headers, statusCode, request);
    }
}
