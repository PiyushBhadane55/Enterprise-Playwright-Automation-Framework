# Enterprise Playwright Automation Framework

A production-ready, highly extensible, enterprise-grade test automation framework built using **Java 21**, **Playwright (Java)**, **REST Assured**, **PostgreSQL**, and **TestNG**.

This framework is built in **six distinct phases** and is fully self-contained for local, dockerized, and CI/CD executions.

---

## 🛠️ Technology Stack

| Category | Technology |
|---|---|
| **Language** | Java 21 |
| **Build Tool** | Maven |
| **UI Testing** | Playwright (Java) |
| **API Testing** | REST Assured |
| **Database Validation** | PostgreSQL (JDBC + HikariCP Connection Pool) |
| **Database Migrations** | Flyway |
| **Test Runner** | TestNG |
| **Reporting** | Allure Reports |
| **Logging** | Log4j2 |
| **CI/CD** | Jenkins / GitHub Actions |
| **Email Validation** | MailHog (SMTP REST API Integration) |
| **PDF Validation** | Apache PDFBox |
| **Assertions** | AssertJ / TestNG Assertions |

---

## 📂 Project Structure

```text
EnterprisePlaywrightFramework
│
├── .github/workflows
│     └── maven.yml          # GitHub Actions workflow
│
├── src
│   ├── main
│   │   ├── java
│   │   │     ├── config/ConfigManager.java         # Environment property manager
│   │   │     ├── driver/PlaywrightFactory.java     # ThreadLocal driver factory
│   │   │     ├── database/DatabaseManager.java     # Database & HikariCP pool
│   │   │     ├── api/ApiClient.java                # REST Assured client
│   │   │     ├── pages/                            # Page Object Models
│   │   │     │     ├── BasePage.java               # Self-Healing base page
│   │   │     │     ├── LoginPage.java
│   │   │     │     ├── DashboardPage.java
│   │   │     │     └── CustomerPage.java
│   │   │     ├── utilities/                        # Framework helpers
│   │   │     │     ├── WaitUtil.java
│   │   │     │     ├── ScreenshotUtil.java
│   │   │     │     ├── JsonUtil.java
│   │   │     │     ├── ExcelUtil.java
│   │   │     │     └── PdfUtil.java
│   │   │     ├── listeners/                        # TestNG listeners
│   │   │     │     ├── TestListener.java           # Failure screenshot capturing
│   │   │     │     ├── RetryAnalyzer.java          # Flaky tests retrying
│   │   │     │     └── AnnotationTransformer.java  # Global retry binding
│   │   │     └── services/
│   │   │           └── EmailService.java           # SMTP MailHog OTP handler
│   │   │
│   │   └── resources
│   │         ├── config/                           # Environment property files
│   │         │     ├── dev.properties
│   │         │     ├── qa.properties
│   │         │     ├── uat.properties
│   │         │     └── prod.properties
│   │         ├── db/migration/V1__init_schema.sql  # Flyway schema migrations
│   │         ├── log4j2.xml                        # Console/file logging config
│   │         ├── testng.xml                        # Parallel execution suites
│   │         └── allure.properties                 # Allure folder output path
│   │
│   └── test
│       └── java
│             └── com/enterprise/tests/
│                   ├── BaseTest.java               # Test startup/cleanup base
│                   ├── ui/                         # UI test cases
│                   │     ├── SauceDemoTest.java
│                   │     └── OrangeHrmTest.java
│                   ├── api/CustomerApiTest.java    # API verification scripts
│                   ├── db/CustomerDatabaseTest.java# Database verification scripts
│                   └── integration/
│                         └── E2eCustomerWorkflowTest.java # Full integrated test
│
├── Jenkinsfile             # Jenkins CI/CD pipeline
└── pom.xml                 # Maven dependencies configurations
```

---

## 🚀 Getting Started

### 📋 Prerequisites
1. **Java 21 JDK** installed.
2. **Maven 3.x** installed.
3. **Local PostgreSQL DB & MailHog** (installed and running locally for DB/Email tests).

### 1. Compile the Project
To compile code and download dependencies:
```bash
mvn clean test-compile
```

### 2. Execution via Command Line
Run all test suites defined in `testng.xml` under the **QA** environment using **Chrome**:
```bash
mvn test -Denv=qa -Dbrowser=chrome
```

To run under **UAT** environment with **Firefox**:
```bash
mvn test -Denv=uat -Dbrowser=firefox
```

#### System Properties Support:
*   `-Denv`: Switches configuration files (defaults to `qa`).
*   `-Dbrowser`: Sets browser type (`chrome`, `firefox`, `edge`, `webkit`).
*   `-Dbrowser.headless`: Overrides headless mode (`true` / `false`).



## 📊 Generating Allure Reports

After running the tests, Allure results are stored in `target/allure-results/`.

### 1. Install Allure Commandline
Ensure Allure CLI is installed (via `brew install allure`, `npm install -g allure-commandline`, or download manually).

### 2. Generate and View Report
Run the following command to compile results and launch a local web server displaying the interactive report:
```bash
allure serve target/allure-results
```

---

## ⚡ Key Framework Features Details

### 🔄 Dynamic Selector Self-Healing
If an element selector changes in production, the framework uses the fallback chain configured in the Page Object Model (POM):
```java
// BasePage.java
Locator element = findElementWithFallback("#user-name", "input[placeholder='Username']", "//input[@id='user-name']");
```
It tests selectors sequentially using a 1-second timeout. If the primary fails but a fallback succeeds, it logs the event and keeps the execution running without throwing a NoSuchElementException.

### 🔁 Flaky Test Recovery
The framework integrates a global `RetryAnalyzer` which runs failed tests up to 3 times automatically. It is bound globally at runtime using the `AnnotationTransformer` listener, removing the need to annotate every test method manually.

### 📩 Email OTP Parsing
The `EmailService` interfaces with the local MailHog REST API `/api/v2/messages`, filters inbox entries by recipient email, and extracts OTPs using Regex:
```java
String body = EmailService.getLatestEmailBody("user@example.com");
String otp = EmailService.extractOtp(body); // Finds 6-digit codes
```

### 📄 PDF Document Checking
Using Apache PDFBox, the framework can verify the content of downloaded reports:
```java
String pdfText = PdfUtil.getPdfText("target/downloaded_report.pdf");
assertThat(pdfText).contains("Customer Name: John Doe");
```
