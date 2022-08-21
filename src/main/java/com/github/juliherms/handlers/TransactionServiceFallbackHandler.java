package com.github.juliherms.handlers;

import org.eclipse.microprofile.faulttolerance.FallbackHandler;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.jboss.logging.Logger;

import javax.ws.rs.core.Response;

public class TransactionServiceFallbackHandler  implements FallbackHandler<Response> {

    Logger LOG = Logger.getLogger(TransactionServiceFallbackHandler.class);


    @Override
    public Response handle(ExecutionContext executionContext) {

        Response response;
        String name;

        if (executionContext.getFailure().getCause() == null) {
            name = executionContext.getFailure() .getClass().getSimpleName();
        } else {
            name = executionContext.getFailure().getCause().getClass().getSimpleName();
        }

        switch (name) {
            case "BulkheadException":
                response = Response
                        .status(Response.Status.TOO_MANY_REQUESTS)
                        .build();
                break;

            case "TimeoutException":
                response = Response
                        .status(Response.Status.GATEWAY_TIMEOUT)
                        .build();
                break;

            case "CircuitBreakerOpenException":
                response = Response
                        .status(Response.Status.SERVICE_UNAVAILABLE)
                        .build();
                break;

            case "ResteasyWebApplicationException":
            case "WebApplicationException":
            case "HttpHostConnectException":
                response = Response
                        .status(Response.Status.BAD_GATEWAY)
                        .build();
                break;

            default:
                response = Response
                        .status(Response.Status.NOT_IMPLEMENTED)
                        .build();

        }

        LOG.info("******** "
                +  executionContext.getMethod().getName()
                + ": " + name
                + " ********");

        return response;
    }
}
