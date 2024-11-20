package com.perfecto.expensetracker;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
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
import static org.testng.Assert.assertTrue;

public class ExpenseTrackerLoginTest {
	RemoteWebDriver driver;
	ReportiumClient reportiumClient;

	private static final String ANDROID_MODEL = "Galaxy S.*|LG.*";
	private static final String IOS_MODEL = "iPhone-15 Plus";
	private static final String PACKAGE_NAME = "io.perfecto.expense.tracker";
	private static final String EXPENSE_APP_REPO_KEY_ANDROID = "PUBLIC:ExpenseTracker/Native/ExpenseAppVer1.0.apk";
	private static final String EXPENSE_APP_REPO_KEY_IOS = "PUBLIC:ExpenseTracker/Native/InvoiceApp1.0.ipa";

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
		String cloudName = "<<cloud name>>"; // -DcloudName=${cloudName}
		String securityToken = "<<security token>>"; // -DsecurityToken=${securityToken}
		if (cloudName == null || securityToken == null) {
			throw new RuntimeException("Perfecto Cloud Name and Security Token must be set as properties.");
		}

		String platformName = System.getProperty("platform", "Android"); // -Dplatform=${Android/iOS}
		cloudName = PerfectoLabUtils.fetchCloudName(cloudName);
		securityToken = PerfectoLabUtils.fetchSecurityToken(securityToken);

		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("securityToken", securityToken);
		capabilities.setCapability("takesScreenshot", false);
		capabilities.setCapability("screenshotOnError", true);

		if (platformName.equalsIgnoreCase("Android")) {
			setupAndroidCapabilities(cloudName, securityToken, capabilities);
			driver = new AndroidDriver<>(new URL("https://" + cloudName + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
		} else if (platformName.equalsIgnoreCase("iOS")) {
			setupIOSCapabilities(cloudName, securityToken, capabilities);
			driver = new IOSDriver<>(new URL("https://" + cloudName + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
		} else {
			throw new RuntimeException("Unsupported platform: " + platformName);
		}

		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
	}

	private void setupAndroidCapabilities(String cloudName, String securityToken, DesiredCapabilities capabilities) throws Exception {
		capabilities.setCapability("model", ANDROID_MODEL);
		capabilities.setCapability("app", EXPENSE_APP_REPO_KEY_ANDROID);
		capabilities.setCapability("appPackage", PACKAGE_NAME);
		capabilities.setCapability("autoLaunch", true);
		capabilities.setCapability("fullReset", true);
		capabilities.setCapability("automationName", "Appium");
		capabilities.setCapability("sensorInstrument", true);
	}

	private void setupIOSCapabilities(String cloudName, String securityToken, DesiredCapabilities capabilities) throws Exception {
		capabilities.setCapability("model", IOS_MODEL);
		capabilities.setCapability("app", EXPENSE_APP_REPO_KEY_IOS);
		capabilities.setCapability("bundleId", PACKAGE_NAME);
		capabilities.setCapability("autoLaunch", true);
		capabilities.setCapability("fullReset", true);
		capabilities.setCapability("iOSResign", true);
		capabilities.setCapability("automationName", "Appium");
		capabilities.setCapability("sensorInstrument", true);
	}

	@Test(dataProvider = "loginData")
	public void loginTest(String email, String password, boolean shouldLoginSucceed) throws Exception {
		WebDriverWait wait = new WebDriverWait(driver, 30);
		reportiumClient = PerfectoLabUtils.setReportiumClient(driver, reportiumClient);
		reportiumClient.testStart("Expense Tracker Login Mobile Test", new TestContext("ios", "android", "login", "expense tracker"));

		performLoginSteps(wait, email, password);

		if (shouldLoginSucceed) {
			handleLoginSuccess(wait);
		} else {
			handleLoginFailure();
		}
	}

	private void performLoginSteps(WebDriverWait wait, String email, String password){
		reportiumClient.stepStart("Enter Email");
		MobileElement emailField = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.id("login_email"))));
		emailField.clear();
		emailField.sendKeys(email);
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Enter Password");
		MobileElement passwordField = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.id("login_password"))));
		passwordField.clear();
		passwordField.sendKeys(password);
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Enable Biometric");
		MobileElement biometricSelect = (MobileElement) wait.until(ExpectedConditions.presenceOfElementLocated(driver instanceof AndroidDriver ? By.id("login_biometric_check_box") : By.xpath("//*[@value='0' or @value='1']")));
		if ((driver instanceof IOSDriver && biometricSelect.getAttribute("value").equals("0")) || (driver instanceof AndroidDriver && biometricSelect.getAttribute("checked").equalsIgnoreCase("false"))) {
			biometricSelect.click();
		}
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Click Login");
		MobileElement loginButton = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(driver instanceof AndroidDriver ? By.id("login_login_btn") : By.xpath("//*[@name='Login']"))));
		loginButton.click();
		reportiumClient.stepEnd();
	}

	private void handleLoginSuccess(WebDriverWait wait) {
		reportiumClient.stepStart("Biometric Authenticate");
		Map<String, Object> params = new HashMap<>();
		params.put("identifier", PACKAGE_NAME);
		params.put("resultAuth", "success");
		params.put("errorType", "authFailed");
		driver.executeScript("mobile:sensorAuthentication:set", params);
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Login Successful");
		MobileElement listAddButton = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.id("list_add_btn"))));
		assertTrue(listAddButton.isDisplayed(), "Login failed - add button element not displayed.");
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Click Hamburger Menu");
		MobileElement hamburgerMenu = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(driver instanceof AndroidDriver ? By.xpath("//*[@content-desc='Open Drawer']") : By.id("list_left_menu_btn"))));
		hamburgerMenu.click();
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Click About Page Menu Item");
		MobileElement aboutItem = (MobileElement) driver.findElement(By.xpath(driver instanceof AndroidDriver ? "//*[@text='About']" : "//*[@name='list_about_menu']"));
		aboutItem.click();
		reportiumClient.stepEnd();

		reportiumClient.stepStart("Click Crash Me");
		MobileElement crashButton = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(driver instanceof AndroidDriver ? By.id("crash_me_button") : By.xpath("//*[@name='Crash Me']"))));
		crashButton.click();
		reportiumClient.stepEnd();
	}

	private void handleLoginFailure() {
		reportiumClient.stepStart("Login Invalid");
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		if (driver instanceof AndroidDriver) {
			driver.findElement(By.id("snackbar_text"));
		}
		else{
			driver.findElement(By.xpath("//*[@label='Invalid email or password']"));
			MobileElement okButton = (MobileElement) driver.findElement(By.xpath("//XCUIElementTypeAlert/XCUIElementTypeOther[1]/XCUIElementTypeOther[1]/XCUIElementTypeOther[2]/XCUIElementTypeScrollView[2]"));
			okButton.click();
		}
		reportiumClient.stepEnd();
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

