package com.force.DTO;

import java.util.UUID;

import com.force.util.ValidUUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
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
public class RegisterUserDTO {

    private UUID id;

    @NotNull(message = "This field is required")
    @NotEmpty(message = "This field must not be empty")
    @ValidUUID(message = "This field must be a valid UUID")
    private String companyRuleId;

    @NotNull(message = "This field is required")
    @NotEmpty(message = "This field must not be empty")
    @Email(message = "This field must be a valid email")
    private String email;

    @NotNull(message = "This field is required")
    @NotEmpty(message = "This field must not be empty")
    @Size(max = 255, message = "This field must be less than 100 characters")
    private String firstName;

    @Size(max = 255, message = "This field must be less than 200 characters")
    private String lastName;

    @Size(max = 255, message = "This field must be less than 255 characters")
    private String imageUrl;
}
