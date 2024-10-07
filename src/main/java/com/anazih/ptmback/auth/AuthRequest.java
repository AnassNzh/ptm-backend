package com.anazih.ptmback.auth;

public record AuthRequest(
    String email,
    String password
) {
}
