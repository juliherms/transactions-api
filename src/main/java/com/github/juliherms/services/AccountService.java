package com.github.juliherms.services;

import com.github.juliherms.exceptions.AccountExceptionMapper;
import com.github.juliherms.exceptions.AccountNotFoundException;
import com.github.juliherms.filters.AccountRequestFilter;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * This class responsible to access accounts API by rest client
 */
@Path("/accounts")
@RegisterRestClient
@ClientHeaderParam(name = "class-level-param", value = "AccountService-interface")
@RegisterProvider(AccountExceptionMapper.class) //register provider for check's errors
@RegisterProvider(AccountRequestFilter.class)
@RegisterClientHeaders
@Produces(MediaType.APPLICATION_JSON)
public interface AccountService {

    /**
     * Find actual balance by account number in the account api
     * @param accountNumber
     * @return
     */
    @GET
    @Path("/{acctNumber}/balance")
    BigDecimal getBalance(@PathParam("acctNumber") Long accountNumber);


    /**
     * Method responsible to send transaction to account-api
     * @param accountNumber
     * @param amount
     * @return
     * @throws AccountNotFoundException
     */
    @POST
    @Path("{accountNumber}/transaction")
    Map<String, List<String>> transact(
            @PathParam("accountNumber") Long accountNumber,
            BigDecimal amount) throws AccountNotFoundException;

    /**
     * Method responsible to send transaction to account-api - async
     * Send message into Header param
     * @param accountNumber
     * @param amount
     * @return
     */
    @POST
    @Path("{accountNumber}/transaction")
    @ClientHeaderParam(name = "method-level-param", value = "{generateValue}")
    CompletionStage<Map<String, List<String>>> transactAsync(
            @PathParam("accountNumber") Long accountNumber,
            BigDecimal amount);

    default String generateValue() {
        return "Value generated in method for async call";
    }
}
