package com.enterprise.listeners;

import com.enterprise.driver.PlaywrightFactory;
import com.enterprise.utilities.ScreenshotUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG Test Listener to handle logging, capturing screenshots on UI test failures,
 * and reporting test events to the console/log files.
 */
public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        log.info("=== STARTING TEST SUITE: {} ===", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("=== TEST SUITE COMPLETED: {} ===", context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info("Starting Test: {}.{}", result.getTestClass().getName(), result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("Test PASSED: {}.{}", result.getTestClass().getName(), result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("Test FAILED: {}.{}", result.getTestClass().getName(), result.getName());
        log.error("Failure Reason: ", result.getThrowable());

        // Check if there is an active Playwright page instance to capture a failure screenshot
        try {
            if (PlaywrightFactory.getPage() != null) {
                log.info("Active Playwright session detected. Capturing screenshot on failure.");
                ScreenshotUtil.attachScreenshotToAllure(PlaywrightFactory.getPage(), result.getName() + "_failure");
            } else {
                log.debug("No active Playwright session found for this thread; skipping screenshot capture.");
            }
        } catch (Exception e) {
            log.error("Failed to capture and attach screenshot on failure.", e);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("Test SKIPPED: {}.{}", result.getTestClass().getName(), result.getName());
    }
}
