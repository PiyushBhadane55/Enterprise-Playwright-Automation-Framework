package com.enterprise.utilities;

import com.microsoft.playwright.Page;
import io.qameta.allure.Attachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Screenshot Utility for capturing page views and attaching them to Allure reports.
 */
public class ScreenshotUtil {

    private static final Logger log = LogManager.getLogger(ScreenshotUtil.class);

    /**
     * Captures a screenshot of the current page and saves it to the target directory.
     *
     * @param page           Playwright Page instance
     * @param screenshotName Filename without extension
     * @return Path to the saved screenshot file
     */
    public static String captureScreenshot(Page page, String screenshotName) {
        String path = "target/screenshots/" + screenshotName + "_" + System.currentTimeMillis() + ".png";
        log.info("Capturing screenshot to path: {}", path);
        try {
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(path)).setFullPage(true));
            log.info("Screenshot successfully saved.");
        } catch (Exception e) {
            log.error("Failed to capture screenshot.", e);
        }
        return path;
    }

    /**
     * Captures screenshot and returns it as a byte array.
     */
    public static byte[] captureScreenshotAsBytes(Page page) {
        log.debug("Capturing screenshot as byte array.");
        try {
            return page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        } catch (Exception e) {
            log.error("Failed to capture screenshot as byte array.", e);
            return new byte[0];
        }
    }

    /**
     * Captures a screenshot and attaches it directly to the Allure Report.
     *
     * @param page           Playwright Page instance
     * @param attachmentName Name to display in the Allure Report
     * @return Byte array of the screenshot (required for Allure Attachment)
     */
    @Attachment(value = "{attachmentName}", type = "image/png")
    public static byte[] attachScreenshotToAllure(Page page, String attachmentName) {
        log.info("Attaching screenshot '{}' to Allure report.", attachmentName);
        return captureScreenshotAsBytes(page);
    }
}
