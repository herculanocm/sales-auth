package com.force.service;

import org.jboss.logging.Logger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import com.force.postgres.model.CompanyRule;

import com.force.postgres.repository.CompanyRuleRepository;
import com.force.util.PagedResult;
import com.force.util.UuidUtil;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CompanyRuleService {

    private static final Logger logger = Logger.getLogger(CompanyRuleService.class);
    
    private final CompanyRuleRepository companyRuleRepository;

    @Inject
    public CompanyRuleService(CompanyRuleRepository companyRuleRepository) {
        this.companyRuleRepository = companyRuleRepository;
    }

    public List<CompanyRule> getAllCompanyRules() {
        logger.info("Getting all company rules");
        return companyRuleRepository.listAll();
    }

    @Transactional
    public CompanyRule saveCompanyRule(CompanyRule companyRule) {
        logger.info("Saving company rule: " + companyRule);
 
            companyRule.setId(UuidUtil.generateUuidV7());
            companyRule.setDtInclude(LocalDateTime.now());
            companyRule.setUserInclude("user");
            companyRule.setDtUpdate(LocalDateTime.now());
            companyRule.setUserUpdate("user");
            companyRuleRepository.persist(companyRule);
            return companyRule;
        
    }

    public Boolean existsCompanyRule(UUID id) {
        logger.info("Checking if company rule exists with id: " + id);
        return companyRuleRepository.findByIdOptional(id).isPresent();
    }

    public Boolean existsCompanyRuleByCgc(String cgc) {
        logger.info("Checking if company rule exists with cgc: " + cgc);
        return companyRuleRepository.findByCgc(cgc).isPresent();
    }

    public Boolean existsCompanyRuleByCgcDifId(String cgc, UUID id) {
        logger.info("Checking if company rule exists with cgc: " + cgc);
        return companyRuleRepository.existsCompanyRuleByCgcDifId(cgc, id).isPresent();
    }

    @Transactional
    public Optional<CompanyRule> updateCompanyRule(CompanyRule companyRule) {
        Optional<CompanyRule> existingCompanyRule = companyRuleRepository.findByIdOptional(companyRule.getId());
        if (existingCompanyRule.isPresent()) {
            CompanyRule updatedCompanyRule = existingCompanyRule.get();
            updatedCompanyRule.setName(companyRule.getName());
            updatedCompanyRule.setCgc(companyRule.getCgc());
            updatedCompanyRule.setEnabled(companyRule.getEnabled());

            updatedCompanyRule.setDtUpdate(LocalDateTime.now());
            updatedCompanyRule.setUserUpdate("user");
            companyRuleRepository.persist(updatedCompanyRule);
            return Optional.of(updatedCompanyRule);
        }

        return Optional.empty();
    }

    @Transactional
    public void deleteCompanyRule(UUID id) {
        logger.info("Deleting company rule with id: " + id);
        companyRuleRepository.deleteById(id);
    }

    public Optional<CompanyRule> getCompanyRuleById(UUID id) {
        logger.info("Getting company rule with id: " + id);
        return companyRuleRepository.findByIdOptional(id);
    }

    public PagedResult<CompanyRule> getCompanyRuleByQueryParams(int page, int size, Optional<String> id, Optional<String> name, Optional<String> cgc, Optional<Boolean> enabled) {
        logger.info("Getting company rule by query params: id=" + id + ", name=" + name + ", cgc=" + cgc + ", enabled=" + enabled + ", page=" + page + ", size=" + size);
        
        PanacheQuery<CompanyRule> query = companyRuleRepository.findByQueryParams(id, name, cgc, enabled);
        query.page(Page.of(page, size));

        List<CompanyRule> companyRules = query.list();
        long totalElements = query.count();
        int totalPages = query.pageCount();

        return new PagedResult<>(companyRules, page, size, totalElements, totalPages);
    }
}
