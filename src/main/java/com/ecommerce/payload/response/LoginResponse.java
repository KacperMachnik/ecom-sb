package com.ecommerce.payload.response;

import com.ecommerce.security.service.UserDetailsImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private UserDetailsImpl userDetails;
    private List<String> roles;
}