package com.force.DTO;

import java.util.UUID;
import java.util.Optional;
import java.time.LocalDateTime;

import com.force.postgres.model.CompanyRule;
import com.force.postgres.model.GroupUser;
import com.force.util.ValidUUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupUserDTO {

    private UUID id;

    @NotNull(message = "This field is required")
    @NotEmpty(message = "This field is required")
    @Size(max = 255, message = "This field must be less than 255 characters")
    private String name;

    @Size(max = 3000, message = "This field must be less than 3000 characters")
    private String description;

    private Boolean enabled;

    @NotNull(message = "This field is required")
    @ValidUUID(message = "This field must be a valid UUID")
    private String companyRuleId;

	private LocalDateTime dtInclude;

	private String userInclude;

	private LocalDateTime dtUpdate;

	private String userUpdate;

    private CompanyRuleDTO companyRule;

    public GroupUserDTO(GroupUser groupUser) {
        this.id = groupUser.getId();
        this.name = groupUser.getName();
        this.description = groupUser.getDescription();
        this.enabled = groupUser.getEnabled();
        if (Optional.ofNullable(groupUser.getCompanyRule()).isPresent()) {
            this.companyRuleId = groupUser.getCompanyRule().getId().toString();
            this.companyRule = new CompanyRuleDTO(groupUser.getCompanyRule());
        }
        this.dtInclude = groupUser.getDtInclude();
        this.userInclude = groupUser.getUserInclude();
        this.dtUpdate = groupUser.getDtUpdate();
        this.userUpdate = groupUser.getUserUpdate();
    }


    public GroupUser toEntity() {
        GroupUser groupUser = new GroupUser();
        
        if (Optional.ofNullable(this.id).isPresent()) {
            groupUser.setId(this.id);
        }

        if (Optional.ofNullable(this.companyRuleId).isPresent()) {
            groupUser.setCompanyRule(new CompanyRule(UUID.fromString(this.companyRuleId)));
        }

        groupUser.setName(this.name.trim().toUpperCase());
        groupUser.setDescription(this.description);

        if (Optional.ofNullable(this.enabled).isPresent()) {
            groupUser.setEnabled(this.enabled);
        } else {
            groupUser.setEnabled(Boolean.FALSE);
        }

        return groupUser;
    }
}