package com.market.global.jwt.repository;

import com.market.global.jwt.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    RefreshToken findByMemberNo(Long memberNo);
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
