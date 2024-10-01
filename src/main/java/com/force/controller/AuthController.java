package com.force.controller;

import java.util.Set;

import org.jboss.logging.Logger;

import com.force.DTO.JWTTokenReturnDTO;
import com.force.DTO.LoginDTO;
import com.force.DTO.ResponseError;
import com.force.security.AuthService;
import com.force.security.jwt.JWTProvider;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@PermitAll
@Path("/api/v1")
public class AuthController {
    private static final Logger logger = Logger.getLogger(AuthController.class);
    private final Validator validator;
    private final AuthService authService;
    private final JWTProvider jwtProvider;

    @Inject
    public AuthController(Validator validator, AuthService authService, JWTProvider jwtProvider) {
        this.validator = validator;
        this.authService = authService;
        this.jwtProvider = jwtProvider;
    }


    @POST
    @Path("/authenticate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authorize(@Valid @NotNull LoginDTO loginDTO) {
        logger.info("Authorizing LoginDTO: " + loginDTO);

        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        if (!violations.isEmpty()) {
            return ResponseError.createFromValidation(violations).withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        if (!authService.authenticate(loginDTO.getUsername(), loginDTO.getPassword())) {
            return Response.status(Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }

        boolean rememberMe = (loginDTO.getRememberMe() == null) ? false : loginDTO.getRememberMe();
        JWTTokenReturnDTO jwtTokenReturnDTO = jwtProvider.generateTokenByUsernameAndCompanyId(loginDTO.getUsername(), loginDTO.getCompanyId(), rememberMe);
        logger.debug("JWT Token Return DTO: " + jwtTokenReturnDTO);

        return Response.ok(jwtTokenReturnDTO).build();
    }

}
