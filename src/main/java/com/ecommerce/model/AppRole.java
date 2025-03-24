package com.ecommerce.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum AppRole {
    ROLE_USER("user"),
    ROLE_SELLER("seller"),
    ROLE_ADMIN("admin");

    private final String stringValue;

    private static final Map<String, AppRole> STRING_TO_ENUM = new HashMap<>();

    static {
        for (AppRole role : values()) {
            STRING_TO_ENUM.put(role.stringValue, role);
        }
    }

    AppRole(String stringValue) {
        this.stringValue = stringValue;
    }

    public static AppRole fromString(String roleName) {
        return roleName != null
                ? STRING_TO_ENUM.getOrDefault(roleName.toLowerCase(), ROLE_USER)
                : ROLE_USER;
    }

}