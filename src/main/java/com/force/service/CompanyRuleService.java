package com.force.service;

import org.jboss.logging.Logger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import com.force.postgres.model.CompanyRule;
import com.force.postgres.model.CompanyRuleKey;
import com.force.postgres.repository.CompanyRuleKeyRepository;
import com.force.postgres.repository.CompanyRuleRepository;
import com.force.util.PagedResult;
import com.force.util.UuidUtil;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CompanyRuleService {

    private static final Logger logger = Logger.getLogger(CompanyRuleService.class);
    
    private final CompanyRuleRepository companyRuleRepository;

    private final SecurityIdentity securityIdentity;

    private final CompanyRuleKeyRepository companyRuleKeyRepository;

    @Inject
    public CompanyRuleService(
        CompanyRuleRepository companyRuleRepository,
        SecurityIdentity securityIdentity,
        CompanyRuleKeyRepository companyRuleKeyRepository
        ) {
        this.companyRuleRepository = companyRuleRepository;
        this.securityIdentity = securityIdentity;
        this.companyRuleKeyRepository = companyRuleKeyRepository;
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
            companyRule.setUserInclude(securityIdentity.getPrincipal().getName());
            companyRule.setDtUpdate(LocalDateTime.now());
            companyRule.setUserUpdate(securityIdentity.getPrincipal().getName());
            companyRuleRepository.persist(companyRule);
            return companyRule;
        
    }

    @Transactional
    public CompanyRuleKey saveCompanyRuleKey(CompanyRuleKey companyRuleKey) {
        logger.info("Saving company rule key: " + companyRuleKey);

        companyRuleKey.setId(UuidUtil.generateUuidV7());
        companyRuleKey.setDtInclude(LocalDateTime.now());
        companyRuleKey.setUserInclude(securityIdentity.getPrincipal().getName());
        companyRuleKey.setDtUpdate(LocalDateTime.now());
        companyRuleKey.setUserUpdate(securityIdentity.getPrincipal().getName());

        if (!Optional.ofNullable(companyRuleKey.getCompanyRule()).isPresent()) {
            // throws an exception if the company rule does not exist
            throw new IllegalArgumentException("Company rule does not exist");
        }

        companyRuleKeyRepository.persist(companyRuleKey);
        return companyRuleKey;
    }

    public Optional<CompanyRuleKey> getCompanyRuleKey(UUID id) {
        logger.info("Getting company rule key with id: " + id);
        return companyRuleKeyRepository.findByIdOptional(id);
    }

    public Optional<CompanyRuleKey> getCompanyRuleKeysByCompanyRuleId(UUID companyRuleId) {
        logger.info("Getting company rule keys by company rule id: " + companyRuleId);
        return companyRuleKeyRepository.findByCompanyRuleUUID(companyRuleId);
    }

    public Boolean existsCompanyRuleKeyByCompanyUUID(UUID companyRuleUuid) {
        logger.info("Checking if company rule key exists with company rule uuid: " + companyRuleUuid);
        return companyRuleKeyRepository.findByCompanyRuleUUID(companyRuleUuid).isPresent();
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

    public Boolean existsCompanyRuleKeyByCompanyUUIDDiff(UUID companyId, UUID id) {
        logger.info("Checking if company rule key exists with company rule uuid: " + companyId);
        return companyRuleKeyRepository.existsCompanyRuleKeyByCompanyUUIDDiff(companyId, id).isPresent();
    }

    @Transactional
    public Optional<CompanyRuleKey> updateCompanyRuleKey(CompanyRuleKey companyRuleKey) {
        Optional<CompanyRuleKey> existingCompanyRuleKey = companyRuleKeyRepository.findByIdOptional(companyRuleKey.getId());

        if (!Optional.ofNullable(companyRuleKey.getCompanyRule()).isPresent()) {
            return Optional.empty();
        }

        if (existingCompanyRuleKey.isPresent()) {
            CompanyRuleKey updatedCompanyRulekey = existingCompanyRuleKey.get();

            updatedCompanyRulekey.setPublicKey(companyRuleKey.getPublicKey());
            updatedCompanyRulekey.setPrivateKey(companyRuleKey.getPrivateKey());
            updatedCompanyRulekey.setTipoKey(companyRuleKey.getTipoKey());

            updatedCompanyRulekey.setCompanyRule(companyRuleKey.getCompanyRule());


            updatedCompanyRulekey.setDtUpdate(LocalDateTime.now());
            updatedCompanyRulekey.setUserUpdate(securityIdentity.getPrincipal().getName());
            companyRuleKeyRepository.persist(updatedCompanyRulekey);
            return Optional.of(updatedCompanyRulekey);
        }

        return Optional.empty();
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
            updatedCompanyRule.setUserUpdate(securityIdentity.getPrincipal().getName());
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

    @Transactional
    public void deleteCompanyRuleKey(UUID id) {
        logger.info("Deleting company rule key with id: " + id);
        companyRuleKeyRepository.deleteById(id);
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
