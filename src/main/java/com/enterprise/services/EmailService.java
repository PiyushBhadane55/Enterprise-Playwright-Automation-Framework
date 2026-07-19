package com.enterprise.services;

import com.enterprise.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to interact with the local SMTP MailHog REST API for email validation and OTP extraction.
 */
public class EmailService {

    private static final Logger log = LogManager.getLogger(EmailService.class);

    private static String getMailHogBaseUri() {
        String host = ConfigManager.get("smtp.host", "localhost");
        int port = ConfigManager.getInt("smtp.inbox.port", 8025);
        return "http://" + host + ":" + port;
    }

    /**
     * Retrieves the text body of the latest email received for a specific recipient.
     *
     * @param recipientEmail The recipient's email address
     * @return Email body, or null if no email is found
     */
    public static String getLatestEmailBody(String recipientEmail) {
        String baseUri = getMailHogBaseUri();
        log.info("Fetching latest emails from MailHog at: {}", baseUri);

        try {
            Response response = RestAssured.given()
                    .baseUri(baseUri)
                    .get("/api/v2/messages");

            if (response.getStatusCode() != 200) {
                log.error("Failed to connect to MailHog. Status: {}", response.getStatusCode());
                return null;
            }

            // Extract all messages
            List<Map<String, Object>> messages = response.jsonPath().getList("items");
            if (messages == null || messages.isEmpty()) {
                log.warn("No emails found in MailHog inbox.");
                return null;
            }

            log.info("Total emails in MailHog: {}. Filtering for recipient: {}", messages.size(), recipientEmail);

            for (Map<String, Object> msg : messages) {
                // Parse recipients
                List<Map<String, String>> toList = (List<Map<String, String>>) msg.get("To");
                if (toList == null) continue;

                boolean match = toList.stream().anyMatch(to -> {
                    String mailbox = to.get("Mailbox");
                    String domain = to.get("Domain");
                    String fullAddress = mailbox + "@" + domain;
                    return fullAddress.equalsIgnoreCase(recipientEmail);
                });

                if (match) {
                    Map<String, Object> content = (Map<String, Object>) msg.get("Content");
                    if (content != null) {
                        String body = (String) content.get("Body");
                        log.info("Matching email found. Body content retrieved.");
                        return body;
                    }
                }
            }

            log.warn("No email found for recipient: {}", recipientEmail);

        } catch (Exception e) {
            log.error("Failed to retrieve emails from MailHog API.", e);
        }

        return null;
    }

    /**
     * Extracts a 6-digit OTP code from an email body string.
     *
     * @param emailBody The body content of the email
     * @return 6-digit OTP, or null if not found
     */
    public static String extractOtp(String emailBody) {
        if (emailBody == null || emailBody.isEmpty()) {
            return null;
        }
        // Match a standard 6-digit verification code
        Pattern pattern = Pattern.compile("\\b\\d{6}\\b");
        Matcher matcher = pattern.matcher(emailBody);
        if (matcher.find()) {
            String otp = matcher.group();
            log.info("Successfully extracted OTP: {}", otp);
            return otp;
        }
        log.warn("No 6-digit OTP code found in email body.");
        return null;
    }

    /**
     * Deletes all messages in the MailHog inbox to keep the workspace clean.
     */
    public static void clearInbox() {
        String baseUri = getMailHogBaseUri();
        log.info("Clearing MailHog inbox...");
        try {
            Response response = RestAssured.given()
                    .baseUri(baseUri)
                    .delete("/api/v2/messages");
            if (response.getStatusCode() == 200) {
                log.info("MailHog inbox successfully cleared.");
            } else {
                log.warn("Failed to clear MailHog inbox. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to clear MailHog inbox.", e);
        }
    }
}
