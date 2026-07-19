package com.enterprise.tests;

import com.enterprise.database.DatabaseManager;
import com.enterprise.driver.PlaywrightFactory;
import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Base Test class executing driver setup, teardown, and database cleanup for all tests.
 */
public class BaseTest {

    private static final Logger log = LogManager.getLogger(BaseTest.class);
    protected Page page;

    /**
     * Set up browser before each test execution.
     *
     * @param browser Browser name passed from TestNG XML suite (defaults to Chrome)
     */
    @BeforeMethod
    @Parameters({"browser"})
    public void setUp(@Optional("chrome") String browser) {
        log.info("Setting up test driver for browser: {}", browser);
        page = PlaywrightFactory.initDriver(browser);
    }

    /**
     * Clean up driver session after each test execution.
     */
    @AfterMethod
    public void tearDown() {
        log.info("Tearing down test driver.");
        PlaywrightFactory.quitDriver();
    }

    /**
     * Cleanup and close database connection pool at the end of execution.
     */
    @AfterSuite
    public void tearDownSuite() {
        log.info("Shutting down resources at suite level.");
        DatabaseManager.close();
    }
}
