package com.kkmall.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class AuthUser implements UserDetails {
    private final Long id;
    private final String phone;
    private final String role;

    public AuthUser(Long id, String phone, String role) {
        this.id = id;
        this.phone = phone;
        this.role = role;
    }

    public Long id() { return id; }
    public String role() { return role; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public String getPassword() { return ""; }
    @Override public String getUsername() { return phone; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
