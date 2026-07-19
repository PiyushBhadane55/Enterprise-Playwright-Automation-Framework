package com.enterprise.listeners;

import com.enterprise.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retry Analyzer to automatically rerun failed tests up to a configured threshold.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private int count = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetry = ConfigManager.getInt("test.retry.max", 3);

        if (count < maxRetry) {
            count++;
            log.warn("Test '{}' failed. Retrying test (Attempt {} of {}).", result.getName(), count, maxRetry);
            return true; // Rerun the test
        }
        return false; // Do not rerun
    }
}
