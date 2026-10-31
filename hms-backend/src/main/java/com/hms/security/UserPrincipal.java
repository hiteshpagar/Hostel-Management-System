package com.hms.security;

import com.hms.common.enums.UserStatus;
import com.hms.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final Long hostelId;
    private final String email;
    private final String password;
    private final String fullName;
    private final boolean mustChangePassword;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, Long hostelId, String email, String password, String fullName,
                         boolean mustChangePassword, boolean active, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.hostelId = hostelId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.mustChangePassword = mustChangePassword;
        this.active = active;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());
        Long hostelId = user.getHostel() != null ? user.getHostel().getId() : null;
        String fullName = user.getFirstName() + " " + user.getLastName();
        boolean isActive = user.getStatus() == UserStatus.ACTIVE;

        return new UserPrincipal(
                user.getId(),
                hostelId,
                user.getEmail(),
                user.getPasswordHash(),
                fullName,
                user.isMustChangePassword(),
                isActive,
                Collections.singletonList(authority)
        );
    }

    public Long getId() {
        return id;
    }

    public Long getHostelId() {
        return hostelId;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
