package com.ecommerce.service;


import com.ecommerce.model.User;
import com.ecommerce.payload.dto.LoginDTO;
import com.ecommerce.payload.response.LoginResponse;
import com.ecommerce.payload.dto.SignUpDTO;
import com.ecommerce.security.service.UserDetailsImpl;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;

public interface AuthService {
    LoginResponse authenticateUser(LoginDTO loginDTO);

    ResponseCookie generateCookie(UserDetailsImpl userDetails);

    User registerUser(SignUpDTO signUpDTO);

    ResponseCookie getCleanJwtCookie();

    LoginResponse getUserDetails(Authentication authentication);
}
