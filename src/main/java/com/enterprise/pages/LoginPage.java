package com.enterprise.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Login Page object handling actions for both SauceDemo and OrangeHRM platforms.
 * Showcases self-healing locators.
 */
public class LoginPage extends BasePage {

    private static final Logger log = LogManager.getLogger(LoginPage.class);

    // SauceDemo Selectors (Primary and Fallbacks)
    private static final String[] SAUCE_USER_SELECTORS = {"#user-name", "input[placeholder='Username']", "//input[@id='user-name']"};
    private static final String[] SAUCE_PWD_SELECTORS = {"#password", "input[placeholder='Password']", "//input[@id='password']"};
    private static final String[] SAUCE_LOGIN_SELECTORS = {"#login-button", "input[type='submit']", "//input[@id='login-button']"};

    // OrangeHRM Selectors (Primary and Fallbacks)
    private static final String[] HRM_USER_SELECTORS = {"input[name='username']", "//input[@placeholder='Username']", "input.oxd-input"};
    private static final String[] HRM_PWD_SELECTORS = {"input[name='password']", "//input[@placeholder='Password']", "input[type='password']"};
    private static final String[] HRM_LOGIN_SELECTORS = {"button[type='submit']", "//button[contains(.,'Login')]", "button.oxd-button"};

    public LoginPage(Page page) {
        super(page);
    }

    /**
     * Executes login workflow for SauceDemo.
     */
    public void loginToSauceDemo(String username, String password) {
        log.info("Logging in to SauceDemo with user: {}", username);
        
        Locator userField = findElementWithFallback(SAUCE_USER_SELECTORS);
        Locator pwdField = findElementWithFallback(SAUCE_PWD_SELECTORS);
        Locator loginBtn = findElementWithFallback(SAUCE_LOGIN_SELECTORS);

        fill(userField, username);
        fill(pwdField, password);
        click(loginBtn);
        log.info("SauceDemo login form submitted.");
    }

    /**
     * Executes login workflow for OrangeHRM.
     */
    public void loginToOrangeHrm(String username, String password) {
        log.info("Logging in to OrangeHRM with user: {}", username);
        
        Locator userField = findElementWithFallback(HRM_USER_SELECTORS);
        Locator pwdField = findElementWithFallback(HRM_PWD_SELECTORS);
        Locator loginBtn = findElementWithFallback(HRM_LOGIN_SELECTORS);

        fill(userField, username);
        fill(pwdField, password);
        click(loginBtn);
        log.info("OrangeHRM login form submitted.");
    }
}
