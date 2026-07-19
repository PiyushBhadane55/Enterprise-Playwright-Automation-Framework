package com.enterprise.tests.ui;

import com.enterprise.config.ConfigManager;
import com.enterprise.pages.CustomerPage;
import com.enterprise.pages.DashboardPage;
import com.enterprise.pages.LoginPage;
import com.enterprise.tests.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

@Feature("OrangeHRM Employee Management")
public class OrangeHrmTest extends BaseTest {

    @Test(description = "Verify OrangeHRM login, employee creation, and logout flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test log in to OrangeHRM, navigates to the PIM module, adds a new employee, verifies creation, and logs out.")
    public void testOrangeHrmEmployeeCreation() {
        LoginPage loginPage = new LoginPage(page);
        DashboardPage dashboardPage = new DashboardPage(page);
        CustomerPage customerPage = new CustomerPage(page);

        // 1. Navigate to OrangeHRM
        String url = ConfigManager.get("orangehrm.url");
        loginPage.navigate(url);

        // 2. Login
        String user = ConfigManager.get("orangehrm.username");
        String pass = ConfigManager.get("orangehrm.password");
        loginPage.loginToOrangeHrm(user, pass);

        // 3. Verify Dashboard Loaded
        Assert.assertTrue(dashboardPage.isOrangeHrmDashboardVisible(), "OrangeHRM Dashboard failed to display.");

        // 4. Navigate to PIM and click Add Employee
        dashboardPage.navigateToPimModule();
        customerPage.clickAddEmployeeButton();

        // 5. Create Employee
        String firstName = "AgAutomator_" + System.currentTimeMillis();
        String lastName = "QA";
        customerPage.createEmployee(firstName, lastName);

        // 6. Verify Employee Created
        boolean isCreated = customerPage.isEmployeeCreated();
        Assert.assertTrue(isCreated, "Employee personal details page did not load (creation failed).");

        // 7. Logout
        dashboardPage.logoutFromOrangeHrm();
    }
}
