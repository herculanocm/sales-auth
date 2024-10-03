package com.force.util.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


public class CustomForbiddenException extends WebApplicationException {
    
    private String errorCode;
    private String errorMessage;

    public CustomForbiddenException(String errorMessage) {
        super(Response.status(Response.Status.FORBIDDEN)
                .entity(errorMessage)
                .type(MediaType.TEXT_PLAIN)
                .build());
        this.errorMessage = errorMessage;
    }

    public CustomForbiddenException(String errorCode, String errorMessage) {
        super(Response.status(Response.Status.FORBIDDEN)
                .entity(errorMessage)
                .type(MediaType.TEXT_PLAIN)
                .build());
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

      // Getters for additional fields
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }

}
