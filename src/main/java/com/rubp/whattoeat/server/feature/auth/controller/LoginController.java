package com.rubp.whattoeat.server.feature.auth.controller;

import com.rubp.whattoeat.server.feature.auth.model.LoginRequest;
import com.rubp.whattoeat.server.feature.auth.model.LoginResponse;
import com.rubp.whattoeat.server.feature.auth.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/login")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return loginService.login(request);
    }
}
