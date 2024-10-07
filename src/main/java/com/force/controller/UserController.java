package com.force.controller;

import org.jboss.logging.Logger;

import com.force.DTO.CompanyRuleDTO;
import com.force.DTO.RegisterUserDTO;
import com.force.DTO.ResponseError;
import com.force.postgres.model.CompanyRule;
import com.force.postgres.model.User;
import com.force.security.PermissionsAllowed;
import com.force.service.CompanyRuleService;
import com.force.service.UserService;
import com.force.util.PagedResult;
import com.force.util.ValidUUID;

import jakarta.validation.Validator;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.Set;
import java.util.UUID;
import java.util.Optional;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class);
    private final UserService userService;
    private final Validator validator;
    private final CompanyRuleService companyRuleService;

    @Inject
    public UserController(
        UserService userService, 
        Validator validator,
        CompanyRuleService companyRuleService
        ) {
        this.userService = userService;
        this.validator = validator;
        this.companyRuleService = companyRuleService;
    }
    
    @GET
    @Path("/users/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    //@PermissionsAllowed(roles = {"ROLE_ADMIN_SYSTEM", "ROLE_ADMIN_COMPANY"})
    public Response getUserById(
    @PathParam("id") @ValidUUID(message = "This field must be a valid UUID") String id
    ) {
        logger.info("Getting user by id: " + id);
        Optional<User> user = userService.getUserById(UUID.fromString(id));
        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(user.get()).build();
    }

    @POST
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    //@PermissionsAllowed(roles = {"ROLE_ADMIN_SYSTEM", "ROLE_ADMIN_COMPANY"})
    public Response registerUser(@NotNull @Valid RegisterUserDTO user) {
        logger.info("Registering user: " + user);

        Set<ConstraintViolation<RegisterUserDTO>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            return ResponseError.createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        if (userService.getUserByEmail(user.getEmail()).isPresent()) {
            return Response
            .status(Response.Status.CONFLICT)
            .entity("User with this email already exists")
            .type(MediaType.TEXT_PLAIN)
            .build();
        }

        if (!companyRuleService.existsCompanyRule(UUID.fromString(user.getCompanyRuleId()))) {
            return Response
            .status(Response.Status.NOT_FOUND)
            .entity("CompanyRule not found")
            .type(MediaType.TEXT_PLAIN)
            .build();
        }

        RegisterUserDTO userRegistered = userService.registerUser(user);
        return Response.ok(userRegistered).build();
    }
}
