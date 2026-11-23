package com.rubp.whattoeat.server.core.config

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.cfg.CoercionAction
import tools.jackson.databind.cfg.CoercionInputShape

@Configuration
class JacksonConfig {
    @Bean
    fun strictJsonStrings(): JsonMapperBuilderCustomizer {
        return JsonMapperBuilderCustomizer { builder ->
            builder.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
            builder.withCoercionConfig(String::class.java) { config ->
                config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                config.setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                config.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
                config.setCoercion(CoercionInputShape.EmptyArray, CoercionAction.Fail)
            }
        }
    }
}
