package com.market.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.global.jwt.repository.RefreshTokenRepository;
import com.market.global.jwt.config.TokenAuthenticationFilter;
import com.market.global.jwt.config.TokenProvider;
import com.market.global.redis.RedisUtils;
import com.market.global.security.handler.ApiLoginSuccessHandler;
import com.market.global.security.handler.CustomAccessDeniedHandler;
import com.market.global.security.handler.CustomAuthenticationEntryPoint;
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

        @Bean
        public SecurityFilterChain filterChain1(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

            http
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers(HttpMethod.GET, "/api/members").hasAnyRole("ADMIN") // 전체 회원 목록 조회
                            .requestMatchers("/api/admin/**", "/api/markets/**", "/api/shops/**", "/api/items/**").hasAnyRole("ADMIN")
                            .requestMatchers("/", "/api/members/signup", "/members/login", "/api/members/login", "/api/members/logout",
                                    "/api/markets", "/api/shops", "/api/items", "/css/**", "/js/**").permitAll()
                            .requestMatchers("/api/members/**").authenticated()
                            .anyRequest().authenticated()
                    )

                    .formLogin(formLogin -> formLogin
                            .loginPage("/members/login")
//                            .usernameParameter("memberId")
//                            .passwordParameter("memberPw")
//                            .loginProcessingUrl("/members/login")
                            .failureUrl("/members/login")
////////                            .successHandler(new ApiLoginSuccessHandler())
//                            .defaultSuccessUrl("/success")
                    )
                    .logout(logout -> logout
//                            .logoutUrl("/members/logout")
                                    .logoutSuccessUrl("/")
                    )

                    .csrf((AbstractHttpConfigurer::disable))
//                    .httpBasic(AbstractHttpConfigurer::disable)
//                    .formLogin(AbstractHttpConfigurer::disable)
                    .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                    .logout(AbstractHttpConfigurer::disable)
                    .cors(Customizer.withDefaults())

                    .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                    .exceptionHandling(exceptionHandling -> exceptionHandling
                            // 401 Error 처리, Authorization 즉, 인증과정에서 실패할 시 처리
                            .authenticationEntryPoint(customAuthenticationEntryPoint)
                            // 403 Error 처리, 인증과는 별개로 추가적인 권한이 충족되지 않는 경우
                            .accessDeniedHandler(customAccessDeniedHandler));

            return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder(){
            return new BCryptPasswordEncoder();
        }

        @Bean
        public TokenAuthenticationFilter tokenAuthenticationFilter() {
            return new TokenAuthenticationFilter(tokenProvider, redisUtils);
        }

        @Bean
        public ApiLoginSuccessHandler apiLoginSuccessHandler() {
            return new ApiLoginSuccessHandler(tokenProvider, refreshTokenRepository, objectMapper, redisUtils);
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
            return authenticationConfiguration.getAuthenticationManager();
        }

    }
}