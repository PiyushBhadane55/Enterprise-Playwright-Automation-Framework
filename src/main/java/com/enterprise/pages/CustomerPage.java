package com.enterprise.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Customer/Employee Page Object managing employee creation in OrangeHRM.
 */
public class CustomerPage extends BasePage {

    private static final Logger log = LogManager.getLogger(CustomerPage.class);

    // OrangeHRM Employee Selectors
    private static final String[] ADD_BTN_SELECTORS = {"//button[contains(.,'Add')]", "button.oxd-button--secondary", "//button[@class='oxd-button oxd-button--medium oxd-button--secondary']"};
    private static final String[] FIRST_NAME_SELECTORS = {"input[name='firstName']", "input.orangehrm-firstname", "//input[@placeholder='First Name']"};
    private static final String[] LAST_NAME_SELECTORS = {"input[name='lastName']", "input.orangehrm-lastname", "//input[@placeholder='Last Name']"};
    private static final String[] SAVE_BTN_SELECTORS = {"button[type='submit']", "//button[text()=' Save ']", "button.oxd-button--secondary"};
    private static final String[] DETAILS_HEADER_SELECTORS = {"h6.orangehrm-main-title", "//h6[text()='Personal Details']"};

    public CustomerPage(Page page) {
        super(page);
    }

    /**
     * Clicks on the 'Add Employee' button in OrangeHRM.
     */
    public void clickAddEmployeeButton() {
        log.info("Clicking Add Employee Button.");
        Locator addBtn = findElementWithFallback(ADD_BTN_SELECTORS);
        click(addBtn);
        // Wait for page transition
        page.waitForURL("**/addEmployee");
        log.info("Add Employee page loaded.");
    }

    /**
     * Fills customer/employee details and clicks save.
     */
    public void createEmployee(String firstName, String lastName) {
        log.info("Creating employee: {} {}", firstName, lastName);
        
        Locator fnField = findElementWithFallback(FIRST_NAME_SELECTORS);
        Locator lnField = findElementWithFallback(LAST_NAME_SELECTORS);
        Locator saveBtn = findElementWithFallback(SAVE_BTN_SELECTORS);

        fill(fnField, firstName);
        fill(lnField, lastName);
        click(saveBtn);
        log.info("Employee details filled and Save clicked.");
    }

    /**
     * Validates if the employee was successfully created.
     */
    public boolean isEmployeeCreated() {
        log.info("Checking employee creation status.");
        try {
            // After save, OrangeHRM redirects to viewPersonalDetails page
            page.waitForURL("**/viewPersonalDetails/**", new Page.WaitForURLOptions().setTimeout(10000));
            Locator personalDetailsHeader = findElementWithFallback(DETAILS_HEADER_SELECTORS);
            return personalDetailsHeader.isVisible();
        } catch (Exception e) {
            log.error("Employee details page did not load.", e);
            return false;
        }
    }
}
