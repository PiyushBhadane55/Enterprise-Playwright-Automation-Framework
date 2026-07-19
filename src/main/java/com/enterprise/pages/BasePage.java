package com.enterprise.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base Page class containing reusable action wrappers and Advanced Self-Healing Locators.
 */
public class BasePage {

    protected Page page;
    private static final Logger log = LogManager.getLogger(BasePage.class);

    public BasePage(Page page) {
        this.page = page;
    }

    /**
     * Navigates to a URL.
     */
    public void navigate(String url) {
        log.info("Navigating to URL: {}", url);
        page.navigate(url);
    }

    /**
     * Interactive Click wrapper.
     */
    public void click(String selector) {
        log.info("Clicking on element: {}", selector);
        page.click(selector);
    }

    /**
     * Click wrapper on a resolved Locator.
     */
    public void click(Locator locator) {
        log.info("Clicking on locator element.");
        locator.click();
    }

    /**
     * Interactive Type/Fill wrapper.
     */
    public void fill(String selector, String text) {
        log.info("Typing text into: {}", selector);
        page.fill(selector, text);
    }

    /**
     * Type/Fill wrapper on a resolved Locator.
     */
    public void fill(Locator locator, String text) {
        log.info("Typing text into locator element.");
        locator.fill(text);
    }

    /**
     * Retrieves inner text of selector.
     */
    public String getText(String selector) {
        String text = page.innerText(selector);
        log.debug("Read text from '{}': {}", selector, text);
        return text;
    }

    /**
     * Retrieves inner text of locator.
     */
    public String getText(Locator locator) {
        String text = locator.innerText();
        log.debug("Read text from locator: {}", text);
        return text;
    }

    /**
     * Self-Healing Locator Fallback Mechanism.
     * Evaluates fallback selectors sequentially (e.g. ID -> CSS -> XPath -> Text -> Role)
     * using a short timeout (e.g. 1.5s) until a successful selector is located.
     *
     * @param selectors Array of selectors to try in order of preference.
     * @return Resolved Playwright Locator.
     * @throws RuntimeException If none of the selectors are resolvable.
     */
    public Locator findElementWithFallback(String... selectors) {
        if (selectors == null || selectors.length == 0) {
            throw new IllegalArgumentException("Selectors list cannot be null or empty.");
        }

        // Try the primary selector first
        String primary = selectors[0];
        log.info("Attempting to locate element using primary selector: {}", primary);

        try {
            Locator locator = page.locator(primary);
            // Verify visibility with a quick check (1 second timeout)
            locator.waitFor(new Locator.WaitForOptions().setTimeout(1000));
            return locator;
        } catch (Exception primaryEx) {
            log.warn("Primary locator '{}' failed. Commencing self-healing fallback...", primary);

            // Loop through fallback selectors
            for (int i = 1; i < selectors.length; i++) {
                String fallback = selectors[i];
                log.info("Trying fallback selector [{}]: {}", i, fallback);
                try {
                    Locator locator = page.locator(fallback);
                    locator.waitFor(new Locator.WaitForOptions().setTimeout(1000));
                    log.warn("SELF-HEALING SUCCESSFUL: Healed element. Primary '{}' fallback to '{}'.", primary, fallback);
                    return locator;
                } catch (Exception fallbackEx) {
                    log.debug("Fallback locator '{}' failed to resolve.", fallback);
                }
            }

            log.error("SELF-HEALING FAILED: None of the selectors resolved for element.");
            throw new RuntimeException("Element not found using any provided fallback locators: " + String.join(" -> ", selectors));
        }
    }
}
