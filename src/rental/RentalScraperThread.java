package rental;

import java.sql.SQLException;
import java.util.List;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import hotel.DBManager;
import hotel.IdGenerator;
import hotel.ThreadController;

public class RentalScraperThread extends Thread {
	private String url;
	
	public RentalScraperThread(String url) {
		this.url = url;
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
		int id = IdGenerator.createID();
		if (DBManager.insertIntoUrls(url, id) == false) {
			return;
		}
		HtmlUnitDriver driver = new HtmlUnitDriver(false);
		String tempString;
		try {
			driver.get(url);
			if (!url.contentEquals(driver.getCurrentUrl())) {
				DBManager.deleteFromUrls(id);
				return;
			}
		}
		catch (WebDriverException wde) {
			ThreadController.okay = false;
			System.out.println(url);
			wde.printStackTrace();
			try {
				sleep(1000*60*5);
			} catch (InterruptedException ie) {
				// TODO Auto-generated catch block
				ie.printStackTrace();
			}
			driver.get(url);
			ThreadController.okay = true;
		}
		catch (Exception e) {
			ThreadController.okay = false;
			System.out.println(url);
			e.printStackTrace();
			JOptionPane.showConfirmDialog(null, "Human interaction needed!");
			try {
				sleep(1000*60*2);
			} catch (InterruptedException ie) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			driver.get(url);
			ThreadController.okay = true;
		}
		WebElement temp = null;
		String rentalName = driver.findElement(By.id("HEADING")).getText();
		String address = "unknown";
		String city = driver.findElement(By.id("GEO_SCOPED_SEARCH_INPUT")).getAttribute("value");
		String country = city.substring(city.indexOf(",")+2);
		city = city.substring(0, city.indexOf(","));
		List<WebElement> tempList = driver.findElements(By.className("breadcrumb_link"));
		String area = tempList.get(tempList.size()-2).getText();
		String location = "unknown";
		String style = "unknown";
		String star = "unknown";
		String accommodation = "Rentals";
		int numOfReviews;
		try {
			temp = driver.findElement(By.id("rating-bubbles-container"));
			String numOfReviewsString = temp.findElement(By.className("rating-summary-count")).getAttribute("content");
			if (numOfReviewsString == null) numOfReviews = 0;
			else numOfReviews = Integer.parseInt(numOfReviewsString);
		}
		catch(NoSuchElementException nsee) {
			numOfReviews = 0;
		}
		double userRating;
		if (numOfReviews == 0) {
			userRating = 0;
		}
		else {
			userRating = Double.parseDouble(temp.findElement(By.className("sprite-rating_s_fill")).getAttribute("content"));
		}
		String highlights = "";
		String services = "";
		try {
			temp = driver.findElement(By.id("amenitiesCard"));
			tempList = temp.findElement(By.className("amenities")).findElements(By.tagName("li"));
			for (int i=0; i<tempList.size(); i++) {
				highlights += tempList.get(i).getText() + ", ";
		
			}
			if (highlights.isEmpty()) {
				highlights = "unknown";
			}
			else {
				highlights = highlights.substring(0, highlights.length()-2);
			}
			tempList = temp.findElement(By.className("suitability")).findElements(By.tagName("li"));
			for (int i=0; i<tempList.size(); i++) {
				services += tempList.get(i).getText() + ", ";
			}
			if (services.isEmpty()) {
				services = "unknown";
			}
			else {
				services = services.substring(0, services.length()-2);
			}
		}
		catch (NoSuchElementException nsee) {
			highlights = "unknown";
			services = "unknown";
		}
		String aboutTheProperty = "";
		try {
			temp = driver.findElement(By.id("VR_ACCESS"));
			aboutTheProperty += temp.findElement(By.className("nearybyLabel")).getText() + "\n";
			temp = driver.findElement(By.id("VR_HOW_TO"));
			tempList = temp.findElements(By.tagName("div"));
			for (int i=0; i<tempList.size(); i++) {
				//exclude first div
				if (i > 0) {
					if (tempList.get(i).getAttribute("id") != null) break;
					aboutTheProperty += tempList.get(i).getText() + "\n";
				}
			}
			if (aboutTheProperty.isEmpty()) {
				aboutTheProperty = "unknown";
			}
			else {
				aboutTheProperty = aboutTheProperty.trim();
			}
		}
		catch (NoSuchElementException nsee) {
			if (aboutTheProperty.isEmpty()) {
				aboutTheProperty = "unknown";
			}
		}
		String thingsToDo = "";
		try {
			temp = driver.findElement(By.xpath(".//*[@id='VR_ACTIVITIES']/div[2]"));
			thingsToDo = temp.getText();
		}
		catch (NoSuchElementException nsee) {
			thingsToDo = "unknown";
		}
		String roomTypes = "unknown";
		String inYourRoom = "unknown";
		String internet = "";
		if (highlights.contains("Internet Access")) {
			internet = "Internet Access, ";
		}
		if (highlights.contains("Wi-Fi")) {
			internet += "Wifi";
		}
		if (internet.isEmpty()) {
			internet = "unknown";
		}
		else {
			if (internet.endsWith(", ")) internet = internet.substring(0, internet.length()-2);
		}
		String officialDes = "";
		temp = driver.findElement(By.id("vrWideOverview"));
		officialDes = temp.findElement(By.className("detail-card")).getText();
		String[] lines = officialDes.split("\n");
		officialDes = "";
		for (int i=0; i<lines.length; i++) {
			if (lines[i].contains("Overview")) continue;
			else if (lines[i].contains("Send to a friend")) continue;
			else if (lines[i].contains("Powered by")) continue;
			else if (lines[i].contains("Contact Owner")) continue;
			else if (lines[i].contains("Listing")) continue;
			else if (lines[i].trim().isEmpty()) continue;
			else {
				officialDes += lines[i] +"\n";
			}
		}
		String addInfo = "unknown";
		int price;
		tempString = driver.findElement(By.id("fromDailyRate")).getText().trim();
		if (tempString.isEmpty()) price = 0;
		else price = Integer.parseInt(tempString.substring(1));
		String alsoKnownAs = "unknown";
		String hotelStyle = "unknown";
		double longitude;
		double latitude;
		try {
			temp = driver.findElement(By.id("NEARBY_TAB")).findElement(By.className("mapContainer"));
			longitude = Double.parseDouble(temp.getAttribute("data-lng"));
			latitude = Double.parseDouble(temp.getAttribute("data-lat"));
		}
		catch (NoSuchElementException nsee) {
			longitude = 0;
			latitude = 0;
		}
		temp = driver.findElement(By.id("vrPhotoViewer"));
		String[] imageUrls;
		tempList = temp.findElements(By.className("thumb-box"));
		imageUrls = new String[tempList.size()];
		for (int i=0; i<tempList.size(); i++) {
			imageUrls[i] = tempList.get(i).getAttribute("largeurl");
		}
		driver.close();
		driver.quit();
		
		highlights = highlights.replace("\"", "");
		aboutTheProperty= aboutTheProperty.replace("\"", "");
		thingsToDo = thingsToDo.replace("\"", "");
		services = services.replace("\"", "");
		officialDes = officialDes.replace("\"", "");
		officialDes = officialDes.replace("<BR>", "");
		addInfo = addInfo.replace("\"", "");
		alsoKnownAs = alsoKnownAs.replace("\"", "");
		hotelStyle = hotelStyle.replace("\"", "'");
		rentalName = rentalName.replace("\"", "'");
		address = address.replace("\"", "'");
		city = city.replace("\"", "'");
		area = area.replace("\"", "'");
		country = country.replace("\"", "'");
		
		try {
			DBManager.insertIntoAmenities(id, highlights, aboutTheProperty, thingsToDo, roomTypes, inYourRoom, internet, services, officialDes, addInfo, alsoKnownAs, hotelStyle);
			DBManager.insertIntoBasics(id, rentalName, address, city, area, location, country, style, price, star, latitude, longitude, accommodation);
			DBManager.insertIntoReviews(id, numOfReviews, userRating);
			DBManager.insertIntoImages(id, imageUrls);
		} catch (MySQLIntegrityConstraintViolationException e) {
			DBManager.deleteFromUrlsWhereUrl(url);
			run();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("End "+rentalName+" "+city+" "+country);
		
		
		
	}
}
