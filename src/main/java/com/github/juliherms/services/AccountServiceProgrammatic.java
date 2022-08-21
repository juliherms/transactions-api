package com.github.juliherms.services;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

/**
 * In addition to utilizing CDI for injecting and calling REST client beans for external interfaces,
 * we can use a programmatic builder API instead. This API provides more control over the various settings
 * of the REST client without needing to manipulate configuration values
 */
@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
public interface AccountServiceProgrammatic {

    @GET
    @Path("/{acctNumber}/balance")
    BigDecimal getBalance(@PathParam("acctNumber") Long accountNumber);

    @POST
    @Path("{accountNumber}/transaction")
    void transact(@PathParam("accountNumber") Long accountNumber, BigDecimal amount);

    @POST
    @Path("{accountNumber}/transaction")
    CompletionStage<Void> transactAsync(@PathParam("accountNumber") Long accountNumber, BigDecimal amount);
}
