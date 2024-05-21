package com.market.global.security;

import com.market.domain.member.entity.Member;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Member member;

    public Member getMember() {
        return member;
    }

    public UserDetailsImpl(Member member) {
        this.member = member;
    }
    
    // 권한
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collectRole = new ArrayList<>();
        collectRole.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "ROLE_" + member.getRole().toString(); // ROLE_
            }
        });
        return collectRole;
    }

    @Override
    public String getPassword() {
        return member.getMemberPw();
    }

    @Override
    public String getUsername() {
        return member.getMemberId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}