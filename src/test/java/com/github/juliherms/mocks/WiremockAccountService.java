package com.github.juliherms.mocks;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.Collections;
import java.util.Map;

public class WiremockAccountService implements QuarkusTestResourceLifecycleManager {
    private WireMockServer wireMockServer;

    private static String URL_PROPERTIES = "com.github.juliherms.services.AccountService/mp-rest/url";

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();

        //mock's call balance to accounts
        stubFor(get(urlEqualTo("/accounts/121212/balance"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("435.76")
                ));

        //mock's call accounts transaction
        stubFor(post(urlEqualTo("/accounts/121212/transaction"))
                .willReturn(aResponse()
                        //noContent() needed to be changed once the external service returned a Map
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{}")
                ));

        return Collections.singletonMap(URL_PROPERTIES, wireMockServer.baseUrl());
    }

    @Override
    public void stop() {
        if (null != wireMockServer) {
            wireMockServer.stop();
        }
    }
}
