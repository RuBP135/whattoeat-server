package com.rubp.whattoeat.server.core.web.error

import jakarta.servlet.http.HttpServletRequest
import org.jetbrains.annotations.PropertyKey
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.servlet.LocaleResolver
import java.util.Locale

private const val BUNDLE_NAME = "i18n.messages"

class ErrorResponseFactory(
    private val messageSource: MessageSource,
    private val localeResolver: LocaleResolver
) {
    fun create(
        code: String,
        @PropertyKey(resourceBundle = BUNDLE_NAME) messageKey: String,
        vararg args: Any
    ): ErrorResponse {
        return create(LocaleContextHolder.getLocale(), code, messageKey, *args)
    }

    fun create(
        request: HttpServletRequest,
        code: String,
        @PropertyKey(resourceBundle = BUNDLE_NAME) messageKey: String,
        vararg args: Any
    ): ErrorResponse {
        return create(localeResolver.resolveLocale(request), code, messageKey, *args)
    }

    fun create(
        locale: Locale,
        code: String,
        @PropertyKey(resourceBundle = BUNDLE_NAME) messageKey: String,
        vararg args: Any
    ): ErrorResponse {
        val resolvedArgs = if (args.isEmpty()) null else args
        val message = messageSource.getMessage(messageKey, resolvedArgs, locale)
        return ErrorResponse(code, message)
    }
}
