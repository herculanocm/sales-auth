package com.force.controller;


import java.util.Set;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jboss.logging.Logger;

import com.force.DTO.GroupUserDTO;
import com.force.DTO.ResponseError;
import com.force.DTO.mapper.GroupUserMapper;
import com.force.postgres.model.GroupUser;
import com.force.service.CompanyRuleService;
import com.force.service.GroupUserService;
import com.force.util.PagedResult;
import com.force.util.ValidUUID;

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

    private static final String ENTITY_NAME = "GroupUser";
    private static final String COMPANY_NAME = "Company";

    private final GroupUserService groupUserService;
    private final CompanyRuleService companyRuleService;
    private Validator validator;

    @Inject
    public GroupUserController(GroupUserService groupUserService, Validator validator, CompanyRuleService companyRuleService) {
        this.groupUserService = groupUserService;
        this.validator = validator;
        this.companyRuleService = companyRuleService;
    }

    @GET
    @Path("/group-users/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupUserByQueryParams(
        @QueryParam("page") @DefaultValue("" + DefaultValuesConstants.DEFAULT_PAGE) int page,
        @QueryParam("size") @DefaultValue("" + DefaultValuesConstants.DEFAULT_SIZE) int size,
        @QueryParam("id") Optional<String> id, 
        @QueryParam("companyRuleId") Optional<String> companyRuleId, 
        @QueryParam("name") Optional<String> name, 
        @QueryParam("enabled") Optional<Boolean> enabled
        ) {
        logger.info("Getting group user by query params: id=" + id + ", companyRuleId=" + companyRuleId + ", name=" + name + ", enabled=" + enabled + ", page=" + page + ", size=" + size);
        PagedResult<GroupUserDTO> pagedResult = groupUserService.getGroupUserByQueryParams(page, size, id, companyRuleId, name, enabled);

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
        List<GroupUser> groupUsers = groupUserService.getAllGroupUsers();
        List<GroupUserDTO> groupUserDTOs = GroupUserMapper.toDTO(groupUsers);
        return Response.ok(groupUserDTOs).build();
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

        if (!companyRuleService.existsCompanyRule(UUID.fromString(groupUserDTO.getCompanyRuleId()))) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseError(String.format("%s not found", COMPANY_NAME), null)).build();
        }

        groupUserDTO = groupUserService.saveGroupUserDTO(groupUserDTO);
        return Response.status(Response.Status.CREATED).entity(groupUserDTO).build();
    }

    @PUT
    @Path("/group-users/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateGroupUser(
        @PathParam("id") @ValidUUID(message = "This field must be a valid UUID") String id, 
        @NotNull @Valid GroupUserDTO groupUserDTO) {
        logger.info("Updating group user: " + groupUserDTO);

        Set<ConstraintViolation<GroupUserDTO>> violations = validator.validate(groupUserDTO);
        if (!violations.isEmpty()) {
            return ResponseError.createFromValidation(violations).withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        } 

        if (!groupUserService.existsGroupUser(UUID.fromString(id))) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseError(String.format("%s not found", ENTITY_NAME), null)).build();
        }
        
        if (!companyRuleService.existsCompanyRule(UUID.fromString(groupUserDTO.getCompanyRuleId()))) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseError(String.format("%s not found", COMPANY_NAME), null)).build();
        }

        groupUserDTO.setId(UUID.fromString(id));

        Optional<GroupUserDTO> updatedGroupUser = groupUserService.optUpdateGroupUserDTO(groupUserDTO);
        if (updatedGroupUser.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseError(String.format("%s not found", ENTITY_NAME), null)).build();
        }

        return Response.ok(updatedGroupUser.get()).build();
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
