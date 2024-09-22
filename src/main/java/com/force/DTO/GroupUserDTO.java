package com.force.DTO;

import java.util.UUID;
import java.util.Optional;
import com.force.postgres.model.GroupUser;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupUserDTO {

    private UUID id;

    @NotNull(message = "This field is required")
    @Size(max = 255, message = "This field must be less than 255 characters")
    private String name;

    @Size(max = 3000, message = "This field must be less than 3000 characters")
    private String description;

    private Boolean enabled;

    public GroupUser toEntity() {
        GroupUser groupUser = new GroupUser();
        
        if (Optional.ofNullable(this.id).isPresent()) {
            groupUser.setId(this.id);
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