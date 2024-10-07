package com.force.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.jboss.logging.Logger;

import com.force.DTO.GroupUserDTO;
import com.force.DTO.mapper.GroupUserMapper;
import com.force.postgres.model.GroupUser;
import com.force.postgres.repository.GroupUserRepository;
import com.force.util.PagedResult;
import com.force.util.UuidUtil;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class GroupUserService {
    private static final Logger logger = Logger.getLogger(GroupUserService.class);
    
    private final GroupUserRepository groupUserReporitory;
    private final SecurityIdentity securityIdentity;

    @Inject
    public GroupUserService(GroupUserRepository groupUserReporitory, SecurityIdentity securityIdentity) {
        this.groupUserReporitory = groupUserReporitory;
        this.securityIdentity = securityIdentity;
    }

    public List<GroupUser> getAllGroupUsers() {
        logger.info("Getting all group users");




        logger.info("User: " + securityIdentity.getPrincipal().getName());
        return groupUserReporitory.listAll();
    }

    public Boolean existsGroupUser(UUID id) {
        logger.info("Checking if group user exists with id: " + id);
        return groupUserReporitory.findByIdOptional(id).isPresent();
    }

    @Transactional
    public GroupUser saveGroupUser(GroupUser groupUser) {
        logger.info("Saving group user: " + groupUser);
        if (Optional.ofNullable(groupUser.getId()).isEmpty()) {
            groupUser.setId(UuidUtil.generateUuidV7());
        }

        groupUser.setDtInclude(LocalDateTime.now());
        groupUser.setUserInclude(securityIdentity.getPrincipal().getName());
        groupUser.setDtUpdate(LocalDateTime.now());
        groupUser.setUserUpdate(securityIdentity.getPrincipal().getName());

        groupUserReporitory.persist(groupUser);
        return groupUser;
    }

    public GroupUserDTO saveGroupUserDTO(GroupUserDTO groupUserDTO) {
        logger.info("Saving group user DTO: " + groupUserDTO);
        GroupUser groupUser = GroupUserMapper.toEntity(groupUserDTO);
        groupUser = saveGroupUser(groupUser);
        return GroupUserMapper.toDTO(getGroupUserById(groupUser.getId()).get());
    }

    public Optional<GroupUserDTO> optUpdateGroupUserDTO(GroupUserDTO groupUserDTO) {
        logger.info("Update group user DTO: " + groupUserDTO);
        GroupUser groupUser = GroupUserMapper.toEntity(groupUserDTO);
        Optional<GroupUser> updatedGroupUser = optUpdateGroupUser(groupUser);
        if (updatedGroupUser.isPresent()) {
            return Optional.of(GroupUserMapper.toDTO(getGroupUserById(updatedGroupUser.get().getId()).get()));
        }

        return Optional.empty();
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
            existingGroupUser.get().setUserUpdate(securityIdentity.getPrincipal().getName());

            existingGroupUser.get().setName(groupUser.getName());
            existingGroupUser.get().setDescription(groupUser.getDescription());
            existingGroupUser.get().setEnabled(groupUser.getEnabled());
            existingGroupUser.get().setCompanyRule(groupUser.getCompanyRule());



    
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

    public PagedResult<GroupUserDTO> getGroupUserByQueryParams(int page,int  size, Optional<String> id, Optional<String> companyRuleId, Optional<String> name, Optional<Boolean> enabled) {
        logger.info("Getting group user by query params: id=" + id + ", name=" + name + ", enabled=" + enabled + ", page=" + page + ", size=" + size);
        logger.info("User: " + securityIdentity.getPrincipal().getName());
        logger.info("User: " + securityIdentity.getPrincipal());
     

       
        if (securityIdentity == null) {
            logger.error("SecurityIdentity is null");
        } else {
            logger.info("SecurityIdentity is not null");
        }

        if (securityIdentity.getPrincipal() == null) {
            logger.error("Principal is null");
        } else {
            logger.info("Principal is not null");
        }

        if (securityIdentity.getPrincipal().getName() == null || securityIdentity.getPrincipal().getName().isEmpty()) {
            logger.error("Principal name is null or empty");
        } else {
            logger.info("Principal name: " + securityIdentity.getPrincipal().getName());
        }
        
        PanacheQuery<GroupUser> query = groupUserReporitory.findByQueryParams(id, companyRuleId, name, enabled);
        query.page(Page.of(page, size));

        List<GroupUser> data = query.list();
        long totalElements = query.count();
        int totalPages = query.pageCount();

        
        return new PagedResult<>(GroupUserMapper.toDTO(data), page, size, totalElements, totalPages);

    }
}
