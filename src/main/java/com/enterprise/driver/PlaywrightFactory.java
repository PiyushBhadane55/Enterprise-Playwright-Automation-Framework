package com.enterprise.driver;

import com.enterprise.config.ConfigManager;
import com.microsoft.playwright.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Playwright Driver Factory managing ThreadLocal instances of Playwright components.
 * Supports cross-browser testing and parallel execution out of the box.
 */
public class PlaywrightFactory {

    private static final Logger log = LogManager.getLogger(PlaywrightFactory.class);

    // ThreadLocal instances to ensure thread safety during parallel execution
    private static final ThreadLocal<Playwright> tlPlaywright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> tlBrowser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> tlContext = new ThreadLocal<>();
    private static final ThreadLocal<Page> tlPage = new ThreadLocal<>();

    /**
     * Initializes the Browser, Context, and Page instances for the current thread.
     *
     * @param browserName Name of the browser (chrome, firefox, edge, webkit)
     * @return Initialized Page instance
     */
    public static synchronized Page initDriver(String browserName) {
        log.info("Initializing Playwright driver for browser: {}", browserName);

        // 1. Initialize Playwright
        Playwright playwright = Playwright.create();
        tlPlaywright.set(playwright);

        // 2. Set Headless mode based on configuration
        boolean isHeadless = ConfigManager.getBoolean("browser.headless");
        log.info("Browser Headless Mode: {}", isHeadless);

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless)
                .setSlowMo(ConfigManager.getInt("browser.slowmo", 0));

        Browser browser;
        String targetBrowser = browserName.toLowerCase().trim();

        // 3. Launch specific Browser
        switch (targetBrowser) {
            case "chrome":
                launchOptions.setChannel("chrome");
                browser = playwright.chromium().launch(launchOptions);
                break;
            case "edge":
                launchOptions.setChannel("msedge");
                browser = playwright.chromium().launch(launchOptions);
                break;
            case "firefox":
                browser = playwright.firefox().launch(launchOptions);
                break;
            case "webkit":
                browser = playwright.webkit().launch(launchOptions);
                break;
            default:
                log.warn("Invalid browser name '{}' specified. Defaulting to Chromium.", browserName);
                browser = playwright.chromium().launch(launchOptions);
                break;
        }
        tlBrowser.set(browser);

        // 4. Create Browser Context with video, tracing, and viewport size configurations
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
        
        // Setup Viewport
        int width = ConfigManager.getInt("browser.viewport.width", 1280);
        int height = ConfigManager.getInt("browser.viewport.height", 720);
        contextOptions.setViewportSize(width, height);

        // Setup Video Recording if enabled
        if (ConfigManager.getBoolean("browser.video.enabled")) {
            String videoPath = ConfigManager.get("browser.video.path", "target/videos/");
            contextOptions.setRecordVideoDir(Paths.get(videoPath));
            log.info("Video recording enabled. Path: {}", videoPath);
        }

        BrowserContext context = browser.newContext(contextOptions);
        tlContext.set(context);

        // Setup Tracing if enabled
        if (ConfigManager.getBoolean("browser.tracing.enabled")) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
            log.info("Playwright Tracing started.");
        }

        // 5. Create Page
        Page page = context.newPage();
        tlPage.set(page);

        log.info("Driver successfully initialized for thread: {}", Thread.currentThread().threadId());
        return page;
    }

    public static Playwright getPlaywright() {
        return tlPlaywright.get();
    }

    public static Browser getBrowser() {
        return tlBrowser.get();
    }

    public static BrowserContext getContext() {
        return tlContext.get();
    }

    public static Page getPage() {
        return tlPage.get();
    }

    /**
     * Cleans up all Playwright driver instances for the current thread.
     */
    public static void quitDriver() {
        log.info("Closing Playwright driver for thread: {}", Thread.currentThread().threadId());

        BrowserContext context = tlContext.get();
        if (context != null) {
            // Save tracing if enabled
            if (ConfigManager.getBoolean("browser.tracing.enabled")) {
                String tracePath = ConfigManager.get("browser.tracing.path", "target/traces/");
                String traceFileName = tracePath + "trace_" + Thread.currentThread().threadId() + ".zip";
                try {
                    context.tracing().stop(new Tracing.StopOptions()
                            .setPath(Paths.get(traceFileName)));
                    log.info("Saved browser trace to: {}", traceFileName);
                } catch (Exception e) {
                    log.error("Failed to save Playwright trace file.", e);
                }
            }
            context.close();
        }

        Browser browser = tlBrowser.get();
        if (browser != null) {
            browser.close();
        }

        Playwright playwright = tlPlaywright.get();
        if (playwright != null) {
            playwright.close();
        }

        // Clean ThreadLocals
        tlPage.remove();
        tlContext.remove();
        tlBrowser.remove();
        tlPlaywright.remove();

        log.info("Driver cleanup completed for thread: {}", Thread.currentThread().threadId());
    }
}
