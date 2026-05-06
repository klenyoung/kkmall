package com.kkmall.account.domain;

public final class User {
    private final Long id;
    private final PhoneNumber phone;
    private final String nickname;
    private final Role role;

    public User(Long id, PhoneNumber phone, String nickname, Role role) {
        this.id = id;
        this.phone = phone;
        this.nickname = nickname;
        this.role = role;
    }

    public Long id() { return id; }
    public PhoneNumber phone() { return phone; }
    public String nickname() { return nickname; }
    public Role role() { return role; }
}
