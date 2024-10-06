package com.market.global.jwt.controller;

import com.market.global.jwt.config.TokenProvider;
import com.market.global.jwt.dto.CreateAccessTokenRequestDto;
import com.market.global.jwt.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// 사용안함
@RestController
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;
    private final TokenProvider tokenProvider;

//    @PostMapping("/api/token")
//    public ResponseEntity<CreateAccessTokenResponseDto> createNewAccessToken(
//            @RequestBody CreateAccessTokenRequestDto request) {
//
//        String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(new CreateAccessTokenResponseDto(newAccessToken));
//    }

    // 새로운 액세스토큰 생성
    @PostMapping("/api/new-token")
    public ResponseEntity<String> createNewAccessToken(
        @RequestBody CreateAccessTokenRequestDto requestDto, HttpServletRequest request,
        HttpServletResponse response)
        throws Exception {

        String newAccessToken = tokenProvider.createNewAccessToken(requestDto.getRefreshToken(),
            request, response);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(newAccessToken);
    }

    // 액세스토큰 가져오기
    @GetMapping("/api/acc-token")
    public ResponseEntity<String> getAccessToken(HttpServletRequest request) {
        String accessToken = tokenService.getAccessTokenFromCookie(request);
        return ResponseEntity.ok().body(accessToken);
    }

    // 리프레시토큰 가져오기
    @GetMapping("/api/ref-token")
    public ResponseEntity<String> getRefreshToken(HttpServletRequest request) {
        String refreshToken = tokenService.getRefreshTokenFromCookie(request);
        return ResponseEntity.ok().body(refreshToken);
    }

}
