package com.test.indomaret;

import com.test.indomaret.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestAuthHelper {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public String getAdminBearerToken() {
        return "Bearer " + jwtTokenProvider.generateTokenFromUsername("admin");
    }

    public String getUserBearerToken() {
        return "Bearer " + jwtTokenProvider.generateTokenFromUsername("user");
    }
}
