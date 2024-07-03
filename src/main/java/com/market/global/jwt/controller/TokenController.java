package com.market.global.jwt.controller;

import com.market.global.jwt.dto.CreateAccessTokenRequestDto;
import com.market.global.jwt.dto.CreateAccessTokenResponseDto;
import com.market.global.jwt.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
// 사용 안함
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/api/token")
    public ResponseEntity<CreateAccessTokenResponseDto> createNewAccessToken(
            @RequestBody CreateAccessTokenRequestDto request) {

        String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponseDto(newAccessToken));
    }

}
