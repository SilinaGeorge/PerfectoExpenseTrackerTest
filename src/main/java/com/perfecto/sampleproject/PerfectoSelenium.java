package com.perfecto.sampleproject;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResult;
import com.perfecto.reportium.test.result.TestResultFactory;

public class PerfectoSelenium {
	RemoteWebDriver driver;
	ReportiumClient reportiumClient;
	// Replace <<cloud name>> with your perfecto cloud name (e.g. testingcloud ) or
	// pass it as maven properties: -DcloudName=<<cloud name>>
	String cloudName = "demo";

	// Replace <<security token>> with your perfecto security token or pass it as
	// maven properties: -DsecurityToken=<<SECURITY TOKEN>> More info:
	// https://developers.perfectomobile.com/display/PD/Generate+security+tokens
	String securityToken = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4YmI4YmZmZS1kMzBjLTQ2MjctYmMxMS0zNTYyMmY1ZDkyMGYifQ.eyJpYXQiOjE3MzE2MjgyMjksImp0aSI6IjJjYTc2ZjY3LTJkYWQtNDM5MS04ZGIzLWU5MDM5YTAzNDEyMSIsImlzcyI6Imh0dHBzOi8vYXV0aC5wZXJmZWN0b21vYmlsZS5jb20vYXV0aC9yZWFsbXMvZGVtby1wZXJmZWN0b21vYmlsZS1jb20iLCJhdWQiOiJodHRwczovL2F1dGgucGVyZmVjdG9tb2JpbGUuY29tL2F1dGgvcmVhbG1zL2RlbW8tcGVyZmVjdG9tb2JpbGUtY29tIiwic3ViIjoiNWM2N2JmZGYtNzRkMi00NWUwLThkZjktOTMwNmUwZWNiMTg3IiwidHlwIjoiT2ZmbGluZSIsImF6cCI6Im9mZmxpbmUtdG9rZW4tZ2VuZXJhdG9yIiwibm9uY2UiOiI0ODg0MjhiOC0xYmU5LTRmNjAtYmU5MC0zN2I1MmNkYmQyZDEiLCJzZXNzaW9uX3N0YXRlIjoiNDE2ZTc5YTctMzc1NS00NzcyLTljNjItMzVhNjFkMjQwYWYxIiwic2NvcGUiOiJvcGVuaWQgb2ZmbGluZV9hY2Nlc3MiLCJzaWQiOiI0MTZlNzlhNy0zNzU1LTQ3NzItOWM2Mi0zNWE2MWQyNDBhZjEifQ.x24ART9UI3AcudDoVTzev-vD3w3v4Y5WSuS42g3CwiY";

	@Test
	public void androidTest() throws Exception {
		// Mobile: Auto generate capabilities for device selection:
		// https://developers.perfectomobile.com/display/PD/Select+a+device+for+manual+testing#Selectadeviceformanualtesting-genCapGeneratecapabilities
		String browserName = "mobileOS";

		// Perfecto Media repository path
		String repositoryKey = "PUBLIC:ExpenseTracker/Native/ExpenseAppVer1.0.apk";
		// Local apk/ipa file path
		String localFilePath = System.getProperty("user.dir") + "//libs//ExpenseAppVer1.0.apk";
		// Uploads local apk file to Media repository
		PerfectoLabUtils.uploadMedia(cloudName, securityToken, localFilePath, repositoryKey);

		DesiredCapabilities capabilities = new DesiredCapabilities(browserName, "", Platform.ANY);
		capabilities.setCapability("platformName", "Android");
		capabilities.setCapability("useAppiumForWeb", true);
		capabilities.setCapability("openDeviceTimeout", 2);
		capabilities.setCapability("automationName", "Appium");
		capabilities.setCapability("appPackage", "io.perfecto.expense.tracker"); // Set the unique identifier of your app

		capabilities.setCapability("app", repositoryKey);
		// The below capability is mandatory. Please do not replace it.
		capabilities.setCapability("securityToken", PerfectoLabUtils.fetchSecurityToken(securityToken));

		driver = new RemoteWebDriver(new URL("https://" + PerfectoLabUtils.fetchCloudName(cloudName)
				+ ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);

		reportiumClient = PerfectoLabUtils.setReportiumClient(driver, reportiumClient); // Creates reportiumClient
		reportiumClient.testStart("Perfecto Expense Tracker login test", new TestContext("tag2", "tag3"));

		reportiumClient.stepStart("Enter email");
		WebDriverWait wait = new WebDriverWait(driver, 30);
		WebElement  email =  wait.until(ExpectedConditions.elementToBeClickable(
				driver.findElement(By.id("login_email"))));
		email.sendKeys("testFailure@perfecto.com");
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Enter password");
		WebElement  password = wait.until(ExpectedConditions.elementToBeClickable(
				driver.findElement(By.id("login_password"))));
		password.sendKeys("test123");
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Click login");
		WebElement  login =  wait.until(ExpectedConditions.elementToBeClickable(
				driver.findElement(By.id("login_login_btn"))));
		login.click();
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Login Successful");
		wait.until(ExpectedConditions.elementToBeClickable(
				driver.findElement(By.id("list_add_btn"))));
		reportiumClient.stepEnd();

//		reportiumClient.stepStart("Verify title");
//		String aTitle = driver.getTitle();
//		PerfectoLabUtils.assertTitle(aTitle, reportiumClient); // compare the actual title with the expected title
//		reportiumClient.stepEnd();
	}

	@Test
	@Ignore
	public void iOSTest() throws Exception {
		// Mobile: Auto generate capabilities for device selection:
		// https://developers.perfectomobile.com/display/PD/Select+a+device+for+manual+testing#Selectadeviceformanualtesting-genCapGeneratecapabilities
		// browserName should be set to safari by default to open safari browser.
		String browserName = "safari";
		String ipaRepoPath = "PUBLIC:ExpenseTracker/Native/InvoiceApp1.0.ipa";
		DesiredCapabilities capabilities = new DesiredCapabilities(browserName, "", Platform.ANY);
		capabilities.setCapability("platformName", "iOS");
		capabilities.setCapability("useAppiumForWeb", true);
		capabilities.setCapability("model", "iPhone.*");
		capabilities.setCapability("openDeviceTimeout", 2);
		capabilities.setCapability("automationName", "Appium");
		// The below capability is mandatory. Please do not replace it.
		capabilities.setCapability("securityToken", PerfectoLabUtils.fetchSecurityToken(securityToken));

		driver = new RemoteWebDriver(new URL("https://" + PerfectoLabUtils.fetchCloudName(cloudName)
				+ ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);

		reportiumClient = PerfectoLabUtils.setReportiumClient(driver, reportiumClient); // Creates reportiumClient
		reportiumClient.testStart("Perfecto iOS mobile web test", new TestContext("tag2", "tag3")); 
		reportiumClient.stepStart("browser navigate to perfecto"); // Starts a reportium step
		driver.get("https://www.google.com");
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Verify title");
		String aTitle = driver.getTitle();
		PerfectoLabUtils.assertTitle(aTitle, reportiumClient); // compare the actual title with the expected title
		reportiumClient.stepEnd();
	}

	@Test
	@Ignore
	public void webTest() throws Exception {
		// Web: Make sure to Auto generate capabilities for device selection:
		// https://developers.perfectomobile.com/display/PD/Select+a+device+for+manual+testing#Selectadeviceformanualtesting-genCapGeneratecapabilities
		DesiredCapabilities capabilities = new DesiredCapabilities("", "", Platform.ANY);
		capabilities.setCapability("platformName", "Windows");
		capabilities.setCapability("platformVersion", "11");
		capabilities.setCapability("browserName", "Chrome");
		capabilities.setCapability("browserVersion", "beta");
		capabilities.setCapability("location", "US East");
		capabilities.setCapability("resolution", "1920x1080");

		// The below capability is mandatory. Please do not replace it.
		capabilities.setCapability("securityToken", PerfectoLabUtils.fetchSecurityToken(securityToken));

		driver = new RemoteWebDriver(new URL("https://" + PerfectoLabUtils.fetchCloudName(cloudName)
				+ ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);

		reportiumClient = PerfectoLabUtils.setReportiumClient(driver, reportiumClient); // Creates reportiumClient
		reportiumClient.testStart("Perfecto desktop web test", new TestContext("tag2", "tag3")); 
		reportiumClient.stepStart("browser navigate to perfecto"); // Starts a reportium step
		driver.get("https://www.google.com");
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Verify title");
		String aTitle = driver.getTitle();
		PerfectoLabUtils.assertTitle(aTitle, reportiumClient); // compare the actual title with the expected title
		reportiumClient.stepEnd();
	}

	@AfterMethod
	public void afterMethod(ITestResult result) {
		// STOP TEST
		TestResult testResult = null;

		if (result.getStatus() == result.SUCCESS) {
			testResult = TestResultFactory.createSuccess();
		} else if (result.getStatus() == result.FAILURE) {
			testResult = TestResultFactory.createFailure(result.getThrowable());
		}
		reportiumClient.testStop(testResult);
		try {
			driver.close();
		} catch (Exception e) {
		}
		driver.quit();
		// Retrieve the URL to the DigitalZoom Report
		String reportURL = reportiumClient.getReportUrl();
		System.out.println(reportURL);
	}
}
