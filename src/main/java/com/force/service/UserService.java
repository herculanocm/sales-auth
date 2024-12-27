package com.force.service;

import java.util.UUID;
import java.util.Optional;
import java.util.Random;
import java.util.List;
import java.time.LocalDateTime;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import org.jboss.logging.Logger;

import com.force.DTO.RegisterUserDTO;
import com.force.postgres.model.CompanyRule;
import com.force.postgres.model.User;
import com.force.postgres.repository.UserRepository;
import com.force.util.PagedResult;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.quarkus.panache.common.Page;

@ApplicationScoped
public class UserService {
    
    private static final Logger logger = Logger.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final SecurityIdentity securityIdentity;
    private final EmailService emailService;

    @Inject
    public UserService(
        UserRepository userRepository,
        SecurityIdentity securityIdentity,
        EmailService emailService
        ) {
        this.userRepository = userRepository;
        this.securityIdentity = securityIdentity;
        this.emailService = emailService;
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
        user.setActivationKey(generateRandomCode());

        if (password.isPresent()) {
            user.setPasswordHash(BcryptUtil.bcryptHash(password.get()));
        }
        
        userRepository.persist(user);
        return id;
    }

    public static String generateRandomCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        // Generate 8 random digits
        for (int i = 0; i < 6; i++) {
            int digit = random.nextInt(10); // Generates a random digit between 0 and 5
            code.append(digit);
        }

        return code.toString();
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


        // sending email 
        String subject = "Registro no sistema Sales - " + user.getFirstName();
        String htmlContent = """
        <h1>Bem vindo ao sistema</h1>
        <p>Você foi registrado com sucesso como $2</p>
        <p>Utilize o código <strong>$1</strong> para registrar/alterar sua senha</p>
        """;

        htmlContent = htmlContent.replace("$1", newUser.getActivationKey());
        htmlContent = htmlContent.replace("$2", newUser.getEmail());

        try {
            emailService.sendEmailSMTP(user.getEmail(), subject, htmlContent);
        } catch (Exception e) {
            logger.error("Error sending email", e);
        }


        return user;
    }

    public PagedResult<User> getUserByQueryParams(int page, int size, Optional<String> companyRuleIdOptional,
            Optional<String> emailOptional, Optional<String> firstNameOptional, Optional<String> idOptional, Optional<Boolean> enabledOptional, Optional<Boolean> activatedOptional) {
        logger.info("Searching users with params: page=" + page + ", size=" + size + ", id=" + idOptional + ", companyRuleId=" + companyRuleIdOptional + ", email=" + emailOptional + ", firstName=" + firstNameOptional + ", enabled=" + enabledOptional + ", activated=" + activatedOptional);
        PanacheQuery<User> query = userRepository.findByQueryParams(idOptional, companyRuleIdOptional, emailOptional, firstNameOptional, enabledOptional, activatedOptional);
        query.page(Page.of(page, size));

        List<User> data = query.list();
        long totalElements = query.count();
        int totalPages = query.pageCount();

        
        return new PagedResult<>((data), page, size, totalElements, totalPages);
    }

}
