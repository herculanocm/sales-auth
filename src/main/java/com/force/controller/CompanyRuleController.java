package com.force.controller;

import org.jboss.logging.Logger;

import com.force.DTO.CompanyRuleDTO;
import com.force.DTO.CompanyRuleKeyDTO;
import com.force.DTO.ResponseError;
import com.force.postgres.model.CompanyRule;
import com.force.postgres.model.CompanyRuleKey;
import com.force.security.PermissionsAllowed;
import com.force.service.CompanyRuleService;
import com.force.util.PagedResult;
import com.force.util.ValidUUID;

import jakarta.validation.Validator;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.Set;
import java.util.UUID;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
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
public class CompanyRuleController {

    private static final Logger logger = Logger.getLogger(CompanyRuleController.class);
    private final CompanyRuleService companyRuleService;
    private final Validator validator;

    @Inject
    public CompanyRuleController(CompanyRuleService companyRuleService, Validator validator) {
        this.companyRuleService = companyRuleService;
        this.validator = validator;
    }

    @POST
    @Path("/company-rules")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(roles = {"ROLE_ADMIN_SYSTEM"})
    public Response saveCompanyRule(@NotNull @Valid CompanyRuleDTO companyRuleDTO) {
        logger.info("Saving CompanyRuleDTO: " + companyRuleDTO);

        Set<ConstraintViolation<CompanyRuleDTO>> violations = validator.validate(companyRuleDTO);
        if (!violations.isEmpty()) {
            return ResponseError.createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        if (companyRuleService.existsCompanyRuleByCgc(companyRuleDTO.getCgc())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseError("Company with this cgc already exists", null)).build();
        }

        CompanyRule companyRule = companyRuleDTO.toEntity();
        companyRuleService.saveCompanyRule(companyRule);

        return Response.ok(new CompanyRuleDTO(companyRule)).build();
    }

    @GET
    @Path("/company-rules")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(roles = {"ROLE_ADMIN", "ROLE_ADMIN_SYSTEM", "ROLE_ADMIN_COMPANY"})
    public Response getAllCompanyRules() {
        logger.info("Getting all company rules");
        return Response.ok(companyRuleService.getAllCompanyRules()).build();
    }

    @GET
    @Path("/company-rules/{id}")
    @PermissionsAllowed(roles = {"ROLE_ADMIN", "ROLE_ADMIN_SYSTEM", "ROLE_ADMIN_COMPANY", "ROLE_USER"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCompanyRuleById(@PathParam("id") String id) {
        logger.info("Getting company rule by id: " + id);
        return companyRuleService.getCompanyRuleById(UUID.fromString(id))
                .map(companyRule -> Response.ok(new CompanyRuleDTO(companyRule)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/company-rules/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(roles = {"ROLE_ADMIN_SYSTEM"})
    public Response updateCompanyRule(
            @PathParam("id") @ValidUUID(message = "This field must be a valid UUID") String id,
            @NotNull @Valid CompanyRuleDTO companyRuleDTO) {
        logger.info("Updating company rule with id: " + id);

        Set<ConstraintViolation<CompanyRuleDTO>> violations = validator.validate(companyRuleDTO);
        if (!violations.isEmpty()) {
            return ResponseError.createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        if (!companyRuleService.existsCompanyRule(UUID.fromString(id))) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseError("Company not found with this ID", null)).build();
        }

        if (companyRuleService.existsCompanyRuleByCgcDifId(companyRuleDTO.getCgc(), UUID.fromString(id))) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseError("Company with this cgc already exists", null)).build();
        }

        CompanyRule companyRule = companyRuleDTO.toEntity();
        companyRule.setId(UUID.fromString(id));
        Optional<CompanyRule> optCompany = companyRuleService.updateCompanyRule(companyRule);
        if (optCompany.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(new CompanyRuleDTO(optCompany.get())).build();
    }

    @DELETE
    @Path("/company-rules/{id}")
    @PermissionsAllowed(roles = {"ROLE_ADMIN_SYSTEM"})
    public Response deleteCompanyRule(@PathParam("id") String id) {
        logger.info("Deleting company rule with id: " + id);

        if (Optional.ofNullable(id).isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseError("Id is required", null))
                    .build();
        }

        CompanyRule companyRule = companyRuleService.getCompanyRuleById(UUID.fromString(id)).orElse(null);

        if (Optional.ofNullable(companyRule).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseError("Company rule not found", null))
                    .build();
        }

        try {
            companyRuleService.deleteCompanyRule(UUID.fromString(id));
            return Response.ok().build();
        } catch (Throwable t) {
            logger.error("Error deleting company rule with id: " + id, t);
            if (t.getMessage().contains("violates foreign key constraint")) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseError("Company rule is being used", null)).build();
            }

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseError("Error deleting company rule", null)).build();
        }
    }

    @GET
    @Path("/company-rules/search")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(roles = {"ROLE_USER"})
    public Response getCompanyRuleByQueryParams(@DefaultValue("0") @QueryParam("page") int page,
            @DefaultValue("10") @QueryParam("size") int size,@Valid @ValidUUID @QueryParam("id") String id,
            @QueryParam("name") Optional<String> name,
            @QueryParam("cgc") Optional<String> cgc, @QueryParam("enabled") Optional<Boolean> enabled) {
                Optional<String> sid = Optional.ofNullable(id);
                logger.info("Getting company rule by query params: id=" + id + ", name=" + name + ", cgc=" + cgc + ", enabled="
                + enabled + ", page=" + page + ", size=" + size);

        PagedResult<CompanyRule> pagedResult = companyRuleService.getCompanyRuleByQueryParams(page, size, sid, name, cgc,
                enabled);

        return Response.ok(pagedResult).build();
    }

    ///////////////////////////////////////////////////////////////////
    @DELETE
    @Path("/company-rule-keys/{id}")
    @PermissionsAllowed(roles = {"ROLE_ADMIN_SYSTEM"})
    public Response deleteCompanyRuleKey(@PathParam("id") String id) {
        logger.info("Deleting company rule key with id: " + id);

        if (Optional.ofNullable(id).isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseError("Id is required", null))
                    .build();
        }

        Optional<CompanyRuleKey> companyRuleKey = companyRuleService.getCompanyRuleKey(UUID.fromString(id));

        if (companyRuleKey.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseError("Company rule key not found", null))
                    .build();
        }

        try {
            companyRuleService.deleteCompanyRuleKey(UUID.fromString(id));
            return Response.ok().build();
        } catch (Throwable t) {
            logger.error("Error deleting company rule key with id: " + id, t);
            if (t.getMessage().contains("violates foreign key constraint")) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseError("Company rule key is being used", null)).build();
            }

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseError("Error deleting company rule key", null)).build();
        }
    }

    @POST
    @Path("/company-rule-keys")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(roles = {"ROLE_ADMIN_SYSTEM"})
    public Response saveCompanyRuleKey(@NotNull @Valid CompanyRuleKeyDTO companyRuleKeyDTO) {
        logger.info("Saving CompanyRuleKeyDTO: " + companyRuleKeyDTO);

        Set<ConstraintViolation<CompanyRuleKeyDTO>> violations = validator.validate(companyRuleKeyDTO);
        if (!violations.isEmpty()) {
            return ResponseError.createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        if (!companyRuleService.existsCompanyRule(UUID.fromString(companyRuleKeyDTO.getCompanyRuleId()))) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseError("Company does not exists", null)).build();
        }

        CompanyRuleKey companyRuleKey = companyRuleKeyDTO.toEntity();
        companyRuleService.saveCompanyRuleKey(companyRuleKey);

        return Response.ok(CompanyRuleKeyDTO.fromEntity(companyRuleKey)).build();
    }

    @PUT
    @Path("/company-rule-keys")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(roles = {"ROLE_ADMIN_SYSTEM"})
    public Response updateCompanyRuleKey(@NotNull @Valid CompanyRuleKeyDTO companyRuleKeyDTO) {
        logger.info("Saving CompanyRuleKeyDTO: " + companyRuleKeyDTO);

        Set<ConstraintViolation<CompanyRuleKeyDTO>> violations = validator.validate(companyRuleKeyDTO);
        if (!violations.isEmpty()) {
            return ResponseError.createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        if (!companyRuleService.existsCompanyRule(UUID.fromString(companyRuleKeyDTO.getCompanyRuleId()))) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseError("Company does not exists", null)).build();
        }

        CompanyRuleKey companyRuleKey = companyRuleKeyDTO.toEntity();
        companyRuleService.saveCompanyRuleKey(companyRuleKey);

        return Response.ok(CompanyRuleKeyDTO.fromEntity(companyRuleKey)).build();
    }

    @PUT
    @Path("/company-rule-keys/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PermissionsAllowed(roles = {"ROLE_ADMIN_SYSTEM"})
    public Response updateCompanyRuleKey(
            @PathParam("id") @ValidUUID(message = "This field must be a valid UUID") String id,
            @NotNull @Valid CompanyRuleKeyDTO companyRuleKeyDTO) {
        logger.info("Updating company rule key with id: " + id);

        Set<ConstraintViolation<CompanyRuleKeyDTO>> violations = validator.validate(companyRuleKeyDTO);
        if (!violations.isEmpty()) {
            return ResponseError.createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        if (!companyRuleService.existsCompanyRule(UUID.fromString(companyRuleKeyDTO.getCompanyRuleId()))) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseError("Company not found with this ID", null)).build();
        }

        if (companyRuleService.existsCompanyRuleKeyByCompanyUUIDDiff(UUID.fromString(companyRuleKeyDTO.getCompanyRuleId()), UUID.fromString(id))) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseError("Exists another row with this company", null)).build();
        }

        CompanyRuleKey companyRuleKey = companyRuleKeyDTO.toEntity();
        companyRuleKey.setId(UUID.fromString(id));

        Optional<CompanyRuleKey> optCompanykey = companyRuleService.updateCompanyRuleKey(companyRuleKey);
        if (optCompanykey.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(CompanyRuleKeyDTO.fromEntity(companyRuleKey)).build();
    }

}
