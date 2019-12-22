package com.husain.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class GenerateImageDiff {

	public static void main(String[] args) throws Throwable {
		WebDriver driver = null;
		System.setProperty("webdriver.chrome.driver", "src/main/resources/Drivers/chromedriver.exe");
		try {
			driver = new ChromeDriver();
			driver.get("https://en.wikipedia.org/wiki/Selenium_(software)");
			driver.manage().window().maximize();
			Thread.sleep(5000);
			// driver.manage().window().setSize(new Dimension(1920, 1080));
			// Thread.sleep(1000);

			takeFullPageScreenshot(driver, "./src/main/resources/Images/Actual.png");
			File imageExpected = new File("./src/main/resources/Images/Expected.png");
			File imageActual = new File("./src/main/resources/Images/Actual.png");

			getDifferencePercent(ImageIO.read(imageExpected), ImageIO.read(imageActual));
			ImageIO.write(getDifferenceImage(ImageIO.read(imageExpected), ImageIO.read(imageActual)), "png",
					new File("./src/main/resources/Images/diffOutput.png"));

			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.close();
			driver.quit();
			System.out.println("End");
		}

	}

	public static void takeFullPageScreenshot(WebDriver driver, String storePath) throws IOException {
		// Screenshot sceenshotPage = new
		// AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);
		Screenshot sceenshotPage = new AShot().shootingStrategy(ShootingStrategies.viewportRetina(100, 0, 0, 1.25f))
				.takeScreenshot(driver);
		ImageIO.write(sceenshotPage.getImage(), "PNG", new File(storePath));
	}

	private static double getDifferencePercent(BufferedImage img1, BufferedImage img2) {
		int width = img1.getWidth();
		int height = img1.getHeight();
		int width2 = img2.getWidth();
		int height2 = img2.getHeight();
		if (width != width2 || height != height2) {
			throw new IllegalArgumentException(String.format(
					"Images must have the same dimensions: (%d,%d) vs. (%d,%d)", width, height, width2, height2));
		}

		long diff = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				diff += pixelDiff(img1.getRGB(x, y), img2.getRGB(x, y));
			}
		}
		long maxDiff = 3L * 255 * width * height;
		System.out.println("% diff: " + 100.0 * diff / maxDiff);
		return 100.0 * diff / maxDiff;
	}

	private static int pixelDiff(int rgb1, int rgb2) {
		int r1 = (rgb1 >> 16) & 0xff;
		int g1 = (rgb1 >> 8) & 0xff;
		int b1 = rgb1 & 0xff;
		int r2 = (rgb2 >> 16) & 0xff;
		int g2 = (rgb2 >> 8) & 0xff;
		int b2 = rgb2 & 0xff;
		return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
	}

	public static BufferedImage getDifferenceImage(BufferedImage img1, BufferedImage img2) {
		// convert images to pixel arrays...
		final int w = img1.getWidth(), h = img1.getHeight(), highlight = Color.MAGENTA.getRGB();
		final int[] p1 = img1.getRGB(0, 0, w, h, null, 0, w);
		final int[] p2 = img2.getRGB(0, 0, w, h, null, 0, w);
		// compare img1 to img2, pixel by pixel. If different, highlight img1's pixel...
		for (int i = 0; i < p1.length; i++) {
			if (p1[i] != p2[i]) {
				p1[i] = highlight;
			}
		}
		// save img1's pixels to a new BufferedImage, and return it...
		// (May require TYPE_INT_ARGB)
		final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		out.setRGB(0, 0, w, h, p1, 0, w);
		return out;
	}

}
