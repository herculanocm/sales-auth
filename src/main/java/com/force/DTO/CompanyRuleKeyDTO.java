package com.force.DTO;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.force.postgres.model.CompanyRule;
import com.force.postgres.model.CompanyRuleKey;
import com.force.util.ValidUUID;

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
public class CompanyRuleKeyDTO {

    private String id;

    private LocalDateTime dtInclude;

    private String userInclude;

    private LocalDateTime dtUpdate;

    private String userUpdate;

    @NotNull(message = "This field is required")
    @NotEmpty(message = "This field is required")
    @Size(min = 1, max = 4000, message = "This field must be between 1 and 4000 characters")
    private String publicKey;

    @NotNull(message = "This field is required")
    @NotEmpty(message = "This field is required")
    @Size(min = 1, max = 4000, message = "This field must be between 1 and 4000 characters")
    private String privateKey;
    
    @NotNull(message = "This field is required")
    @NotEmpty(message = "This field is required")
    @Size(min = 1, max = 255, message = "This field must be between 1 and 255 characters")
    private String tipoKey;

    @ValidUUID(message = "This field must be a valid UUID")
    private String companyRuleId;

    public static CompanyRuleKeyDTO fromEntity(CompanyRuleKey companyRuleKey) {
        return CompanyRuleKeyDTO.builder()
            .id(Optional.ofNullable(companyRuleKey.getId()).map(UUID::toString).orElse(null))
            .dtInclude(companyRuleKey.getDtInclude())
            .userInclude(companyRuleKey.getUserInclude())
            .dtUpdate(companyRuleKey.getDtUpdate())
            .userUpdate(companyRuleKey.getUserUpdate())
            .publicKey(companyRuleKey.getPublicKey())
            .tipoKey(companyRuleKey.getTipoKey())
            .companyRuleId(Optional.ofNullable(companyRuleKey.getCompanyRule()).map(CompanyRule::getId).map(UUID::toString).orElse(null))
            .build();
    }

    public CompanyRuleKey toEntity() {
        CompanyRuleKey companyRuleKey = new CompanyRuleKey();

        if (Optional.ofNullable(id).isPresent()) {
            companyRuleKey.setId(UUID.fromString(id));
        } else {
            companyRuleKey.setId(null);
        }  
        
        if (Optional.ofNullable(companyRuleId).isPresent()) {
            companyRuleKey.setCompanyRule(new CompanyRule(UUID.fromString(companyRuleId)));
        } else {
            companyRuleKey.setCompanyRule(null);
        }
 
        companyRuleKey.setPublicKey(publicKey);
        companyRuleKey.setPrivateKey(privateKey);
        companyRuleKey.setTipoKey(tipoKey);
        
        return companyRuleKey;
    }
}
