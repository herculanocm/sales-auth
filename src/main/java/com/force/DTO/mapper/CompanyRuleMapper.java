package com.force.DTO.mapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.force.DTO.CompanyRuleDTO;
import com.force.postgres.model.CompanyRule;

public class CompanyRuleMapper {
    public static CompanyRuleDTO toDTO(CompanyRule companyRule) {
        if (Optional.ofNullable(companyRule).isEmpty()) {
            return null;
        }
 
        CompanyRuleDTO companyRuleDTO = new CompanyRuleDTO();
        companyRuleDTO.setId(companyRule.getId());
        companyRuleDTO.setName(companyRule.getName());
        companyRuleDTO.setCgc(companyRule.getCgc());
        companyRuleDTO.setEnabled(companyRule.getEnabled());
        companyRuleDTO.setDtInclude(companyRule.getDtInclude());
        companyRuleDTO.setUserInclude(companyRule.getUserInclude());
        companyRuleDTO.setDtUpdate(companyRule.getDtUpdate());
        companyRuleDTO.setUserUpdate(companyRule.getUserUpdate());
        return companyRuleDTO;
    }

    public static CompanyRule toEntity(CompanyRuleDTO companyRuleDTO) {
        if (Optional.ofNullable(companyRuleDTO).isEmpty()) {
            return null;
        }

        CompanyRule companyRule = new CompanyRule();
        companyRule.setId(companyRuleDTO.getId());
        companyRule.setName(companyRuleDTO.getName().trim().toUpperCase());
        companyRule.setCgc(companyRuleDTO.getCgc());
        companyRule.setEnabled(Optional.ofNullable(companyRuleDTO.getEnabled()).orElse(Boolean.FALSE));
        companyRule.setDtInclude(companyRuleDTO.getDtInclude());
        companyRule.setUserInclude(companyRuleDTO.getUserInclude());
        companyRule.setDtUpdate(companyRuleDTO.getDtUpdate());
        companyRule.setUserUpdate(companyRuleDTO.getUserUpdate());
        return companyRule;
    }

    public static List<CompanyRuleDTO> toDTO(List<CompanyRule> companyRules) {
        return companyRules.stream().filter(Objects::nonNull).map(CompanyRuleMapper::toDTO).toList();
    }

    public static List<CompanyRule> toEntity(List<CompanyRuleDTO> companyRuleDTOs) {
        return companyRuleDTOs.stream().filter(Objects::nonNull).map(CompanyRuleMapper::toEntity).toList();
    }
}
