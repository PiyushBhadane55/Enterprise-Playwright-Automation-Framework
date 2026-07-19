package com.enterprise.tests.db;

import com.enterprise.database.DatabaseManager;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Database Validation")
public class CustomerDatabaseTest {

    @Test(description = "Verify reading seed customers from PostgreSQL database")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test executes a SELECT query against PostgreSQL to verify that Flyway seed data was inserted.")
    public void testDatabaseSeedData() {
        try {
            // Retrieve default seed customer
            List<Map<String, Object>> rows = DatabaseManager.executeQuery("SELECT * FROM customers WHERE email = 'john.doe@example.com'");
            
            assertThat(rows).isNotEmpty();
            Map<String, Object> firstRow = rows.getFirst();
            
            assertThat(firstRow.get("first_name")).isEqualTo("John");
            assertThat(firstRow.get("last_name")).isEqualTo("Doe");
            assertThat(firstRow.get("status")).isEqualTo("ACTIVE");
            
        } catch (Exception e) {
            // Database might not be running in clean local compile environments, we log and soft-fail
            Assert.fail("Failed to connect or query database. Ensure PostgreSQL container is running.", e);
        }
    }

    @Test(description = "Verify inserting and deleting customer in PostgreSQL database")
    @Severity(SeverityLevel.NORMAL)
    @Description("This test inserts a customer record, queries database to verify insertion, and deletes the record.")
    public void testDatabaseCrudOperations() {
        try {
            String insertSql = "INSERT INTO customers (first_name, last_name, email, phone, status) " +
                    "VALUES ('Temp', 'User', 'temp.user@example.com', '9999999999', 'PENDING')";
            
            int rowsAffected = DatabaseManager.executeUpdate(insertSql);
            assertThat(rowsAffected).isEqualTo(1);

            // Validate
            List<Map<String, Object>> rows = DatabaseManager.executeQuery("SELECT * FROM customers WHERE email = 'temp.user@example.com'");
            assertThat(rows).hasSize(1);
            assertThat(rows.getFirst().get("status")).isEqualTo("PENDING");

            // Cleanup
            int deletedRows = DatabaseManager.executeUpdate("DELETE FROM customers WHERE email = 'temp.user@example.com'");
            assertThat(deletedRows).isEqualTo(1);

        } catch (Exception e) {
            Assert.fail("Failed CRUD test. Check database status.", e);
        }
    }
}
