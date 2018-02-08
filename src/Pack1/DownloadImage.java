package Pack1;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.util.UUID;

public class DownloadImage {

	public static int localTime1;
	public static int localTime2;

	private static String getShortUUID() {
		UUID uuid = UUID.randomUUID();
		long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}

	public static void takeScreenShot(WebDriver driver, String name) {

		File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(scrFile, new File("d:\\tmp\\" + name + "_" + getShortUUID() + "screenshot.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void screen(WebDriver driver) {
		try {
			driver.get("https://www.amazon.in/s/ref=nb_sb_noss_2?url=node%3D14306014031&field-keywords=t+shirt");

			takeScreenShot(driver, "Expected");

			Thread.sleep(30000);
			
			driver.get("https://www.amazon.in/s/ref=nb_sb_noss_2?url=node%3D14306014031&field-keywords=t+shirt");

			takeScreenShot(driver, "Actual");

		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
