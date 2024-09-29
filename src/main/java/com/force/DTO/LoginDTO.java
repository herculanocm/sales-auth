package com.force.DTO;


import com.force.controller.DefaultValuesConstants;
import com.force.util.ValidUUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDTO {

    @NotNull(message = "This field is required")
    @ValidUUID(message = "This field must be a valid UUID")
    private String companyRuleId;

    @NotNull(message = "This field is required")
    @Size(min = 1 , max = 100, message = "This field must be between 1 and 100 characters")
    private String username;

    @NotNull(message = "This field is required")
    @Size(min = DefaultValuesConstants.MIN_PASSWORD_LENGTH, max = DefaultValuesConstants.MIN_PASSWORD_LENGTH, message = ("This field must be between " + DefaultValuesConstants.MIN_PASSWORD_LENGTH + " and " + DefaultValuesConstants.MAX_PASSWORD_LENGTH + " characters"))
    private String password;

    private Boolean rememberMe;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LoginDTO other = (LoginDTO) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }
}
