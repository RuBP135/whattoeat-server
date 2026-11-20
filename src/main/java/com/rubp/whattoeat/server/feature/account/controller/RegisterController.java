package com.rubp.whattoeat.server.feature.account.controller;

import com.rubp.whattoeat.server.feature.account.model.StartRegistrationRequest;
import com.rubp.whattoeat.server.feature.account.model.StartRegistrationResponse;
import com.rubp.whattoeat.server.feature.account.model.VerifyRegistrationRequest;
import com.rubp.whattoeat.server.feature.account.service.RegisterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts/register")
public class RegisterController {

    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @PostMapping("/start")
    public StartRegistrationResponse startRegistration(
            @Valid @RequestBody StartRegistrationRequest request
    ) {
        return registerService.startRegistration(request);
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyRegistration(@Valid @RequestBody VerifyRegistrationRequest request) {
        registerService.verifyRegistration(request);
    }
}
