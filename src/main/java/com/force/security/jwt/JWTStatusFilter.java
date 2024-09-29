package com.force.security.jwt;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JWTStatusFilter {
    private String jwt;
    private String userId;
    private String companyId;
    private Set<String> permissions;
    private String publicKey;
    private Boolean valid;
    private JWTStatusError error;
}
