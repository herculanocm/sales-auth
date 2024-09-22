package com.force.controller;


import java.util.Set;

import org.jboss.logging.Logger;

import com.force.DTO.GroupUserDTO;
import com.force.DTO.ResponseError;
import com.force.postgres.model.GroupUser;
import com.force.service.GroupUserService;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1")
public class GroupUserController {

    private static final Logger logger = Logger.getLogger(GroupUserController.class);

    private GroupUserService groupUserService;
    private Validator validator;

    @Inject
    public GroupUserController(GroupUserService groupUserService, Validator validator) {
        this.groupUserService = groupUserService;
        this.validator = validator;
    }

    @GET
    @Path("/group-users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllGroupUsers() {
        logger.info("Getting all group users");
        return Response.ok(groupUserService.getAllGroupUsers()).build();
    }

    @POST
    @Path("/group-users")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveGroupUser(@NotNull @Valid GroupUserDTO groupUserDTO) {
        logger.info("Saving group user: " + groupUserDTO);

        Set<ConstraintViolation<GroupUserDTO>> violations = validator.validate(groupUserDTO);
        if (!violations.isEmpty()) {
            return ResponseError.createFromValidation(violations).withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        GroupUser returnEntity = groupUserService.savGroupUser(groupUserDTO.toEntity());
        return Response.status(Response.Status.CREATED).entity(returnEntity).build();
    }
    
}
