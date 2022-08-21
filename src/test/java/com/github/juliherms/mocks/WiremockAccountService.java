package com.github.juliherms.mocks;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import javax.ws.rs.core.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.Collections;
import java.util.Map;

public class WiremockAccountService implements QuarkusTestResourceLifecycleManager {
    private WireMockServer wireMockServer;

    // Scenarios from Circuit Breaker
    private static final String SERVER_ERROR_1 = "CB Fail 1";
    private static final String SERVER_ERROR_2 = "CB Fail 2";
    private static final String CB_OPEN_1 = "CB Open 1";
    private static final String CB_OPEN_2 = "CB Open 2";
    private static final String CB_OPEN_3 = "CB Open 3";
    private static final String CB_SUCCESS_1 = "CB Success 1";
    private static final String CB_SUCCESS_2 = "CB Success 2";

    private static String URL_PROPERTIES = "com.github.juliherms.services.AccountService/mp-rest/url";

    /**
     * This method responsible to mock Account Service
     */
    protected void mockAccountService() {

        /**
         * When calls '/accounts/121212/balance' returns '435.76 balance'
         */
        stubFor(get(urlEqualTo("/accounts/121212/balance"))
                .willReturn(aResponse().
                        withHeader("Content-Type", "application/json").
                        withBody("435.76")));

        /**
         * When calls '/accounts/121212/transaction' return 'http status 200'
         */
        stubFor(post(urlEqualTo("/accounts/121212/transaction"))
                .willReturn(aResponse()
                // noContent() needed to be changed once the external service returned a Map
                .withHeader("Content-Type", "application/json").
                        withStatus(200).withBody("{}")));
    }

    /**
     * This method responsible to mocks timeout to calls account balance
     */
    protected void mockTimeout() {

        // when calls '/accounts/123456/balance' returns 437.76 and http 200 status code and 200 ms for delay
        stubFor(get(urlEqualTo("/accounts/123456/balance"))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(200).withFixedDelay(200)
                                .withBody("435.76")));

        // when class '/accounts/456789/balance' returns 435.76 and http status 200
        stubFor(get(urlEqualTo("/accounts/456789/balance"))
                .willReturn
                        (aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(200)
                                .withBody("435.76")));
    }

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();

        mockAccountService(); //mocks transactions and account balance
        mockTimeout(); //mocks get account balance with timeout
        mockCircuitBreaker(); //mocks circuit breaker

        return Collections.singletonMap(URL_PROPERTIES, wireMockServer.baseUrl());
    }

    /**
     * Define wiremock scenario to support the required by a circuit breaker state machine
     */
    void mockCircuitBreaker() {

        //when first call, server return error
        createCircuitBreakerStub(Scenario.STARTED, SERVER_ERROR_1, "100.00", 200);
        //when second call we have server error and next call return server error
        createCircuitBreakerStub(SERVER_ERROR_1, SERVER_ERROR_2, "200.00", 502);
        //when third call we have server error and circuit breaker is open
        createCircuitBreakerStub(SERVER_ERROR_2, CB_OPEN_1, "300.00", 502);
        //when fourth call we have circuit breaker open and return circuit breaker open
        createCircuitBreakerStub(CB_OPEN_1, CB_OPEN_2, "400.00", 200);
        createCircuitBreakerStub(CB_OPEN_2, CB_OPEN_3, "400.00", 200);
        createCircuitBreakerStub(CB_OPEN_3, CB_SUCCESS_1, "500.00", 200);
        createCircuitBreakerStub(CB_SUCCESS_1, CB_SUCCESS_2, "600.00", 200);
    }


    /**
     * This method responsible to simulate Circuit Breaker Scenario
     * @param currentState - Current state scenario
     * @param nextState - Next state scenario
     * @param response - Response mock
     * @param status - Status mock
     */
    void createCircuitBreakerStub(String currentState, String nextState,
                                  String response, int status) {

        stubFor(
                post(urlEqualTo("/accounts/444666/transaction")).
                        inScenario("circuitbreaker")
                .whenScenarioStateIs(currentState).
                        willSetStateTo(nextState).
                        willReturn(
                        aResponse().
                                withStatus(status).
                                withHeader("Content-Type", MediaType.TEXT_PLAIN)
                                .withBody(response)));
    }

    @Override
    public void stop() {
        if (null != wireMockServer) {
            wireMockServer.stop();
        }
    }
}
