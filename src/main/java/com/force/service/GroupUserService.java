package com.force.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.jboss.logging.Logger;

import com.aayushatharva.brotli4j.common.annotations.Local;
import com.force.postgres.model.CompanyRule;
import com.force.postgres.model.GroupUser;
import com.force.postgres.repository.GroupUserRepository;
import com.force.util.PagedResult;
import com.force.util.UuidUtil;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class GroupUserService {
    private static final Logger logger = Logger.getLogger(GroupUserService.class);
    
    private final GroupUserRepository groupUserReporitory;

    @Inject
    public GroupUserService(GroupUserRepository groupUserReporitory) {
        this.groupUserReporitory = groupUserReporitory;
    }

    public List<GroupUser> getAllGroupUsers() {
        logger.info("Getting all group users");
        return groupUserReporitory.listAll();
    }

    @Transactional
    public GroupUser saveGroupUser(GroupUser groupUser) {
        logger.info("Saving group user: " + groupUser);
        if (Optional.ofNullable(groupUser.getId()).isEmpty()) {
            groupUser.setId(UuidUtil.generateUuidV7());
        }

        groupUser.setDtInclude(LocalDateTime.now());
        groupUser.setUserInclude("user");
        groupUser.setDtUpdate(LocalDateTime.now());
        groupUser.setUserUpdate("user");

        groupUserReporitory.persist(groupUser);
        return groupUser;
    }

    @Transactional
    public GroupUser updateGroupUser(GroupUser groupUser) {
        logger.info("Update group user: " + groupUser);
            groupUser.setDtUpdate(LocalDateTime.now());
            groupUser.setUserUpdate("user");
            groupUserReporitory.persist(groupUser);
            return groupUser;
    }

    @Transactional
    public Optional<GroupUser> optUpdateGroupUser(GroupUser groupUser) {
        logger.info("Update group user: " + groupUser);
        
        Optional<GroupUser> existingGroupUser = groupUserReporitory.findByIdOptional(groupUser.getId());
        if (existingGroupUser.isPresent()) {
            existingGroupUser.get().setDtUpdate(LocalDateTime.now());
            existingGroupUser.get().setUserUpdate("user");
    
            groupUserReporitory.persist(existingGroupUser.get());
            return Optional.of(groupUser);
        }

       return Optional.empty();
    }

    @Transactional
    public void deleteGroupUser(UUID id) {
        logger.info("Deleting group user with id: " + id);
        groupUserReporitory.deleteById(id);
    }

    public Optional<GroupUser> getGroupUserById(UUID id) {
        logger.info("Getting group user with id: " + id);
        return groupUserReporitory.findByIdOptional(id);
    }

    public PagedResult<GroupUser> getGroupUserByQueryParams(int page,int  size, UUID id, UUID companyRuleId, String name, Boolean enabled) {
        logger.info("Getting group user by query params: id=" + id + ", name=" + name + ", enabled=" + enabled + ", page=" + page + ", size=" + size);

        PanacheQuery<GroupUser> query = groupUserReporitory.findByQueryParams(id, companyRuleId, name, enabled);
        query.page(Page.of(page, size));

        List<GroupUser> data = query.list();
        long totalElements = query.count();
        int totalPages = query.pageCount();

        
        return new PagedResult<>(data, page, size, totalElements, totalPages);

    }
}
