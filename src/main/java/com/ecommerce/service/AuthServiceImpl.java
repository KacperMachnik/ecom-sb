package com.ecommerce.service;

import com.ecommerce.exceptions.AuthenticationFailedException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.exceptions.UserAlreadyExistsException;
import com.ecommerce.model.AppRole;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.jwt.JwtUtils;
import com.ecommerce.payload.dto.LoginDTO;
import com.ecommerce.payload.response.LoginResponse;
import com.ecommerce.payload.dto.SignUpDTO;
import com.ecommerce.security.service.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    public LoginResponse login(LoginDTO loginDTO) {
        log.debug("Attempting login for user: {}", loginDTO.getUsername());
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            log.debug("User {} logged in successfully", loginDTO.getUsername());
            return new LoginResponse(userDetails, roles);
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {} - Reason: {}", loginDTO.getUsername(), e.getMessage());
            throw new AuthenticationFailedException("Nieprawidłowe dane logowania");
        }
    }

    public User registerUser(SignUpDTO signUpDTO) {
        log.debug("Registering new user: {}", signUpDTO.getUsername());
        if (userRepository.existsByUserName(signUpDTO.getUsername())) {
            log.debug("Username [{}] is already taken!", signUpDTO.getUsername());
            throw new UserAlreadyExistsException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpDTO.getEmail())) {
            log.debug("Email [{}] is already in use!", signUpDTO.getEmail());
            throw new UserAlreadyExistsException("Email is already in use!");
        }

        User user = new User(
                signUpDTO.getUsername(),
                signUpDTO.getEmail(),
                encoder.encode(signUpDTO.getPassword())
        );

        Set<String> strRoles = signUpDTO.getRole();
        Set<Role> roles = assignRoles(strRoles);

        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        log.info("User [{}] registered successfully", signUpDTO.getUsername());
        return savedUser;
    }

    @Override
    public LoginResponse getUserDetails(Authentication authentication) {
        log.debug("Fetching user details");
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        log.debug("User details fetched successfully");
        return new LoginResponse(userDetails, roles);
    }

    @Override
    public ResponseCookie getCleanJwtCookie() {
        log.debug("Generating clean JWT cookie");
        return jwtUtils.getCleanJwtCookie();
    }

    private Set<Role> assignRoles(Set<String> strRoles) {
        log.debug("Assigning roles");
        if (strRoles == null || strRoles.isEmpty()) {
            log.debug("No roles specified, assigning default ROLE_USER");
            return Set.of(roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> {
                        log.error("Role [{}] not found", AppRole.ROLE_USER);
                        return new ResourceNotFoundException("Role", "name", AppRole.ROLE_USER.toString());
                    }));
        }

        Set<Role> roles = strRoles.stream()
                .map(AppRole::fromString)
                .map(appRole -> {
                    log.debug("Assigning role: {}", appRole);
                    return roleRepository.findByRoleName(appRole)
                            .orElseThrow(() -> {
                                log.error("Role [{}] not found", appRole);
                                return new ResourceNotFoundException("Role", "name", appRole.toString());
                            });
                })
                .collect(Collectors.toSet());
        log.debug("Roles assigned successfully");
        return roles;
    }

    @Override
    public ResponseCookie generateCookie(UserDetailsImpl userDetails) {
        log.debug("Generating JWT cookie for user: {}", userDetails.getUsername());
        return jwtUtils.generateJwtCookie(userDetails);
    }
}