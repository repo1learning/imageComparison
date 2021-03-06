package com.imageUtility;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class CompareClass {
	static BufferedImage biA;
	static BufferedImage biB;
	static String actualImage, expectedImage;
	static Boolean OutcomeResult;
	static Workbook workbook = null;
	static float percentage = 0;
	private static String excelSheetPath = System.getProperty("user.home") + "/Desktop/ImageComparisonUtility/Data.xls";

	static WebDriver driver = null;

	public static HSSFSheet getExcelSheet(String sheetName) {
		try {
			FileInputStream inputStream = new FileInputStream(new File(excelSheetPath));
			workbook = new HSSFWorkbook(inputStream);
			HSSFSheet sheet = (HSSFSheet) workbook.getSheet(sheetName);

			return sheet;
		} catch (IOException e) {
		}
		return null;
	}

	public static String readFromExcel(String sheetName, String Key) {

		ArrayList<String> keys = new ArrayList<String>();
		ArrayList<String> Values = new ArrayList<String>();
		HashMap<String, String> keyValues = new HashMap<String, String>();
		HSSFSheet readFromSheet = getExcelSheet(sheetName);

		for (int i = 0; i <= readFromSheet.getRow(0).getLastCellNum(); i++) {
			String key;
			String value;
			DataFormatter formatter = new DataFormatter();
			key = formatter.formatCellValue(readFromSheet.getRow(i).getCell(0));
			value = formatter.formatCellValue(readFromSheet.getRow(i).getCell(1));
			keys.add(key);
			Values.add(value);
			keyValues.put(keys.get(i), Values.get(i));
		}
		return keyValues.get(Key);
	}

	public static void writeToExcel(String sheetName, String testCaseId, String result, String location,
			String expectedImageLocation, String actualImageLocation, float compareImagePercentage) {
		HSSFSheet sheetTowrite = getExcelSheet(sheetName);
		sheetTowrite.createRow(sheetTowrite.getLastRowNum() + 1);
		sheetTowrite.getRow(sheetTowrite.getLastRowNum()).createCell(0).setCellValue(testCaseId);
		sheetTowrite.getRow(sheetTowrite.getLastRowNum()).createCell(1).setCellValue(result);
		sheetTowrite.getRow(sheetTowrite.getLastRowNum()).createCell(2).setCellValue(location);
		sheetTowrite.getRow(sheetTowrite.getLastRowNum()).createCell(3).setCellValue(actualImageLocation);
		sheetTowrite.getRow(sheetTowrite.getLastRowNum()).createCell(4).setCellValue(expectedImageLocation);
		sheetTowrite.getRow(sheetTowrite.getLastRowNum()).createCell(5).setCellValue(compareImagePercentage);

		try {
			FileOutputStream fileOut = new FileOutputStream(excelSheetPath);
			workbook.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getTimeStamp() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public static void takeScreenShot(Object driver, String name) {
		File file = null;

		if (readFromExcel("Browser", "Browser").equalsIgnoreCase("Chrome")) {
			try {

				file = ((ChromeDriverEx) driver).getFullScreenshotAs(OutputType.FILE);
			} catch (Exception e1) {

				e1.printStackTrace();
			}
		} else {
			file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		}
		try {
			FileUtils.copyFile(file, new File(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean compareImage(String actualImage, String expectedImage) {
		try {

			int count = 0;
			File f1 = new File(actualImage);
			File f2 = new File(expectedImage);
			biA = ImageIO.read(f1);
			DataBuffer dbA = biA.getData().getDataBuffer();
			int sizeA = dbA.getSize();
			biB = ImageIO.read(f2);
			DataBuffer dbB = biB.getData().getDataBuffer();
			int sizeB = dbB.getSize();
			if (sizeA == sizeB) {
				for (int i = 0; i < sizeA; i++) {
					if (dbA.getElem(i) == dbB.getElem(i)) {
						count = count + 1;
					}
				}
				percentage = (count * 100) / sizeA;

			}
			if (sizeA == sizeB) {
				for (int i = 0; i < sizeA; i++) {
					if (dbA.getElem(i) != dbB.getElem(i)) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {

			return false;
		}
	}

	public static WebDriver initializeDriver() {
		if (readFromExcel("Browser", "Browser").equalsIgnoreCase("Chrome")) {
			System.setProperty("webdriver.chrome.driver",
					System.getProperty("user.home") + "/Desktop/ImageComparisonUtility/Drivers/chromedriver.exe");
			ChromeOptions options = new ChromeOptions();
			options.addArguments("start-maximized");
			options.setExperimentalOption("useAutomationExtension", false);
			options.addArguments("disable-infobars");
			WebDriver chromedriver = null;
			try {
				chromedriver = new ChromeDriverEx(options);
				return chromedriver;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		else if ((readFromExcel("Browser", "Browser").equalsIgnoreCase("FireFox"))) {
			System.setProperty("webdriver.gecko.driver",
					System.getProperty("user.home") + "/Desktop/ImageComparisonUtility/Drivers/geckodriver.exe");
			driver = new FirefoxDriver();
			return driver;
		} else {
			System.setProperty("webdriver.ie.driver", System.getProperty("user.home") + "/Desktop/ImageComparisonUtility/Drivers/IEDriverServer.exe");
			DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
			  caps.setCapability("EnableNativeEvents", false);
			  caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			  
			  caps.setCapability("ignoreZoomSetting", true);
			  driver = new InternetExplorerDriver(caps);
			  driver.manage().window().maximize();
			return driver;
		}
		return null;
	}

	public static BufferedImage highlightDifference() {

		int width1 = biA.getWidth();
		int height1 = biB.getHeight();
		BufferedImage outImg = new BufferedImage(width1, height1, BufferedImage.TYPE_INT_RGB);
		int diff;
		int result;

		try {
			for (int i = 0; i < height1 - 1; i++) {

				for (int j = 0; j < width1; j++) {

					int rgb1 = biA.getRGB(j, i);
					int rgb2 = biB.getRGB(j, i);
					int r1 = (rgb1 >> 16) & 0xff;
					int g1 = (rgb1 >> 8) & 0xff;
					int b1 = (rgb1) & 0xff;
					int r2 = (rgb2 >> 16) & 0xff;
					int g2 = (rgb2 >> 8) & 0xff;
					int b2 = (rgb2) & 0xff;
					diff = Math.abs(r1 - r2);
					diff += Math.abs(g1 - g2);
					diff += Math.abs(b1 - b2);
					diff /= 3;
					result = (diff << 16) | (diff << 8) | diff;

					outImg.setRGB(j, i, result);
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {

		}
		return outImg;
	}

	public static String getTestCaseNumber(int i) {
		String url = getExcelSheet("Url Sheet").getRow(i).getCell(0).getStringCellValue();
		return url;
	}

	public static String getBaseUrl(int i) {
		String url = getExcelSheet("Url Sheet").getRow(i).getCell(1).getStringCellValue();
		return url;
	}

	public static String getMigratedUrl(int i) {
		String url = getExcelSheet("Url Sheet").getRow(i).getCell(2).getStringCellValue();
		return url;
	}

	public static void main(String[] args) throws InterruptedException {

		WebDriver driver = initializeDriver();

		for (int i = 1; i <= getExcelSheet("Url Sheet").getLastRowNum(); i++) {
			driver.get(getBaseUrl(i));
			actualImage = System.getProperty("user.home") + "/Desktop/ImageComparisonUtility/Output/TestCase"
					+ getTestCaseNumber(i) + "/BaseImage" + getTimeStamp() + "screenshot.png";
			expectedImage = System.getProperty("user.home") + "/Desktop/ImageComparisonUtility/Output/TestCase"
					+ getTestCaseNumber(i) + "/MigratedImage" + getTimeStamp() + "screenshot.png";

			takeScreenShot(driver, actualImage);

			driver.get(getMigratedUrl(i));
			takeScreenShot(driver, expectedImage);
			OutcomeResult = compareImage(actualImage, expectedImage);
			String result;
			System.out.println(percentage);
			if (OutcomeResult.equals(true)) {
				result = "Pass";
			} else {
				result = "fail";
			}
			if (result.equals("fail")) {
				try {
					String OutputLocation = System.getProperty("user.home")
							+ "/Desktop/ImageComparisonUtility/Output/TestCase" + getTestCaseNumber(i) + "/Output"
							+ LocalDateTime.now().getNano() + "screenshot.png";

					ImageIO.write(highlightDifference(), "png", new File(OutputLocation));
					System.out.println(actualImage);
					System.out.println(OutputLocation);
					writeToExcel("Output", getTestCaseNumber(i), result, OutputLocation, expectedImage, actualImage,
							percentage);
				} catch (IOException e) {

					e.printStackTrace();
				}
			} else {
				writeToExcel("Output", getTestCaseNumber(i), result, "NA", expectedImage, actualImage, percentage);
			}
			percentage = 0;
		}
		driver.quit();
	}
}