package com.force.service;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

import io.quarkus.elytron.security.common.BcryptUtil;

import org.jboss.logging.Logger;

import com.force.DTO.RegisterUserDTO;
import com.force.postgres.model.CompanyRule;
import com.force.postgres.model.User;
import com.force.postgres.repository.UserRepository;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserService {
    
    private static final Logger logger = Logger.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final SecurityIdentity securityIdentity;

    @Inject
    public UserService(
        UserRepository userRepository,
        SecurityIdentity securityIdentity
        ) {
        this.userRepository = userRepository;
        this.securityIdentity = securityIdentity;
    }

    public Optional<User> getUserByEmail(String email) {
        logger.info("Getting user by email: " + email);
        return userRepository.findByEmailOptional(email);
    }

    public Optional<User> getUserById(UUID id) {
        logger.info("Getting user by id: " + id);
        return userRepository.findByIdOptional(id);
    }

    public List<User> getUsersByCompanyRuleId(UUID companyRuleId) {
        logger.info("Getting users by company rule id: " + companyRuleId);
        return userRepository.getUsersByCompanyRuleId(companyRuleId);
    }

    public void deactivateUser(UUID id) {
        logger.info("Deactivating user with id: " + id);
        Optional<User> user = userRepository.findByIdOptional(id);
        if (user.isPresent()) {
            user.get().setEnabled(false);

            user.get().setDtUpdate(LocalDateTime.now());
            user.get().setUserUpdate(securityIdentity.getPrincipal().getName());

            userRepository.persist(user.get());
        }
    }

    @Transactional
    public UUID registerUser(User user, Optional<String> password) {
        logger.info("Registering user: " + user);
        UUID id = UUID.randomUUID();

        user.setId(id);
        

        user.setDtInclude(LocalDateTime.now());
        user.setUserInclude(securityIdentity.getPrincipal().getName());
        user.setDtUpdate(LocalDateTime.now());
        user.setUserUpdate(securityIdentity.getPrincipal().getName());
        user.setActivated(false);
        user.setEnabled(false);
        user.setEmail(user.getEmail().toLowerCase().trim());
        user.setActivationKey(UUID.randomUUID().toString());

        if (password.isPresent()) {
            user.setPasswordHash(BcryptUtil.bcryptHash(password.get()));
        }
        
        userRepository.persist(user);
        return id;
    }

    
    public RegisterUserDTO registerUser(RegisterUserDTO user) {
        logger.info("Registering user: " + user);

        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setCompanyRule(new CompanyRule(UUID.fromString(user.getCompanyRuleId())));
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setImageUrl(user.getImageUrl());

        UUID id = registerUser(newUser, Optional.empty());

        user.setId(id);
        return user;
    }

}
