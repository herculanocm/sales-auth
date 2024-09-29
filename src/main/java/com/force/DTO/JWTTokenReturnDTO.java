package com.force.DTO;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JWTTokenReturnDTO {
    private String accessToken;
    private Set<String> roles;
    private String tokenType;
    private Integer expiresIn;
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JWTTokenReturnDTO other = (JWTTokenReturnDTO) obj;
        if (accessToken == null) {
            if (other.accessToken != null)
                return false;
        } else if (!accessToken.equals(other.accessToken))
            return false;
        return true;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
        return result;
    }
    @Override
    public String toString() {
        return "JWTTokenReturnDTO [accessToken=" + accessToken + ", roles=" + roles + ", tokenType=" + tokenType
                + ", expiresIn=" + expiresIn + "]";
    }
}
