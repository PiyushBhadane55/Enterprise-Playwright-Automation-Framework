package com.enterprise.tests.api;

import com.enterprise.api.ApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Customer API Management")
public class CustomerApiTest {

    @Test(description = "Verify GET Customer details")
    @Severity(SeverityLevel.NORMAL)
    @Description("Sends a GET request to retrieve user details and asserts the status code and values.")
    public void testGetCustomer() {
        Response response = ApiClient.get("/users/2");
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getTime()).isLessThan(3000L); // Response time check
        
        String firstName = response.jsonPath().getString("data.first_name");
        String lastName = response.jsonPath().getString("data.last_name");
        
        assertThat(firstName).isEqualTo("Janet");
        assertThat(lastName).isEqualTo("Weaver");
    }

    @Test(description = "Verify POST Create Customer")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Sends a POST request to register a customer, validates the schema and response properties.")
    public void testCreateCustomer() {
        Map<String, String> body = new HashMap<>();
        body.put("name", "Antigravity");
        body.put("job", "AI Agent");

        Response response = ApiClient.post("/users", body);

        assertThat(response.getStatusCode()).isEqualTo(201);
        
        // Assert response values
        String name = response.jsonPath().getString("name");
        String job = response.jsonPath().getString("job");
        assertThat(name).isEqualTo("Antigravity");
        assertThat(job).isEqualTo("AI Agent");

        // Validate JSON Schema
        response.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/customer-schema.json"));
    }

    @Test(description = "Verify PUT Update Customer")
    @Severity(SeverityLevel.NORMAL)
    @Description("Sends a PUT request to update customer details and asserts successful updates.")
    public void testUpdateCustomer() {
        Map<String, String> body = new HashMap<>();
        body.put("name", "Antigravity Modified");
        body.put("job", "Lead AI Agent");

        Response response = ApiClient.put("/users/2", body, null);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("name")).isEqualTo("Antigravity Modified");
        assertThat(response.jsonPath().getString("job")).isEqualTo("Lead AI Agent");
    }

    @Test(description = "Verify DELETE Customer")
    @Severity(SeverityLevel.NORMAL)
    @Description("Sends a DELETE request and asserts 204 No Content response status code.")
    public void testDeleteCustomer() {
        Response response = ApiClient.delete("/users/2", null);
        assertThat(response.getStatusCode()).isEqualTo(204);
    }
}
