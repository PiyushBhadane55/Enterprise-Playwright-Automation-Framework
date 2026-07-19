package com.enterprise.utilities;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Dynamic Wait Utility to handle custom explicit waits using Playwright APIs.
 * Eliminates the need for Thread.sleep().
 */
public class WaitUtil {

    private static final Logger log = LogManager.getLogger(WaitUtil.class);
    private static final double DEFAULT_TIMEOUT_MS = 10000; // 10 seconds

    /**
     * Waits for the page to reach a specific load state.
     */
    public static void waitForLoadState(Page page, LoadState loadState) {
        log.info("Waiting for page load state: {}", loadState);
        page.waitForLoadState(loadState);
    }

    /**
     * Waits for the page to reach NETWORKIDLE state.
     */
    public static void waitForNetworkIdle(Page page) {
        waitForLoadState(page, LoadState.NETWORKIDLE);
    }

    /**
     * Waits for the locator to become visible.
     */
    public static void waitForElementVisible(Locator locator, double timeoutMs) {
        log.info("Waiting for element to be visible: {}", locator);
        locator.waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(timeoutMs));
    }

    public static void waitForElementVisible(Locator locator) {
        waitForElementVisible(locator, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Waits for the locator to become hidden.
     */
    public static void waitForElementHidden(Locator locator, double timeoutMs) {
        log.info("Waiting for element to be hidden: {}", locator);
        locator.waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN).setTimeout(timeoutMs));
    }

    public static void waitForElementHidden(Locator locator) {
        waitForElementHidden(locator, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Waits for a custom javascript function to evaluate to true.
     */
    public static void waitForJavaScriptCondition(Page page, String jsExpression, double timeoutMs) {
        log.info("Waiting for JS condition to be true: {}", jsExpression);
        page.waitForFunction(jsExpression, null, new Page.WaitForFunctionOptions().setTimeout(timeoutMs));
    }

    /**
     * Custom poll sleep (use very sparingly, when waiting for non-UI tasks like file downloads or DB updates).
     *
     * @param ms milliseconds to sleep
     */
    public static void sleep(long ms) {
        log.warn("Standard sleep of {}ms called. Prefer using dynamic wait options.", ms);
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Sleep interrupted.", e);
        }
    }
}
