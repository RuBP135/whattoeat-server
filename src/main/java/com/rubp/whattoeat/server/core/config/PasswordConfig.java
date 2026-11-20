package com.rubp.whattoeat.server.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder argon2Encoder() {
        return new Argon2PasswordEncoder(
                16,         // 盐长度：16 字节
                32,         // 哈希长度：32 字节
                1,          // 并行度
                19 * 1024,  // 内存成本：19 MiB
                2           // 迭代次数
        );
    }
}
