package com.force.controller;


import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import org.jboss.logging.Logger;

import com.force.DTO.GroupUserDTO;
import com.force.DTO.ResponseError;
import com.force.postgres.model.GroupUser;
import com.force.service.GroupUserService;
import com.force.util.PagedResult;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1")
public class GroupUserController {

    private static final Logger logger = Logger.getLogger(GroupUserController.class);

    private final GroupUserService groupUserService;
    private Validator validator;

    @Inject
    public GroupUserController(GroupUserService groupUserService, Validator validator) {
        this.groupUserService = groupUserService;
        this.validator = validator;
    }

    @GET
    @Path("/group-users/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupUserByQueryParams(
        @QueryParam("page") @DefaultValue("" + DefaultValuesConstants.DEFAULT_PAGE) int page,
        @QueryParam("size") @DefaultValue("" + DefaultValuesConstants.DEFAULT_SIZE) int size,
        @QueryParam("id") String id, 
        @QueryParam("companyRuleId") String companyRuleId, 
        @QueryParam("name") String name, 
        @QueryParam("enabled") Boolean enabled
        ) {
        logger.info("Getting group user by query params: id=" + id + ", companyRuleId=" + companyRuleId + ", name=" + name + ", enabled=" + enabled + ", page=" + page + ", size=" + size);
        PagedResult<GroupUser> pagedResult = groupUserService.getGroupUserByQueryParams(page, size, UUID.fromString(id), UUID.fromString(companyRuleId), name, enabled);

        if (pagedResult.getData().isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(pagedResult).build();
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

    @PUT
    @Path("/group-users")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateGroupUser(@NotNull @Valid GroupUserDTO groupUserDTO) {
        logger.info("Updating group user: " + groupUserDTO);

        Set<ConstraintViolation<GroupUserDTO>> violations = validator.validate(groupUserDTO);
        if (!violations.isEmpty()) {
            return ResponseError.createFromValidation(violations).withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        } else if (Optional.ofNullable(groupUserDTO.getId()).isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseError("Id is required", null)).build();
        }   

        GroupUser groupUser = groupUserService.getGroupUserById(groupUserDTO.getId()).orElse(null);

        if (Optional.ofNullable(groupUser).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseError("Group user not found", null)).build();
        }

        GroupUser returnEntity = groupUserService.savGroupUser(groupUserDTO.toEntity());
        return Response.ok(returnEntity).build();
    }

    @DELETE
    @Path("/group-users/{id}")
    public Response deleteGroupUser(@PathParam("id") String id) {
        logger.info("Deleting group user with id: " + id);

        if (Optional.ofNullable(id).isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseError("Id is required", null)).build();
        }

        GroupUser groupUser = groupUserService.getGroupUserById(UUID.fromString(id)).orElse(null);

        if (Optional.ofNullable(groupUser).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseError("Group user not found", null)).build();
        }

        try {
            groupUserService.deleteGroupUser(UUID.fromString(id));

            return Response.noContent().build();
        } catch (Throwable t) {
            logger.error(t.getMessage());
            if (t.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                return Response.status(Response.Status.CONFLICT).entity(new ResponseError("Group user is in use", null)).build();
            }

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseError("Error deleting group user", null)).build();
        }
    }
    
}
