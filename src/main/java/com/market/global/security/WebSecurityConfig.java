package com.market.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.domain.member.repository.MemberRepository;
import com.market.global.jwt.config.TokenAuthenticationFilter;
import com.market.global.jwt.config.TokenProvider;
import com.market.global.jwt.repository.RefreshTokenRepository;
import com.market.global.redis.RedisUtils;
import com.market.global.security.handler.ApiLoginSuccessHandler;
import com.market.global.security.handler.CustomAccessDeniedHandler;
import com.market.global.security.handler.CustomAuthenticationEntryPoint;
import com.market.global.security.oauth2.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.market.global.security.oauth2.OAuth2SuccessHandler;
import com.market.global.security.oauth2.OAuth2UserCustomService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Configuration
    @RequiredArgsConstructor
    public static class MemberConfig {

        private final TokenProvider tokenProvider;
        private final ObjectMapper objectMapper;
        private final RefreshTokenRepository refreshTokenRepository;
        private final CustomAccessDeniedHandler customAccessDeniedHandler;
        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
        private final RedisUtils redisUtils;
        private final MemberRepository memberRepository;

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() { // security를 적용하지 않을 리소스
            return web -> web.ignoring()
                    .requestMatchers("/error", "/favicon.ico");
        }

        @Bean
        public SecurityFilterChain filterChain1(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
            http
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers(HttpMethod.GET, "/api/members").hasAnyRole("ADMIN") // 전체 회원 목록 조회
                            .requestMatchers("/api/admin/**", "/api/markets/**", "/api/shops/**", "/api/items/**").hasAnyRole("ADMIN")
                            .requestMatchers("/api/members/verifycode", "/api/members/addinfo", "/", "/api/members/signup", "/members/login", "/api/members/login", "/api/oauth2/login", "/api/members/logout",
                                    "/api/markets", "/api/shops", "/api/items", "/css/**", "/js/**").permitAll()
                            .requestMatchers("/api/members/**").authenticated()
                            .requestMatchers("/oauth2/authorization", "/*/oauth2/code/*", "/auth/success").permitAll() // oauth2
                            .anyRequest().permitAll() // authenticated로 바꾸기
                    )

                    .oauth2Login(oauth2 -> oauth2
//                            .loginPage("/members/login")
                            .authorizationEndpoint(authorization -> authorization
                                    .baseUri("/oauth2/authorization")
                                    .authorizationRequestRepository(
                                            oAuth2AuthorizationRequestBasedOnCookieRepository()))
                            .redirectionEndpoint(redirection -> redirection
                                    .baseUri("/login/oauth2/code/*"))
                            // OAuth2 로그인 성공 이후 사용자 정보를 가져올 때의 설정을 담당
                            .userInfoEndpoint(userinfo -> userinfo
                                    .userService(oAuth2UserCustomService()))
                            .successHandler(oAuth2SuccessHandler()))
                    .logout(logout -> logout
                            .logoutSuccessUrl("/"))

                    .csrf((AbstractHttpConfigurer::disable)) // cookie를 사용하지 않으면 꺼도 된다. (cookie를 사용할 경우 httpOnly(XSS 방어), sameSite(CSRF 방어)로 방어해야 한다.)
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .cors(Customizer.withDefaults())
//                    .logout(AbstractHttpConfigurer::disable)

                    .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                    .exceptionHandling(exceptionHandling -> exceptionHandling
                            // 401 Error 처리, Authorization 즉, 인증과정에서 실패할 시 처리
                            .authenticationEntryPoint(customAuthenticationEntryPoint)
                            // 403 Error 처리, 인증과는 별개로 추가적인 권한이 충족되지 않는 경우
                            .accessDeniedHandler(customAccessDeniedHandler));

            return http.build();
        }

        @Bean
        public OAuth2SuccessHandler oAuth2SuccessHandler() {
            return new OAuth2SuccessHandler(tokenProvider, oAuth2AuthorizationRequestBasedOnCookieRepository(),
                    memberRepository, redisUtils, objectMapper);
        }

        @Bean
        public TokenAuthenticationFilter tokenAuthenticationFilter() {
            return new TokenAuthenticationFilter(tokenProvider, redisUtils);
        }

        @Bean
        public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
            return new OAuth2AuthorizationRequestBasedOnCookieRepository();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                throws Exception {
            return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public ApiLoginSuccessHandler apiLoginSuccessHandler() {
            return new ApiLoginSuccessHandler(tokenProvider, refreshTokenRepository, objectMapper, redisUtils);
        }

        @Bean
        public OAuth2UserCustomService oAuth2UserCustomService() {
            return new OAuth2UserCustomService(memberRepository, passwordEncoder());
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}