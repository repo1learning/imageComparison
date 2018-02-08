package Pack1;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Compare {
	static BufferedImage biA;
	static BufferedImage biB;
	static String actualImage, expectedImage;
	static Boolean OutcomeResult;
	static Workbook workbook = null;

	public static HSSFSheet getExcelSheet(String sheetName) {
		try {
			FileInputStream inputStream = new FileInputStream(new File(getValueFromPropertyFile("ExcelPath")));
			workbook = new HSSFWorkbook(inputStream);
			HSSFSheet sheet = (HSSFSheet) workbook.getSheet(sheetName);

			return sheet;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getValueFromPropertyFile(String key) {
		try {
			FileReader reader = new FileReader(System.getProperty("user.dir") + "\\TestData\\config.properties");
			Properties prop = new Properties();
			try {
				prop.load(reader);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return prop.getProperty(key);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
			key = formatter.formatCellValue(readFromSheet.getRow(0).getCell(i));
			value = formatter.formatCellValue(readFromSheet.getRow(1).getCell(i));
			keys.add(key);
			Values.add(value);
			keyValues.put(keys.get(i), Values.get(i));
		}
		return keyValues.get(Key);
	}

	public static void writeToExcel(String sheetName, String result, String location, String expectedImageLocation,
			String actualImageLocation) {
		HSSFSheet sheetTowrite = getExcelSheet(sheetName);
		sheetTowrite.createRow(sheetTowrite.getLastRowNum() + 1);
		sheetTowrite.getRow(sheetTowrite.getLastRowNum()).createCell(2).setCellValue(result);
		sheetTowrite.getRow(sheetTowrite.getLastRowNum()).createCell(3).setCellValue(location);
		sheetTowrite.getRow(sheetTowrite.getLastRowNum()).createCell(4).setCellValue(expectedImageLocation);
		sheetTowrite.getRow(sheetTowrite.getLastRowNum()).createCell(5).setCellValue(actualImageLocation);
		try {
			FileOutputStream fileOut = new FileOutputStream(getValueFromPropertyFile("ExcelPath"));
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

	public static void takeScreenShot(WebDriver driver, String name) {
		File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(scrFile, new File(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean compareImage(String actualImage, String expectedImage) {
		try {
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

					if (dbA.getElem(i) != dbB.getElem(i)) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			System.out.println("Failed to compare image files ...");
			return false;
		}
	}

	public static WebDriver initDriver() {
		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\amol.sharma\\Downloads\\chromedriver_win32\\chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("start-maximized");
		WebDriver driver = new ChromeDriver(options);
		return driver;
	}

	public static BufferedImage highlightDifference() {

		int width1 = biA.getWidth();
		int height1 = biB.getHeight();
		BufferedImage outImg = new BufferedImage(width1, height1, BufferedImage.TYPE_INT_RGB);
		int diff;
		int result;
		for (int i = 0; i < height1; i++) {
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
		return outImg;
	}

	public static void main(String[] args) throws InterruptedException {
		int count = 0;

		while (count != 2) {
			WebDriver driver = initDriver();
			driver.get("https://www.google.com");
			actualImage = "d:\\tmp\\" + "actualImage" + getTimeStamp() + "screenshot.png";
			expectedImage = "d:\\tmp\\" + "expectedImage" + getTimeStamp() + "screenshot.png";
			takeScreenShot(driver, actualImage);
			Thread.sleep(2000);
			takeScreenShot(driver, expectedImage);
			OutcomeResult = compareImage(actualImage, expectedImage);
			System.out.println("outcome result is ---- " + OutcomeResult);
			String result;
			if (OutcomeResult.equals(true)) {
				result = "Pass";
			} else {
				result = "fail";
			}
			if (result.equals("fail")) {
				try {
					String OutputLocation = "d:\\tmp\\000" + count + LocalDateTime.now().getNano() + "screenshot.png";
					ImageIO.write(highlightDifference(), "png", new File(OutputLocation));
					System.out.println(actualImage);
					System.out.println(OutputLocation);
					writeToExcel("Url Sheet", result, OutputLocation, expectedImage, actualImage);
				} catch (IOException e) {

					e.printStackTrace();
				}
			} else {

				writeToExcel("Url Sheet", result, "NA", expectedImage, actualImage);
			}
			System.out.println("Count is :" + count);
			driver.close();
			count++;
		}
	}
}