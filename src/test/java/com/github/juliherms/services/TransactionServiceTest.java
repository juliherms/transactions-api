package com.github.juliherms.services;


import com.github.juliherms.mocks.WiremockAccountService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;

/**
 * This class responsible to test Transaction test
 */
@QuarkusTest
@QuarkusTestResource(WiremockAccountService.class)
public class TransactionServiceTest {

    @Test
    void testTransaction() {
        given()
                .body("142.12")
                .contentType(ContentType.JSON)
                .when().post("/transactions/{accountNumber}", 121212)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    /**
     * This method responsible to test calls balance to accounts api with
     * two scenarios
     * 1 - First call occurs timeout
     * 2 - Second call occurs success
     */
    @Test
    void testTimeout() {

        //test for timeout
        given()
                .contentType(ContentType.JSON)
                .get("/transactions/123456/balance")
                .then().statusCode(Response.Status.GATEWAY_TIMEOUT.getStatusCode());

        //test for response without timeout
        given()
                .contentType(ContentType.JSON)
                .get("/transactions/456789/balance")
                .then().statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void testCircuitBreaker() {

        RequestSpecification request =
                given()
                        .body("142.12")
                        .contentType(ContentType.JSON);

        request.post("/transactions/api/444666").then().statusCode(Response.Status.OK.getStatusCode());
        request.post("/transactions/api/444666").then().statusCode(Response.Status.BAD_GATEWAY.getStatusCode());
        request.post("/transactions/api/444666").then().statusCode(Response.Status.BAD_GATEWAY.getStatusCode());
        request.post("/transactions/api/444666").then().statusCode(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        request.post("/transactions/api/444666").then().statusCode(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());

        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
        }

        request.post("/transactions/api/444666").then().statusCode(Response.Status.OK.getStatusCode());
        request.post("/transactions/api/444666").then().statusCode(Response.Status.OK.getStatusCode());
    }
}
