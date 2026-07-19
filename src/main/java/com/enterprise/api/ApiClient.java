package com.enterprise.api;

import com.enterprise.config.ConfigManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;

/**
 * Enterprise API Client wrapper utilizing REST Assured.
 * Automatically injects Allure reporting filters and environment configurations.
 */
public class ApiClient {

    private static final Logger log = LogManager.getLogger(ApiClient.class);
    private static RequestSpecification requestSpec;

    static {
        initialize();
    }

    private static void initialize() {
        String baseUri = ConfigManager.get("api.base.uri");
        log.info("Initializing API Client with Base URI: {}", baseUri);
        
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured()) // Automatic API logs in Allure Reports
                .build();
    }

    /**
     * Standard RequestSpecification builder with custom headers and authorization.
     */
    private static RequestSpecification getRequestSpec(Map<String, String> headers, Map<String, String> queryParams) {
        RequestSpecification spec = RestAssured.given().spec(requestSpec);
        
        if (headers != null && !headers.isEmpty()) {
            spec.headers(headers);
        }
        if (queryParams != null && !queryParams.isEmpty()) {
            spec.queryParams(queryParams);
        }

        // Always log request details for troubleshooting
        spec.log().all();

        return spec;
    }

    /**
     * GET request.
     */
    public static Response get(String endpoint, Map<String, String> headers, Map<String, String> queryParams) {
        log.info("Sending GET request to: {}", endpoint);
        Response response = getRequestSpec(headers, queryParams).get(endpoint);
        log.info("GET Response Status Code: {}", response.getStatusCode());
        response.then().log().all(); // log response
        return response;
    }

    public static Response get(String endpoint) {
        return get(endpoint, Collections.emptyMap(), Collections.emptyMap());
    }

    /**
     * POST request.
     */
    public static Response post(String endpoint, Object body, Map<String, String> headers) {
        log.info("Sending POST request to: {}", endpoint);
        RequestSpecification spec = getRequestSpec(headers, Collections.emptyMap());
        if (body != null) {
            spec.body(body);
        }
        Response response = spec.post(endpoint);
        log.info("POST Response Status Code: {}", response.getStatusCode());
        response.then().log().all(); // log response
        return response;
    }

    public static Response post(String endpoint, Object body) {
        return post(endpoint, body, Collections.emptyMap());
    }

    /**
     * PUT request.
     */
    public static Response put(String endpoint, Object body, Map<String, String> headers) {
        log.info("Sending PUT request to: {}", endpoint);
        RequestSpecification spec = getRequestSpec(headers, Collections.emptyMap());
        if (body != null) {
            spec.body(body);
        }
        Response response = spec.put(endpoint);
        log.info("PUT Response Status Code: {}", response.getStatusCode());
        response.then().log().all(); // log response
        return response;
    }

    /**
     * DELETE request.
     */
    public static Response delete(String endpoint, Map<String, String> headers) {
        log.info("Sending DELETE request to: {}", endpoint);
        Response response = getRequestSpec(headers, Collections.emptyMap()).delete(endpoint);
        log.info("DELETE Response Status Code: {}", response.getStatusCode());
        response.then().log().all(); // log response
        return response;
    }

    /**
     * PATCH request.
     */
    public static Response patch(String endpoint, Object body, Map<String, String> headers) {
        log.info("Sending PATCH request to: {}", endpoint);
        RequestSpecification spec = getRequestSpec(headers, Collections.emptyMap());
        if (body != null) {
            spec.body(body);
        }
        Response response = spec.patch(endpoint);
        log.info("PATCH Response Status Code: {}", response.getStatusCode());
        response.then().log().all(); // log response
        return response;
    }
}
