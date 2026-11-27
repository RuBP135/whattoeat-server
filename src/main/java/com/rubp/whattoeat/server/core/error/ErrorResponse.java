package com.rubp.whattoeat.server.core.error;

public record ErrorResponse(
        String code,
        String message
) { }
