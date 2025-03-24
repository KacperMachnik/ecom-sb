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
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        log.debug("Received request to login");
        LoginResponse response = authService.login(loginDTO);
        ResponseCookie responseCookie = authService.generateCookie(response.getUserDetails());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(response);
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpDTO signUpDTO) {
        log.info("Received request to register user");
        authService.registerUser(signUpDTO);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/username")
    public String currentUserName(Authentication authentication) {
        log.debug("Received request to get current user name");
        return authentication.getName();
    }

    @GetMapping("/user")
    public ResponseEntity<LoginResponse> getUserDetails(Authentication authentication) {
        log.debug("Received request to get user details");
        LoginResponse response = authService.getUserDetails(authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("signOut")
    public ResponseEntity<?> signOutUser() {
        log.debug("Received request to sign out");
        ResponseCookie cookie = authService.getCleanJwtCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }
}
