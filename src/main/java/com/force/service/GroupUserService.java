package com.force.service;

import java.util.List;
import java.util.Optional;

import com.force.postgres.model.GroupUser;
import com.force.postgres.repository.GroupUserReporitory;
import com.force.util.UuidUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class GroupUserService {
    
    GroupUserReporitory groupUserReporitory;

    @Inject
    public GroupUserService(GroupUserReporitory groupUserReporitory) {
        this.groupUserReporitory = groupUserReporitory;
    }

    public List<GroupUser> getAllGroupUsers() {
        return groupUserReporitory.listAll();
    }

    @Transactional
    public GroupUser savGroupUser(GroupUser groupUser) {

        if (Optional.ofNullable(groupUser.getId()).isEmpty()) {
            groupUser.setId(UuidUtil.generateUuidV7());
        }
        groupUserReporitory.persist(groupUser);
        return groupUser;
    }
}
