package com.rubp.whattoeat.server.web.error;

public record ErrorResponse(
        String code,
        String message
) { }
