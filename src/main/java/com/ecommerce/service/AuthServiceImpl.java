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

import java.util.Collections;
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

    public LoginResponse authenticateUser(LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            return new LoginResponse(userDetails, roles);
        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException("Nieprawidłowe dane logowania");
        }
    }

    public User registerUser(SignUpDTO signUpDTO) {
        if (userRepository.existsByUserName(signUpDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpDTO.getEmail())) {
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
        return userRepository.save(user);
    }

    @Override
    public LoginResponse getUserDetails(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new LoginResponse(userDetails, roles);
    }

    @Override
    public ResponseCookie getCleanJwtCookie() {
        return jwtUtils.getCleanJwtCookie();
    }

    private Set<Role> assignRoles(Set<String> strRoles) {
        if (strRoles == null || strRoles.isEmpty()) {
            return Collections.singleton(roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", AppRole.ROLE_USER.toString())));
        }

        return strRoles.stream()
                .map(AppRole::fromString)
                .map(appRole -> roleRepository.findByRoleName(appRole)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", appRole.toString())))
                .collect(Collectors.toSet());
    }


    @Override
    public ResponseCookie generateCookie(UserDetailsImpl userDetails) {
        return jwtUtils.generateJwtCookie(userDetails);
    }
}
