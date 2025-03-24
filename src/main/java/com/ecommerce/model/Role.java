package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Enumerated(EnumType.STRING)
    private AppRole roleName;

    public Role(AppRole appRole) {
        this.roleName = appRole;
    }
}
