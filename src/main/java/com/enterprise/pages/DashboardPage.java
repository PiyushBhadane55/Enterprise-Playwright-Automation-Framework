package com.enterprise.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Dashboard Page object managing operations on post-login dashboard pages.
 */
public class DashboardPage extends BasePage {

    private static final Logger log = LogManager.getLogger(DashboardPage.class);

    // SauceDemo Selectors
    private static final String[] SAUCE_TITLE_SELECTORS = {"span.title", "//span[@class='title']", "//span[text()='Products']"};
    private static final String[] SAUCE_MENU_SELECTORS = {"#react-burger-menu-btn", "//button[@id='react-burger-menu-btn']", "//button[text()='Open Menu']"};
    private static final String[] SAUCE_LOGOUT_SELECTORS = {"#logout_sidebar_link", "//a[@id='logout_sidebar_link']", "//a[text()='Logout']"};

    // OrangeHRM Selectors
    private static final String[] HRM_TITLE_SELECTORS = {"h6.oxd-topbar-header-breadcrumb-module", "//h6[text()='Dashboard']"};
    private static final String[] HRM_PIM_SELECTORS = {"//span[text()='PIM']", "a[href*='pim']", "a[href*='Pim']"};
    private static final String[] HRM_USER_MENU_SELECTORS = {"span.oxd-userdropdown-tab", "i.oxd-userdropdown-icon", "//p[@class='oxd-userdropdown-name']"};
    private static final String[] HRM_LOGOUT_SELECTORS = {"//a[text()='Logout']", "a[href*='logout']", "//a[contains(.,'Logout')]"};

    public DashboardPage(Page page) {
        super(page);
    }

    /**
     * Checks if SauceDemo dashboard page is visible.
     */
    public boolean isSauceDemoDashboardVisible() {
        log.info("Verifying SauceDemo Dashboard display.");
        try {
            Locator title = findElementWithFallback(SAUCE_TITLE_SELECTORS);
            return title.isVisible() && getText(title).equalsIgnoreCase("Products");
        } catch (Exception e) {
            log.error("SauceDemo Dashboard not visible.", e);
            return false;
        }
    }

    /**
     * Checks if OrangeHRM dashboard page is visible.
     */
    public boolean isOrangeHrmDashboardVisible() {
        log.info("Verifying OrangeHRM Dashboard display.");
        try {
            Locator title = findElementWithFallback(HRM_TITLE_SELECTORS);
            return title.isVisible() && getText(title).equalsIgnoreCase("Dashboard");
        } catch (Exception e) {
            log.error("OrangeHRM Dashboard not visible.", e);
            return false;
        }
    }

    /**
     * Navigates to the PIM module (Customer/Employee section) in OrangeHRM.
     */
    public void navigateToPimModule() {
        log.info("Navigating to PIM Module.");
        Locator pimMenu = findElementWithFallback(HRM_PIM_SELECTORS);
        click(pimMenu);
        log.info("PIM Module menu clicked.");
    }

    /**
     * Logs out from SauceDemo.
     */
    public void logoutFromSauceDemo() {
        log.info("Logging out from SauceDemo.");
        click(findElementWithFallback(SAUCE_MENU_SELECTORS));
        // Wait for sliding menu animation to settle
        page.waitForTimeout(500);
        click(findElementWithFallback(SAUCE_LOGOUT_SELECTORS));
        log.info("SauceDemo logout action executed.");
    }

    /**
     * Logs out from OrangeHRM.
     */
    public void logoutFromOrangeHrm() {
        log.info("Logging out from OrangeHRM.");
        click(findElementWithFallback(HRM_USER_MENU_SELECTORS));
        click(findElementWithFallback(HRM_LOGOUT_SELECTORS));
        log.info("OrangeHRM logout action executed.");
    }
}
