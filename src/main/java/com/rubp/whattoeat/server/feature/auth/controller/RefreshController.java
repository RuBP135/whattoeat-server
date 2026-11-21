package com.rubp.whattoeat.server.feature.auth.controller;

import com.rubp.whattoeat.server.feature.auth.model.RefreshTokenPairRequest;
import com.rubp.whattoeat.server.feature.auth.model.RefreshTokenPairResponse;
import com.rubp.whattoeat.server.feature.auth.service.TokenRefreshService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/auth/refresh")
public class RefreshController {

    private final TokenRefreshService tokenRefreshService;

    public RefreshController(TokenRefreshService tokenRefreshService) {
        this.tokenRefreshService = tokenRefreshService;
    }

    @PostMapping
    public RefreshTokenPairResponse refreshTokenPair(
            @Valid @RequestBody RefreshTokenPairRequest request
    ){
        return tokenRefreshService.refreshTokenPair(request);
    }
}
