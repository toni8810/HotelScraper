package hotel;

import java.util.List;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class YellowPagesScraper extends Thread {
	private String hotelName;
	private String address;
	private int id;
	private String website = "unknown";
	private String phoneNumber = "unknown";
	HtmlUnitDriver driver;
	String url;
	
	public YellowPagesScraper(String hotelName, String address, int id) {
		this.hotelName = hotelName;
		this.address = address;
		this.id = id;
	}
	
	public void run() {
		constructURL(hotelName, address);
		driver = new HtmlUnitDriver(false);
		driver.get(url);
		if (driver.getTitle().contains("No results for")) return;
		if (driver.getTitle().contentEquals("Blocked » Yell.com")) {
			System.out.println(url);
			JOptionPane.showConfirmDialog(null, "Have you changed your ip?");
			driver.get(url);
			System.out.println(driver.getCurrentUrl());
			
		}
		try {
			WebElement details = driver.findElement(By.className("businessCapsule--details"));
			List<WebElement> tempList = details.findElements(By.tagName("a"));
			for (int i=0; i<tempList.size(); i++) {
				if (tempList.get(i).getAttribute("itemprop") != null) {
					website = tempList.get(i).getAttribute("href");
					break;
				}
			}
			tempList = details.findElements(By.className("businessCapsule--tel"));
			for (int i=0; i<tempList.size(); i++) {
				if (tempList.get(i).getAttribute("itemprop") != null) {
					phoneNumber = tempList.get(i).getText();
					break;
				}
			}
		}
		catch (NoSuchElementException nsee) {
			System.out.println(url);
			JOptionPane.showConfirmDialog(null, "Whoops human interaction needed");
		}
		if (!(website.contentEquals("unknown")) || !(phoneNumber.contentEquals("unknown"))) {
			DBManager.updateWebsiteAndPhoneNumber(id,website,phoneNumber);
			System.out.println(url);
			System.out.println(website);
			System.out.println(phoneNumber);
		}
		
	}
	private void constructURL(String hotelName2, String address2) {
		// //https://www.yell.com/ucs/UcsSearchAction.do?keywords=Tovey+Lodge&location=Underhill+Lane%2C+Ditchling+BN6+8XE%2C+England
		hotelName2 = hotelName2.replace("&", "and");
		hotelName2 = hotelName2.replace("#", "");
		hotelName2 = hotelName2.trim();
		address2 = address2.replace("&", "and");
		address2 = address2.replace("#", "");
		address2 = address2.trim();
		url = "https://www.yell.com/ucs/UcsSearchAction.do?keywords=" +hotelName2 + "&location=" + address2;
		url = url.replace(" ", "+");
		url = url.replace(",", "%2C");
	}
}
