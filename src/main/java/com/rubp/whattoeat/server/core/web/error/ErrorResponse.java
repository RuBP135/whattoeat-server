package com.rubp.whattoeat.server.core.web.error;

public record ErrorResponse(
        String code,
        String message
) { }
