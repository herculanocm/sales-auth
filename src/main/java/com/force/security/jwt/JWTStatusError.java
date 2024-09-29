package com.force.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JWTStatusError {
    private String errorMessage;
    private String errorDetails;
    private int errorCode;
}
