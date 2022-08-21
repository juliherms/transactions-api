package com.github.juliherms.resources;

import com.github.juliherms.handlers.TransactionServiceFallbackHandler;
import com.github.juliherms.services.AccountService;
import com.github.juliherms.services.AccountServiceProgrammatic;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.*;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {

    @Inject
    @RestClient
    AccountService accountService;

    @ConfigProperty(name = "account.service", defaultValue = "http://localhost:8080")
    String accountServiceUrl;

    /**
     * Method responsible to create transaction by account number - sync
     * @param accountNumber
     * @param amount
     * @return
     */
    @POST
    @Path("/{acctNumber}")
    public Map<String, List<String>> newTransaction(
            @PathParam("acctNumber") Long accountNumber,
            BigDecimal amount) {
        try {
            return accountService.transact(accountNumber, amount);
        } catch (Throwable t) {
            t.printStackTrace();
            Map<String, List<String>> response = new HashMap<>();
            response.put("EXCEPTION - " + t.getClass(), Collections.singletonList(t.getMessage()));
            return response;
        }
    }

    /**
     * Method responsible to create transaction by account number - Async
     * @param accountNumber
     * @param amount
     * @return
     */
    @POST
    @Path("/async/{acctNumber}")
    public CompletionStage<Map<String, List<String>>> newTransactionAsync(
            @PathParam("acctNumber") Long accountNumber,
            BigDecimal amount) {

        System.out.println("chamou o metodo async");
        System.out.println("Valor " + amount.toString());

        return accountService.transactAsync(accountNumber, amount);
    }

    /**
     * Method responsible to create transaction sync
     * Bulkhead - Limits the number of concurrent requests
     * @param accountNumber
     * @param amount
     * @return
     * @throws MalformedURLException
     */
    @POST
    @Path("/api/{acctNumber}")
    @Bulkhead(1) //If more than one concurrent operation is attempted, a BulkheadException will be thrown.
    @CircuitBreaker(
            requestVolumeThreshold=3,
            failureRatio=.66,
            delay = 1,
            delayUnit = ChronoUnit.SECONDS,
            successThreshold=2
    )
    @Fallback(value = TransactionServiceFallbackHandler.class)
    public Response newTransactionWithApi(@PathParam("acctNumber") Long accountNumber,
                                          BigDecimal amount) throws MalformedURLException {
        AccountServiceProgrammatic acctService =
                RestClientBuilder.newBuilder()
                        .baseUrl(new URL(accountServiceUrl))
                        .connectTimeout(500, TimeUnit.MILLISECONDS)
                        .readTimeout(1200, TimeUnit.MILLISECONDS)
                        .build(AccountServiceProgrammatic.class);

        acctService.transact(accountNumber, amount);
        return Response.ok().build();
    }

    @POST
    @Path("/api/async/{acctNumber}")
    public CompletionStage<Void> newTransactionWithApiAsync(@PathParam("acctNumber") Long accountNumber,
                                                            BigDecimal amount) throws MalformedURLException {
        AccountServiceProgrammatic acctService =
                RestClientBuilder.newBuilder()
                        .baseUrl(new URL(accountServiceUrl))
                        .build(AccountServiceProgrammatic.class);

        return acctService.transactAsync(accountNumber, amount);
    }

    /**
     * This method responsible to return actual balance by account number
     * @param accountNumber
     * @return
     */
    @GET
    @Path("/{acctnumber}/balance")
    @Timeout(100)
    @Retry(delay = 100,
            jitter = 25,
            maxRetries = 3,
            retryOn = TimeoutException.class)
    @Fallback(value = TransactionServiceFallbackHandler.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBalance(@PathParam("acctnumber") Long accountNumber) {

        String balance = accountService.getBalance(accountNumber).toString();
        return Response.ok(balance).build();
    }

}
