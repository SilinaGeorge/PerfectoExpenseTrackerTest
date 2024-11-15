package com.perfecto.sampleproject;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.SessionNotCreatedException;;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.appium.java_client.android.AndroidDriver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class LocalAppium {
	RemoteWebDriver driver;

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
		String platformName = System.getProperty("platform", "Android"); // Default to Android if not set
		DesiredCapabilities capabilities = new DesiredCapabilities();

		if (platformName.equalsIgnoreCase("Android")) {
			capabilities.setCapability("platformVersion", "15.0");
			capabilities.setCapability("deviceName", "Android Emulator");
			capabilities.setCapability("platformName", "Android");
			capabilities.setCapability("app", "C:/Users/Sil/Downloads/ExpenseAppVer1.0.apk"); // APK file for Android
			capabilities.setCapability("automationName", "UiAutomator2");
		} else if (platformName.equalsIgnoreCase("iOS")) {
			capabilities.setCapability("platformVersion", "16.0"); // Update for iOS version
			capabilities.setCapability("deviceName", "iPhone 12");
			capabilities.setCapability("platformName", "iOS");
			capabilities.setCapability("app", "C:/path/to/your/ios/app.app"); // .app file for iOS
			capabilities.setCapability("automationName", "XCUITest");
		} else {
			throw new RuntimeException("Unsupported platform: " + platformName);
		}

		try {
			if (platformName.equalsIgnoreCase("Android")) {
				driver = new AndroidDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
			} else if (platformName.equalsIgnoreCase("iOS")) {
				driver = new IOSDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
			}
			driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		} catch (SessionNotCreatedException e) {
			throw new RuntimeException("Driver not created with capabilities: " + capabilities.toString());
		}
	}

	@Test(dataProvider = "loginData")
	public void loginTest(String email, String password, boolean shouldLoginSucceed) throws Exception {

		WebDriverWait wait = new WebDriverWait(driver, 30);

		MobileElement emailField = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
				driver.findElement(By.id("login_email"))));
		emailField.clear();
		emailField.sendKeys(email);

		MobileElement passwordField = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
				driver.findElement(By.id("login_password"))));
		passwordField.clear();
		passwordField.sendKeys(password);

		MobileElement loginButton = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
				driver.findElement(By.id("login_login_btn"))));
		loginButton.click();

		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		if (shouldLoginSucceed) {

			MobileElement listAddButton = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
					driver.findElement(By.id("list_add_btn"))));
			assertTrue(listAddButton.isDisplayed(), "Login failed - add button element not displayed.");

			MobileElement hamburgerMenu = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
					driver.findElement((By.xpath("//*[@content-desc='Open Drawer']")))));
			hamburgerMenu.click();

			MobileElement aboutItem = (MobileElement) driver.findElement(By.xpath("//*[@text='About']"));
			aboutItem.click();

			MobileElement crashButton = (MobileElement) wait.until(ExpectedConditions.elementToBeClickable(
					driver.findElement(By.id("crash_me_button"))));
			crashButton.click();

		} else {
			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			MobileElement snackbar = (MobileElement) driver.findElement(By.id("snackbar_text"));
			assertEquals(snackbar.getText(), "Invalid email or password", "Expected error message not shown.");
		}
	}

	@AfterTest
	public void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}
}
