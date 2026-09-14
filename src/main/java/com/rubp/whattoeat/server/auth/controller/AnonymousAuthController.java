package com.rubp.whattoeat.server.auth.controller;

import com.rubp.whattoeat.server.auth.model.CreateAnonymousAccountResponse;
import com.rubp.whattoeat.server.auth.service.AnonymousAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/anonymous")
public class AnonymousAuthController {

    private final AnonymousAuthService anonymousAuthService;

    public AnonymousAuthController(AnonymousAuthService anonymousAuthService) {
        this.anonymousAuthService = anonymousAuthService;
    }


    @PostMapping
    public CreateAnonymousAccountResponse createAnonymousAccount(){
        return anonymousAuthService.createAnonymousAccount();
    }
}
