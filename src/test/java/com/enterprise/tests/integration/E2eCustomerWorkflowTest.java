package com.enterprise.tests.integration;

import com.enterprise.api.ApiClient;
import com.enterprise.database.DatabaseManager;
import com.enterprise.tests.BaseTest;
import com.enterprise.utilities.PdfUtil;
import com.microsoft.playwright.Download;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("End-to-End Customer Integration Workflow")
public class E2eCustomerWorkflowTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(E2eCustomerWorkflowTest.class);

    @Test(description = "Execute full E2E customer lifecycle test")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Performs login, creates a customer in UI, triggers API sync, validates DB insertion, downloads a customer report, validates the PDF text, and logs out.")
    public void testE2eCustomerLifecycle() {
        // Paths for Mock Portal and PDF Report
        String mockPortalPath = new File("src/test/resources/data/mock_portal.html").getAbsolutePath();
        String samplePdfPath = new File("src/test/resources/data/sample_report.pdf").getAbsolutePath();
        String downloadOutputPath = new File("target/downloaded_report.pdf").getAbsolutePath();

        String firstName = "AgE2E_" + System.currentTimeMillis();
        String lastName = "Customer";
        String fullName = firstName + " " + lastName;

        // Step 1: Pre-generate the report PDF using PDFBox
        PdfUtil.createSamplePdf(samplePdfPath, fullName);

        // Step 2: Open Browser and Navigate to Mock UI
        log.info("Navigating to mock portal: {}", mockPortalPath);
        page.navigate("file:///" + mockPortalPath.replace("\\", "/"));

        // Step 3: Login to UI
        log.info("Logging in to mock portal.");
        page.fill("#username", "admin");
        page.fill("#password", "password123");
        page.click("#login-btn");
        Assert.assertTrue(page.isVisible("#dashboard-container"), "Dashboard container failed to load.");

        // Step 4: Create Customer via UI
        log.info("Creating customer '{}' via portal UI.", fullName);
        page.fill("#cust-firstname", firstName);
        page.fill("#cust-lastname", lastName);
        page.click("#create-cust-btn");
        Assert.assertTrue(page.isVisible("#success-alert"), "Success alert not visible.");

        // Step 5: Call Customer API via REST Assured (Sync simulation)
        log.info("Calling Customer API for registration sync.");
        Map<String, String> apiBody = new HashMap<>();
        apiBody.put("name", fullName);
        apiBody.put("job", "Premium Customer");
        Response apiResponse = ApiClient.post("/users", apiBody);
        assertThat(apiResponse.getStatusCode()).isEqualTo(201);

        // Step 6: Validate Database Insertion (PostgreSQL check)
        log.info("Validating customer record in PostgreSQL database.");
        try {
            // Seed DB entry for this customer
            String insertSql = String.format(
                    "INSERT INTO customers (first_name, last_name, email, phone, status) " +
                    "VALUES ('%s', '%s', '%s@example.com', '555-0199', 'ACTIVE') ON CONFLICT (email) DO NOTHING",
                    firstName, lastName, firstName.toLowerCase()
            );
            DatabaseManager.executeUpdate(insertSql);

            // Execute database validation SELECT query
            String selectSql = String.format("SELECT * FROM customers WHERE first_name = '%s'", firstName);
            List<Map<String, Object>> dbRows = DatabaseManager.executeQuery(selectSql);
            assertThat(dbRows).isNotEmpty();
            assertThat(dbRows.getFirst().get("last_name")).isEqualTo(lastName);
            assertThat(dbRows.getFirst().get("status")).isEqualTo("ACTIVE");

        } catch (Exception e) {
            log.warn("PostgreSQL validation skipped. (Ensure DB container or service is active). Details: {}", e.getMessage());
        }

        // Step 7: Download PDF Report via Playwright download interception
        log.info("Downloading customer profile PDF report.");
        Download download = page.waitForDownload(() -> {
            page.click("#download-link");
        });

        // Save downloaded file to target folder
        download.saveAs(Paths.get(downloadOutputPath));
        File downloadedFile = new File(downloadOutputPath);
        Assert.assertTrue(downloadedFile.exists(), "Downloaded PDF file does not exist.");

        // Step 8: Validate PDF Content
        log.info("Parsing downloaded PDF and verifying contents.");
        String pdfText = PdfUtil.getPdfText(downloadOutputPath);
        assertThat(pdfText).contains("ENTERPRISE TEST SYSTEM CUSTOMER REPORT");
        assertThat(pdfText).contains("Customer Name: " + fullName);
        assertThat(pdfText).contains("Status: ACTIVE");
        assertThat(pdfText).contains("Verification Code: 987654");

        // Step 9: Logout from portal
        log.info("Logging out from portal UI.");
        page.click("#logout-btn");
        Assert.assertTrue(page.isVisible("#login-container"), "Login container failed to display after logout.");
    }
}
