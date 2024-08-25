package com.market.global.security;

import com.market.domain.member.repository.MemberRepository;
import com.market.domain.member.withdrawMember.service.WithdrawMemberService;
import com.market.global.jwt.config.TokenAuthenticationFilter;
import com.market.global.jwt.config.TokenProvider;
import com.market.global.redis.RedisUtils;
import com.market.global.security.handler.CustomAccessDeniedHandler;
import com.market.global.security.handler.CustomAuthenticationEntryPoint;
import com.market.global.security.oauth2.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.market.global.security.oauth2.OAuth2SuccessHandler;
import com.market.global.security.oauth2.OAuth2UserCustomService;
import java.util.Arrays;
import java.util.List;

import com.market.global.visitor.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Configuration
    @RequiredArgsConstructor
    public static class MemberConfig {

        private final TokenProvider tokenProvider;
        private final CustomAccessDeniedHandler customAccessDeniedHandler;
        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
        private final RedisUtils redisUtils;
        private final MemberRepository memberRepository;
        private final VisitorService visitorService;
        private final WithdrawMemberService withdrawMemberService;

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() { // security를 적용하지 않을 리소스
            return web -> web.ignoring()
                .requestMatchers("/error", "/favicon.ico");
        }

        @Bean
        public SecurityFilterChain filterChain1(HttpSecurity http,
            AuthenticationManager authenticationManager) throws Exception {
            http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/members").hasAnyRole("ADMIN") // 전체 회원 목록 조회
                        .requestMatchers(HttpMethod.GET, "/api/members/admin/r/**").hasAnyRole("ADMIN") // 회원 권한 수정
                        .requestMatchers(HttpMethod.GET, "/api/inquiries").hasAnyRole("ADMIN") // 전체 문의사항 조회
                        .requestMatchers(HttpMethod.POST, "/api/notices").hasAnyRole("ADMIN") // 공지사항 생성
                        .requestMatchers(HttpMethod.PUT, "/api/notices/**").hasAnyRole("ADMIN") // 공지사항 수정
                        .requestMatchers(HttpMethod.DELETE, "/api/notices/**").hasAnyRole("ADMIN") // 공지사항 삭제
                        .requestMatchers(HttpMethod.DELETE, "/api/inquiries").hasAnyRole("ADMIN") // 모든 사람 문의사항 전체 삭제
                        .requestMatchers(HttpMethod.POST, "/api/inquiryanswer/**").hasAnyRole("ADMIN") // 문의사항에 답변 등록
                        .requestMatchers(HttpMethod.POST, "/api/markets/{marketNo}/likes").authenticated() // 시장 좋아요 생성
                        .requestMatchers(HttpMethod.GET, "/api/markets/{marketNo}/likes").authenticated() // 시장 좋아요 조회
                        .requestMatchers(HttpMethod.DELETE, "/api/markets/{marketNo}/likes").authenticated() // 시장 좋아요 삭제
                        .requestMatchers(HttpMethod.POST, "/api/markets").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/markets/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/markets/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/shops/{shopNo}/likes").authenticated() // 상점 좋아요 생성
                        .requestMatchers(HttpMethod.GET, "/api/shops/{shopNo}/likes").authenticated() // 상점 좋아요 조회
                        .requestMatchers(HttpMethod.DELETE, "/api/shops/{shopNo}/likes").authenticated() // 상점 좋아요 삭제
                        .requestMatchers(HttpMethod.POST, "/api/shops").hasAnyRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/shops/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/shops/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/items/{itemNo}/likes").authenticated() // 상품 좋아요 생성
                        .requestMatchers(HttpMethod.GET, "/api/items/{itemNo}/likes").authenticated() // 상품 좋아요 조회
                        .requestMatchers(HttpMethod.DELETE, "/api/items/{itemNo}/likes").authenticated() // 상품 좋아요 삭제
//                        .requestMatchers(HttpMethod.POST, "/api/items").hasAnyRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/items/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/items/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/chatrooms/{chatRoomNo}/read").hasAnyRole("ADMIN")
                        .requestMatchers("/admin/**", "/api/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers("/", "/api/visitors/**", "/api/members/signup", "/members/login",
                                "/api/members/verifycode", "/api/members/addinfo", "/api/members/findid",
                                "/api/oauth2/login", "/api/send-mail/**", "/api/members/checkemail", "/api/members/checkid",
                                "/api/markets", "/api/markets/**", "/api/shops", "/api/shops/**", "/api/members/myinfo/myinfo",
                                "/api/members/changepw", "/api/members/login", "/api/members/myinfo/check",
                                "/api/items", "/api/items/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/api/members/**","/api/chatrooms", "/api/chatrooms/**", "/myinfo/inquiry/**",
                                "/api/inquiries/**", "/api/notifications/subscribe", "/api/notifications", "/api/payment/**",
                                "/api/cartitems", "/api/carts", "/api/carts/**").authenticated()
                        .requestMatchers("/oauth2/authorization", "/*/oauth2/code/*", "/oauth/success").permitAll() // oauth2
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

                // cookie 를 사용하지 않으면 꺼도 된다. (cookie 를 사용할 경우 httpOnly(XSS 방어), sameSite(CSRF 방어)로 방어해야 한다.)
                .csrf((AbstractHttpConfigurer::disable))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(
                    management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(
                    corsConfigurationSource()))

                .addFilterBefore(tokenAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                    // 401 Error 처리, Authorization 즉, 인증과정에서 실패할 시 처리
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    // 403 Error 처리, 인증과는 별개로 추가적인 권한이 충족되지 않는 경우
                    .accessDeniedHandler(customAccessDeniedHandler));

            return http.build();
        }

        @Bean
        public OAuth2SuccessHandler oAuth2SuccessHandler() {
            return new OAuth2SuccessHandler(tokenProvider,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                memberRepository, redisUtils);
        }

        @Bean
        public TokenAuthenticationFilter tokenAuthenticationFilter() {
            return new TokenAuthenticationFilter(tokenProvider, visitorService);
        }

        @Bean
        public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
            return new OAuth2AuthorizationRequestBasedOnCookieRepository();
        }

        @Bean
        public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
            return authenticationConfiguration.getAuthenticationManager();
        }

//        @Bean
//        public ApiLoginSuccessHandler apiLoginSuccessHandler() {
//            return new ApiLoginSuccessHandler(tokenProvider, refreshTokenRepository, objectMapper,
//                redisUtils);
//        }

        @Bean
        public OAuth2UserCustomService oAuth2UserCustomService() {
            return new OAuth2UserCustomService(memberRepository, passwordEncoder(), withdrawMemberService);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean // 스프링 서버 전역적으로 CORS 설정
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOriginPatterns(List.of("http://localhost:3000"));
            configuration.setAllowedMethods(
                Arrays.asList("HEAD", "GET", "POST", "PUT", "PATCH", "DELETE")); // 모든 HTTP 메서드 허용
            configuration.setAllowedHeaders(
                Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
            configuration.setAllowCredentials(true); // 클라이언트와 서버 간에 쿠키 주고받기 허용

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);

            return source;
        }
    }
}