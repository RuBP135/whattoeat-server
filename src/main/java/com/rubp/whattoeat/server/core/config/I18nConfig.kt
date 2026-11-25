package com.rubp.whattoeat.server.core.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.nio.charset.StandardCharsets
import java.util.Locale

@Configuration
class I18nConfig : WebMvcConfigurer {
    @Bean
    fun localeResolver(): LocaleResolver {
        val resolver = AcceptHeaderLocaleResolver()
        resolver.setDefaultLocale(Locale.CHINESE)
        resolver.supportedLocales = listOf(Locale.CHINESE, Locale.ENGLISH)
        return resolver
    }

    @Bean
    fun messageSource(): MessageSource {
        return ResourceBundleMessageSource().apply {
            setBasenames("i18n/messages")
            setDefaultEncoding(StandardCharsets.UTF_8.name())
            setFallbackToSystemLocale(false)
        }
    }
}
