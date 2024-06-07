package com.snelson.cadenceAPI.controller;

import com.snelson.cadenceAPI.model.RefreshToken;
import com.snelson.cadenceAPI.dto.RefreshTokenRequest;
import com.snelson.cadenceAPI.dto.RefreshTokenResponse;
import com.snelson.cadenceAPI.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @PostMapping("/token")
    public String token(Authentication authentication) {
        return tokenService.generateAccessToken(authentication);
    }

    @PostMapping("/refresh")
    public RefreshTokenResponse refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return tokenService.findRefreshToken(refreshTokenRequest.getRefreshToken())
                .map(tokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = tokenService.generateAccessTokenByUsername(user.getUsername());
                    return RefreshTokenResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshTokenRequest.getRefreshToken())
                            .build();
                }).orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    }
}
