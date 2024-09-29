package com.force.DTO;

import com.force.postgres.model.CompanyRule;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRuleDTO {
    
    private UUID id;

    @NotNull(message = "This field is required")
    @NotEmpty(message = "This field is required")
    @Size(max = 255, message = "This field must be less than 255 characters")
    private String name;

    @NotNull(message = "This field is required")
    @Size(min = 11 , max = 20, message = "This field must be between 11 and 20 characters")
    private String cgc;

    private Boolean enabled;

    private LocalDateTime dtInclude;

    private String userInclude;

    private LocalDateTime dtUpdate;

    private String userUpdate;

    public CompanyRuleDTO(CompanyRule companyRule) {
        this.id = companyRule.getId();
        this.name = companyRule.getName();
        this.cgc = companyRule.getCgc();
        this.enabled = companyRule.getEnabled();
        this.dtInclude = companyRule.getDtInclude();
        this.userInclude = companyRule.getUserInclude();
        this.dtUpdate = companyRule.getDtUpdate();
        this.userUpdate = companyRule.getUserUpdate();
    }

    public CompanyRule toEntity() {
        CompanyRule companyRule = new CompanyRule();
        
        if (Optional.ofNullable(this.id).isPresent()) {
            companyRule.setId(this.id);
        }

        companyRule.setName(this.name.trim().toUpperCase());
        companyRule.setCgc(this.cgc);

        if (Optional.ofNullable(this.enabled).isPresent()) {
            companyRule.setEnabled(this.enabled);
        } else {
            companyRule.setEnabled(Boolean.FALSE);
        }

        return companyRule;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompanyRuleDTO other = (CompanyRuleDTO) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (cgc == null) {
            if (other.cgc != null)
                return false;
        } else if (!cgc.equals(other.cgc))
            return false;
        if (enabled == null) {
            if (other.enabled != null)
                return false;
        } else if (!enabled.equals(other.enabled))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((cgc == null) ? 0 : cgc.hashCode());
        result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
        return result;
    }

    
}

