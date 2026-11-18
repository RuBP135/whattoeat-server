package com.rubp.whattoeat.server.feature.auth.controller;

import com.rubp.whattoeat.server.feature.auth.model.RefreshAccessTokenRequest;
import com.rubp.whattoeat.server.feature.auth.model.RefreshAccessTokenResponse;
import com.rubp.whattoeat.server.feature.auth.service.RefreshService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/auth/refresh")
public class RefreshController {

    private final RefreshService refreshService;

    public RefreshController(RefreshService refreshService) {
        this.refreshService = refreshService;
    }

    @PostMapping
    public RefreshAccessTokenResponse refreshAccessToken(
            @Valid @RequestBody RefreshAccessTokenRequest request
    ){
        return refreshService.refreshAccessToken(request);
    }
}
