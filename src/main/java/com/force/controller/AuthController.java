package com.force.controller;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.Path;

@Path("/api/v1")
public class AuthController {
    private static final Logger logger = Logger.getLogger(AuthController.class);
    private final Validator validator;

    @Inject
    public AuthController(Validator validator) {
        this.validator = validator;
    }

}
