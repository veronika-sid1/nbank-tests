package specs;

import configs.Config;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import models.LoginUserRequest;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RequestSpecs {
    public static final long NON_EXISTENT_ACCOUNT_ID = Long.MAX_VALUE;
    public static final double INITIAL_BALANCE = 0.0;
    public static final double MAX_DEPOSIT = 5000.0;

    private static final Map<String, String> authHeaders = new HashMap<>(Map.of("admin", "Basic YWRtaW46YWRtaW4="));

    private RequestSpecs(){}

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(
                        new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri(Config.getProperty("server") + Config.getProperty("apiVersion"));
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification brokenAuthUserSpec() {
        return defaultRequestBuilder()
                .addHeader("Authorization", "Basic YWRtaW46YWRta")
                .build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestBuilder()
                .setAuth(RestAssured.preemptive().basic(
                        Config.getProperty("admin.login"),
                        Config.getProperty("admin.password")
                ))
                .build();
    }

    public static RequestSpecification authAsUser(String username, String password) {
        String userAuthHeader;

        if (!authHeaders.containsKey(username)) {
            userAuthHeader = new CrudRequester(
                    RequestSpecs.unauthSpec(),
                    Endpoint.LOGIN,
                    ResponseSpecs.requestReturnsOK())
                    .post(LoginUserRequest.builder().username(username).password(password).build())
                    .extract()
                    .header("Authorization");

            authHeaders.put(username, userAuthHeader);
        } else {
            userAuthHeader = authHeaders.get(username);
        }

        return defaultRequestBuilder()
                .addHeader("Authorization", userAuthHeader)
                .build();
    }
}
