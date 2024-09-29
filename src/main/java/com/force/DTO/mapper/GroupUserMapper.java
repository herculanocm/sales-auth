package com.force.DTO.mapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.force.DTO.GroupUserDTO;
import com.force.postgres.model.CompanyRule;
import com.force.postgres.model.GroupUser;

public class GroupUserMapper {
    public static GroupUserDTO toDTO(GroupUser groupUser) {
        if (Optional.ofNullable(groupUser).isEmpty()) {
            return null;
        }

        GroupUserDTO groupUserDTO = new GroupUserDTO();
        groupUserDTO.setId(groupUser.getId());
        groupUserDTO.setName(groupUser.getName());
        groupUserDTO.setDescription(groupUser.getDescription());
        groupUserDTO.setEnabled(groupUser.getEnabled());
        if (Optional.ofNullable(groupUser.getCompanyRule()).isPresent()) {
            groupUserDTO.setCompanyRuleId(groupUser.getCompanyRule().getId().toString());
            groupUserDTO.setCompanyRule(CompanyRuleMapper.toDTO(groupUser.getCompanyRule()));
        }
        groupUserDTO.setDtInclude(groupUser.getDtInclude());
        groupUserDTO.setUserInclude(groupUser.getUserInclude());
        groupUserDTO.setDtUpdate(groupUser.getDtUpdate());
        groupUserDTO.setUserUpdate(groupUser.getUserUpdate());
        return groupUserDTO;
    }

    public static GroupUser toEntity(GroupUserDTO groupUserDTO) {
        if (Optional.ofNullable(groupUserDTO).isEmpty()) {
            return null;
        }

        GroupUser groupUser = new GroupUser();
        groupUser.setId(groupUserDTO.getId());
        if (Optional.ofNullable(groupUserDTO.getCompanyRuleId()).isPresent()) {
            groupUser.setCompanyRule(new CompanyRule(UUID.fromString(groupUserDTO.getCompanyRuleId())));
        }
        groupUser.setName(groupUserDTO.getName().trim().toUpperCase());
        groupUser.setDescription(groupUserDTO.getDescription());
        groupUser.setEnabled(Optional.ofNullable(groupUserDTO.getEnabled()).orElse(Boolean.FALSE));
        groupUser.setDtInclude(groupUserDTO.getDtInclude());
        groupUser.setUserInclude(groupUserDTO.getUserInclude());
        groupUser.setDtUpdate(groupUserDTO.getDtUpdate());
        groupUser.setUserUpdate(groupUserDTO.getUserUpdate());
        return groupUser;
    }

    public static List<GroupUserDTO> toDTO(List<GroupUser> groupUsers) {
        return groupUsers.stream().filter(Objects::nonNull).map(GroupUserMapper::toDTO).toList();
    }

    public static List<GroupUser> toEntity(List<GroupUserDTO> groupUserDTOs) {
        return groupUserDTOs.stream().filter(Objects::nonNull).map(GroupUserMapper::toEntity).toList();
    }
}
