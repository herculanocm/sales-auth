package com.force.postgres.repository;

import com.force.postgres.model.GroupUser;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GroupUserReporitory implements PanacheRepository<GroupUser> {
    
}
