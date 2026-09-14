package com.rubp.whattoeat.server.auth.controller;

import com.rubp.whattoeat.server.auth.model.RefreshAccessTokenRequest;
import com.rubp.whattoeat.server.auth.model.RefreshAccessTokenResponse;
import com.rubp.whattoeat.server.auth.service.RefreshService;
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
