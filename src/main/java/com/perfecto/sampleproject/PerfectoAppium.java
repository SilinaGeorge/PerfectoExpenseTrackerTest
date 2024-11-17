package com.perfecto.sampleproject;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;

import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResult;
import com.perfecto.reportium.test.result.TestResultFactory;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class PerfectoAppium {
	RemoteWebDriver driver;
	ReportiumClient reportiumClient;


	@DataProvider(name = "loginData")
	public Object[][] credentials() {
		return new Object[][] {
				{"testFailure1@perfecto.com", "wrongpassword1", false},
				{"testFailure2@perfecto.com", "wrongpassword2", false},
				{"test@perfecto.com", "test123", true}
		};
	}

	@BeforeTest
	public void setUp() throws Exception {

		// Replace <<cloud name>> with your perfecto cloud name (e.g. demo) or pass it as maven properties: -DcloudName=<<cloud name>>
		String cloudName = "<<cloud name>>";

		// Replace <<security token>> with your perfecto security token or pass it as maven properties: -DsecurityToken=<<SECURITY TOKEN>>  More info: https://developers.perfectomobile.com/display/PD/Generate+security+tokens
		String securityToken = "<<security token>>";
		String platformName = System.getProperty("platform", "Android");
		cloudName = PerfectoLabUtils.fetchCloudName(cloudName);
		securityToken = PerfectoLabUtils.fetchSecurityToken(securityToken);
		String browserName = "mobileOS";

		DesiredCapabilities capabilities = new DesiredCapabilities(browserName, "", Platform.ANY);
		if (platformName.equalsIgnoreCase("Android")) {
			String repositoryKey = "PUBLIC:ExpenseTracker/Native/ExpenseAppVer1.0.apk";
			String localFilePath = System.getProperty("user.dir") + "//libs//ExpenseAppVer1.0.apk";
			// Uploads local apk file to Media repository
			PerfectoLabUtils.uploadMedia(cloudName, securityToken, localFilePath, repositoryKey);

			capabilities.setCapability("model", "Galaxy S.*|LG.*");
			capabilities.setCapability("enableAppiumBehavior", true);
			capabilities.setCapability("autoLaunch",true);
			capabilities.setCapability("fullReset",true);
			capabilities.setCapability("app", repositoryKey); // Set Perfecto Media repository path of App under test.
			capabilities.setCapability("appPackage", "io.perfecto.expense.tracker"); // Set the unique identifier of your app
			capabilities.setCapability("takesScreenshot", false);
			capabilities.setCapability("screenshotOnError", true); // Take screenshot only on errors
			capabilities.setCapability("automationName", "Appium");
			capabilities.setCapability("securityToken", securityToken);
			capabilities.setCapability("sensorInstrument", true);

		} else if (platformName.equalsIgnoreCase("iOS")) {
			String repositoryKey = "PUBLIC:ExpenseTracker/Native/InvoiceApp1.0.ipa";
			String localFilePath = System.getProperty("user.dir") + "//libs//InvoiceApp1.0.ipa";
			PerfectoLabUtils.uploadMedia(cloudName, securityToken, localFilePath, repositoryKey);

			capabilities.setCapability("autoLaunch",true);
			capabilities.setCapability("fullReset",true);
			capabilities.setCapability("iOSResign",true);
			capabilities.setCapability("model", "iPhone-15 Plus");
			capabilities.setCapability("bundleId", "io.perfecto.expense.tracker"); // Set the unique identifier of your app
			capabilities.setCapability("app", repositoryKey); // Set Perfecto Media repository path of App under test.
			capabilities.setCapability("automationName", "Appium");
			capabilities.setCapability("enableAppiumBehavior", true);
			capabilities.setCapability("iOSResign",true);
			capabilities.setCapability("sensorInstrument", true);
			capabilities.setCapability("takesScreenshot", false);
			capabilities.setCapability("screenshotOnError", true); // Take screenshot only on errors
			capabilities.setCapability("securityToken", securityToken);

		} else {
			throw new RuntimeException("Unsupported platform: " + platformName);
		}

		try {
			if (platformName.equalsIgnoreCase("Android")) {
				driver = new AndroidDriver<AndroidElement>(new URL("https://" + cloudName  + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
			} else if (platformName.equalsIgnoreCase("iOS")) {
				driver = new IOSDriver<>(new URL("https://" + cloudName  + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
			}
			driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		} catch (SessionNotCreatedException e) {
			throw new RuntimeException("Driver not created with capabilities: " + capabilities.toString());
		}
	}

	@Test(dataProvider = "loginData")
	public void loginTest(String email, String password, boolean shouldLoginSucceed) throws Exception {
		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

		WebDriverWait wait = new WebDriverWait(driver, 30);
		reportiumClient = PerfectoLabUtils.setReportiumClient(driver, reportiumClient); //Creates reportiumClient
		reportiumClient.testStart("Expense Tracker Login Mobile Test", new TestContext("ios", "android", "login"));

		reportiumClient.stepStart("Enter Email");
		MobileElement emailField = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
				driver.findElement(By.id("login_email"))));
		emailField.clear();
		emailField.sendKeys(email);
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Enter Password");
		MobileElement passwordField = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
				driver.findElement(By.id("login_password"))));
		passwordField.clear();
		passwordField.sendKeys(password);
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Enable Biometric");
		MobileElement biometricSelect = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
				driver.findElement(driver instanceof AndroidDriver? By.id("login_biometric_check_box"): By.xpath(
						"//*[@value]"))));
		if(!biometricSelect.isSelected()) {
			biometricSelect.click();
		}
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Click Login");
		MobileElement loginButton = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
				driver.findElement(driver instanceof AndroidDriver?By.id("login_login_btn"):By.xpath("//*[@name='Login']"))));
		loginButton.click();
		reportiumClient.stepEnd();

		if (shouldLoginSucceed) {
			reportiumClient.stepStart("Biometric Auth");
			Map<String, Object> params = new HashMap<>();
			params.put("identifier", "io.perfecto.expense.tracker");
			params.put("resultAuth", "success");
			params.put("errorType", "authFailed");
			driver.executeScript("mobile:sensorAuthentication:set", params);
			reportiumClient.stepEnd();

			reportiumClient.stepStart("Login Successful");
			MobileElement listAddButton = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
					driver.findElement(By.id("list_add_btn"))));
			assertTrue(listAddButton.isDisplayed(), "Login failed - add button element not displayed.");
			reportiumClient.stepEnd();

			reportiumClient.stepStart("Click Hamburger Menu");
			MobileElement hamburgerMenu = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
						driver.findElement(driver instanceof AndroidDriver?By.xpath("//*[@content-desc='Open Drawer']"): By.id("list_left_menu_btn"))));
			hamburgerMenu.click();
			reportiumClient.stepEnd();

			reportiumClient.stepStart("Click About Page Menu Item");
			MobileElement aboutItem = (MobileElement) driver.findElement(By.xpath(driver instanceof AndroidDriver?"//*[@text='About']":"//*[@name='list_about_menu']"));
			aboutItem.click();
			reportiumClient.stepEnd();

			reportiumClient.stepStart("Click Crash Me");
			MobileElement crashButton = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
					driver.findElement(driver instanceof AndroidDriver?By.id("crash_me_button"): By.xpath("//*[@name='Crash Me']"))));
			crashButton.click();
			reportiumClient.stepEnd();

		} else {
			reportiumClient.stepStart("Login Invalid");
			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			MobileElement invalidLoginElement;
			MobileElement okButton = null;
			if (driver instanceof AndroidDriver) {
				invalidLoginElement = (MobileElement) driver.findElement(By.id("snackbar_text"));
			}
			else{
				invalidLoginElement = (MobileElement) driver.findElement(By.xpath("//*[@label='Invalid email or password']"));
				okButton = (MobileElement) driver.findElement(By.xpath("//XCUIElementTypeAlert/XCUIElementTypeOther[1]/XCUIElementTypeOther[1]/XCUIElementTypeOther[2]/XCUIElementTypeScrollView[2]"));
			}
			assertEquals(invalidLoginElement.getText(), "Invalid email or password", "Expected error message not shown.");
			if(okButton != null){
				okButton.click();
			}
			reportiumClient.stepEnd();
		}

	}

	@AfterMethod
	public void afterMethod(ITestResult result) {
		TestResult testResult = null;
		if(result.getStatus() == result.SUCCESS) {
			testResult = TestResultFactory.createSuccess();
		}
		else if (result.getStatus() == result.FAILURE) {
			testResult = TestResultFactory.createFailure(result.getThrowable());
		}
		reportiumClient.testStop(testResult);

		// Retrieve the URL to the DigitalZoom Report 
		String reportURL = reportiumClient.getReportUrl();
		System.out.println(reportURL);
	}

	@AfterTest
	public void tearDown() {
		if (driver != null) {
			driver.close();
			driver.quit();
		}
	}

}

