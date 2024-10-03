package com.force.util.exception;

import com.force.DTO.ErrorResponse;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CustomForbiddenExceptionMapper implements ExceptionMapper<CustomForbiddenException> {

    @Override
    public Response toResponse(CustomForbiddenException exception) {
        // TODO Auto-generated method stub
        // Build a custom error response
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(exception.getErrorCode() != null ? exception.getErrorCode() : "FORBIDDEN");
        errorResponse.setErrorMessage(exception.getMessage());

        return Response.status(Response.Status.FORBIDDEN)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
    
}
