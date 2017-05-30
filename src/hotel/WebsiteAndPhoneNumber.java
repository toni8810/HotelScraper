package hotel;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class WebsiteAndPhoneNumber extends Thread {
	private String hotelName;
	private String address;
	private int id;
	private String website;
	private String phoneNumber;
	HtmlUnitDriver driver;
	String url;
	public WebsiteAndPhoneNumber(String hotelName, String address, int id) {
		this.hotelName = hotelName;
		this.address = address;
		this.id = id;
	}
	public void run() {
		while (ThreadController.okay == false) {
			try {
				sleep(1000*10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		url = constructURL(hotelName,address);
		driver = new HtmlUnitDriver(false);
		try {
			driver.get(url);
		}
		catch (WebDriverException wde) {
			try {
				ThreadController.okay = false;
				System.out.println("We are in the catch block");
				sleep(1000*15);
				try {
					driver.get(url);
					System.out.println("It worked out");
					ThreadController.okay = true;
				}
				catch(WebDriverException wde2) {
					System.out.println("It did not work out driver.get(url)");
					ThreadController.okay = true;
					return;
				}
			} catch (InterruptedException ie) {
				// TODO Auto-generated catch block
				ie.printStackTrace();
			}
			ThreadController.okay = true;
		}
		try {
			String allData = driver.findElement(By.id("lgb_info")).getText();
			String[] lines = allData.split("\n");
			for (int i=0; i<lines.length; i++){
				getUrl(lines[i]);
				findPhoneNumber(lines[i]);
			}
		}
		catch (NoSuchElementException nsee) {
			checkSideColumn();
			/*if (website == null && phoneNumber == null) {
				findRightWebsite();
			} */	
		}
		if ((website == null) || (website.contains("'"))) website = "unknown";
		if (phoneNumber == null) phoneNumber = "unknown";
		if (!(website.contentEquals("unknown")) || !(phoneNumber.contentEquals("unknown"))) {
			DBManager.updateWebsiteAndPhoneNumber(id, website, phoneNumber);
			System.out.println(url);
			System.out.println(website);
			System.out.println(phoneNumber);
		}
		
		
	}
	private void checkSideColumn() {
		//class cbtn
		WebElement column = null;
		try {
			column = driver.findElement(By.id("b_context"));
		}
		catch (NoSuchElementException nsee) {
			ThreadController.okay = false;
			driver.get(url);
			System.out.println("We are in the catch block");
			try {
				sleep(1000*15);
			} catch (InterruptedException ie) {
				// TODO Auto-generated catch block
				ie.printStackTrace();
			}
			try {
				String allData = driver.findElement(By.id("lgb_info")).getText();
				String[] lines = allData.split("\n");
				for (int i=0; i<lines.length; i++){
					getUrl(lines[i]);
					findPhoneNumber(lines[i]);
				}
				System.out.println("It worked out");
				ThreadController.okay = true;
				return;
			}
			catch (NoSuchElementException nsee2) {
				try {
					sleep(1000);
					column = driver.findElement(By.id("b_context"));
					System.out.println("It worked out");
					ThreadController.okay = true;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchElementException nsee3) {
						System.out.println("It did not work out");
						ThreadController.okay = true;
						return;
					}
				
					
			}
			ThreadController.okay = true;
			
		}
		String allData = column.getText();
		List<WebElement> buttons = column.findElements(By.className("cbtn"));
		for (int i=0; i<buttons.size(); i++) {
			if (buttons.get(i).getText().contentEquals("Website")) {
				website = buttons.get(i).getAttribute("href");
				break;
			}
		}
		String[] lines = allData.split("\n");
		for (int i=0; i<lines.length; i++) {
			if (lines[i].contains("Phone:")) {
				phoneNumber = lines[i].substring(lines[i].indexOf("Phone:")+7);
				break;
			}
		}
		
	}
	private void getUrl(String string) {
	    Pattern r = Pattern.compile("^[a-z]+\\S+\\..+");
	      
	   // Now create matcher object.
	      Matcher m = r.matcher(string);
	      
	      while (m.find()) {
	    	  if (!m.group().isEmpty()) {
	    		  website = m.group();
	    	  }
	      }
		
	}
	/*private boolean findRightWebsite() {
		List<WebElement> results = driver.findElements(By.className("b_algo"));
		String url = "";
		boolean foundLink = false;
		for (int i=0; i<results.size(); i++) {
			url = results.get(i).findElement(By.tagName("a")).getAttribute("href");
			if (url.startsWith("http://maps")) break;
			StringBuffer sb = new StringBuffer(url);
			if ((sb.indexOf("/", 10)+1) == sb.length()) {
				foundLink = true;
				break;
			}
			else if ((sb.toString().contains("contact-us")) || (sb.toString().contains("Contact-Us"))) {
				foundLink = true;
				break;
			}
			else if ((sb.toString().contains("Contact+Us")) || (sb.toString().contains("home"))) {
				foundLink = true;
				break;
			}
		}
		if (foundLink == true) {
			System.out.println(url);
			website = url;
			driver.get(url);
			findPhoneNumber(driver.getPageSource());
			return true;
		}
		else return false;
		
	} */
	private void findPhoneNumber(String pageSource) {
		// Create a Pattern object
		pageSource = pageSource.replace(" ", "");
	    Pattern r = Pattern.compile("\\+?\\d+(\\()?(\\d+)?(\\))?\\d+(\\/)?\\d+-?\\d+");
	      
	   // Now create matcher object.
	      Matcher m = r.matcher(pageSource);
	      
	      while (m.find()) {
	    	  if ((!m.group().isEmpty()) && (m.group().length() > 9)) {
	    		  phoneNumber = m.group();
	    		  if (phoneNumber.startsWith("+")) break;
	    	  }
	      }
		
	}
	private String constructURL(String hotelName2, String address2) {
		// http://www.bing.com/search?q=Parkhotel+zur+Klause+Am+Sulzbach+10%2C+Bad+Hall+4540%2C+Austria
		hotelName2 = hotelName2.replace("&", "and");
		hotelName2 = hotelName2.replace("#", "");
		hotelName2 = hotelName2.trim();
		address2 = address2.replace("&", "and");
		address2 = address2.replace("#", "");
		address2 = address2.trim();
		String returnString = "http://www.bing.com/search?q=" +hotelName2 + " " + address2;
		returnString = returnString.replace(" ", "+");
		returnString = returnString.replace(",", "%2C");
		return returnString;
	}
}
