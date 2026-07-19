package com.enterprise.tests.ui;

import com.enterprise.config.ConfigManager;
import com.enterprise.pages.DashboardPage;
import com.enterprise.pages.LoginPage;
import com.enterprise.tests.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

@Feature("SauceDemo Authentication")
public class SauceDemoTest extends BaseTest {

    @Test(description = "Verify SauceDemo login and logout workflow")
    @Severity(SeverityLevel.BLOCKER)
    @Description("This test navigates to SauceDemo, logs in using configured credentials, verifies the dashboard, and logs out.")
    public void testSauceDemoLoginLogout() {
        LoginPage loginPage = new LoginPage(page);
        DashboardPage dashboardPage = new DashboardPage(page);

        // 1. Navigate to SauceDemo URL
        String url = ConfigManager.get("saucedemo.url");
        loginPage.navigate(url);

        // 2. Perform Login
        String user = ConfigManager.get("saucedemo.username");
        String pass = ConfigManager.get("saucedemo.password");
        loginPage.loginToSauceDemo(user, pass);

        // 3. Verify Dashboard Loaded
        boolean isDashboardLoaded = dashboardPage.isSauceDemoDashboardVisible();
        Assert.assertTrue(isDashboardLoaded, "SauceDemo Dashboard failed to load after login.");

        // 4. Perform Logout
        dashboardPage.logoutFromSauceDemo();

        // 5. Verify Redirect back to Login Page
        Assert.assertTrue(page.url().contains("saucedemo.com"), "Did not redirect to SauceDemo login page after logout.");
    }
}
