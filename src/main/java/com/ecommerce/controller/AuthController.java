package com.ecommerce.controller;


import com.ecommerce.payload.dto.LoginDTO;
import com.ecommerce.payload.response.LoginResponse;
import com.ecommerce.payload.response.MessageResponse;
import com.ecommerce.payload.dto.SignUpDTO;
import com.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginDTO loginDTO) {
        LoginResponse response = authService.authenticateUser(loginDTO);
        ResponseCookie responseCookie = authService.generateCookie(response.getUserDetails());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(response);
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpDTO signUpDTO) {
        authService.registerUser(signUpDTO);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/username")
    public String currentUserName(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping("/user")
    public ResponseEntity<LoginResponse> getUserDetails(Authentication authentication) {
        LoginResponse response = authService.getUserDetails(authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("signOut")
    public ResponseEntity<?> signOutUser() {
        ResponseCookie cookie = authService.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new MessageResponse("You've been signed out!"));
    }
}
