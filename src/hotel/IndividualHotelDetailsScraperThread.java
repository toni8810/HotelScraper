package hotel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class IndividualHotelDetailsScraperThread extends Thread {
	
	private String url;
	private int id;
	
	public IndividualHotelDetailsScraperThread(String url,int id) {
		this.url = url;
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
		if (id == 0) id = IdGenerator.createID();
		/*if (DBManager.insertIntoUrls(url, id) == false) {
			return;
		} */
		WebDriver driver = new HtmlUnitDriver(false);
		String[] styles = {"Best Value","Boutique","Budget","Business","Charming","Classic","Family-friendly",
							"Green","Luxury","Mid-range","Quaint","Quiet","Romantic","Trendy" };
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
		WebElement heading = driver.findElement(By.id("HEADING_GROUP"));
		String address = heading.findElement(By.className("format_address")).getText();
		String hotelName = driver.findElement(By.id("HEADING")).getText();
		String city = driver.findElement(By.id("GEO_SCOPED_SEARCH_INPUT")).getAttribute("value");
		String country = city.substring(city.indexOf(",")+2);
		city = city.substring(0, city.indexOf(","));
		List<WebElement> tempList = driver.findElements(By.className("breadcrumb_link"));
		String area = tempList.get(tempList.size()-3).getText();
		String location;
		try {
			location = driver.findElement(By.xpath(".//*[@id='NEARBY_TAB']/h3")).getText();
			if (location.contains("Staying in")) {
				location = location.substring(11);
			}
			else location = "unknown";
			}
		catch (NoSuchElementException nsee) {
			location = "unknown";
		}
		String style = "";
		String star = "";
		try {
			tempList = driver.findElement(By.id("HR_HACKATHON_CONTENT")).findElements(By.className("tag"));
			for (int i=0; i<tempList.size(); i++) {
				if (i==0) {
					try{
						star = tempList.get(0).findElement(By.className("sprite-rating_cl_gry_fill")).getAttribute("alt");
						continue;
					}
					catch (NoSuchElementException nsee2) {
						star = "unknown";
					}
				}
				tempString = tempList.get(i).getText();
				if (contains(styles,tempString)) {
					style += tempString+", ";
				}
			}
			if (style.isEmpty()) style = "unknown";
			else style = style.substring(0, style.length()-2);
			if (star.isEmpty()) star = "unknown";
		}
		catch (NoSuchElementException nsee) {
			style = "unknown";
		}
		String accommodation;
		try {
			accommodation = heading.findElement(By.className("popRanking")).findElement(By.tagName("a")).getText();
			accommodation = accommodation.substring(0,accommodation.indexOf(" in "));
		}
		catch (NoSuchElementException nsee) {
			accommodation = "unknown";
		}
		int numOfReviews;
		try {
			String numOfReviewsString = heading.findElement(By.className("taLnk")).getAttribute("content");
			if (numOfReviewsString == null) numOfReviews = 0;
			else numOfReviews = Integer.parseInt(numOfReviewsString);
		}
		catch(NoSuchElementException nsee) {
			numOfReviews = 0;
		}
		double userRating;
		try {
			userRating = Double.parseDouble(heading.findElement(By.className("sprite-rating_rr_fill")).getAttribute("content"));
		}
		catch (NoSuchElementException nsee) {
			userRating = 0;
		}
		WebElement temp = driver.findElement(By.id("AMENITIES_TAB"));
		String highlights;
		try {
			highlights = temp.findElement(By.className("property_tags")).getText();
		}
		catch (NoSuchElementException nsee) {
			highlights = "unknown";
		}
		List<WebElement> amList = temp.findElements(By.className("amenity_row"));
		String aboutTheProperty = "";
		String thingsToDo = "";
		String roomTypes = "";
		String inYourRoom = "";
		String internet = "";
		String services = "";
		for (int i=0; i<amList.size(); i++) {
			tempString = amList.get(i).getText();
			if (tempString.startsWith("About the property")) aboutTheProperty = tempString.substring(18);
			if (tempString.startsWith("Things to do")) thingsToDo = tempString.substring(12);
			if (tempString.startsWith("Room types")) roomTypes = tempString.substring(10);
			if (tempString.startsWith("In your room")) inYourRoom = tempString.substring(12);
			if (tempString.startsWith("Internet")) internet = tempString.substring(8);
			if (tempString.startsWith("Services")) services = tempString.substring(8);
		}
		if(aboutTheProperty.isEmpty()) aboutTheProperty = "unknown";
		if(thingsToDo.isEmpty()) thingsToDo = "unknown";
		if(roomTypes.isEmpty()) roomTypes = "unknown";
		if(inYourRoom.isEmpty()) inYourRoom = "unknown";
		if(internet.isEmpty()) internet = "unknown";
		if(services.isEmpty()) services = "unknown";
		
		String officialDes = "";
		try {
			officialDes = temp.findElement(By.className("tabs_description_content")).getText();
		}
		catch (NoSuchElementException nsee) {
			officialDes = "unknown";
		}
		String addInfo;
		try {
			addInfo = driver.findElement(By.className("additional_info_amenities")).findElement(By.xpath("div/div")).getText();
		}
		catch (NoSuchElementException nsee) {
			addInfo = "unknown";
		}
		int price = getAvaregePrice(addInfo);
		String alsoKnownAs = "";
		try {
			tempList = driver.findElement(By.id("AKA")).findElements(By.className("indent"));
			for (int i=0; i<tempList.size(); i++) {
				alsoKnownAs += tempList.get(i).getText() +", ";
			}
			alsoKnownAs = alsoKnownAs.substring(0,alsoKnownAs.length()-2);
		}
		catch(NoSuchElementException nsee) {
			alsoKnownAs = "unknown";
		}
		String hotelStyle = "";
		try {
			tempList = temp.findElements(By.className("slim_ranking"));
			for (int i=0; i<tempList.size(); i++) {
				tempString = tempList.get(i).getText();
				tempString = tempString.substring(tempString.indexOf(" ")+1);
				hotelStyle += tempString + ", ";
			}
			if (hotelStyle.isEmpty()) hotelStyle = "unknown";
			else hotelStyle = hotelStyle.substring(0, hotelStyle.length()-2);
		}
		catch (NoSuchElementException nsee) {
			hotelStyle = "unknown";
		}
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
		//Start of images
		ArrayList<String> imageUrls = new ArrayList<String>();
		boolean noMoreImages = false;
		tempList = driver.findElements(By.className("carouselPhoto"));
		String photoId = "";
		for (int i=0; i<tempList.size(); i++) {
			tempString = tempList.get(i).getAttribute("data-photoid");
			if (tempString != null) {
				photoId = tempList.get(i).getAttribute("data-photoid");
			}
		}
		if (!photoId.isEmpty()) {
			//50771178
			url = url.replace("Hotel_Review", "LocationPhotoDirectLink");
			url = url.replace("-Reviews", "-i"+photoId);
			driver.get(url);
			while (noMoreImages == false) {
				try {
					temp = driver.findElement(By.className("thumbImg"));
				}
				catch (NoSuchElementException nsee) {
					tempString = driver.findElement(By.id("photo_"+photoId)).findElement(By.className("big_photo")).getAttribute("src");
					tempString = tempString.replace("photo-s", "photo-w");
					imageUrls.add(tempString);
					break;
				}
				tempList = temp.findElements(By.tagName("img"));
				for (int i=0; i<tempList.size(); i++) {
					tempString = tempList.get(i).getAttribute("src");
					tempString = tempString.replace("photo-l", "photo-w");
					imageUrls.add(tempString);
				}
				if (imageUrls.size() > 23) break;
				temp = temp.findElement(By.className("right")).findElement(By.xpath(".."));
				if (!temp.getTagName().contentEquals("a")) {
					noMoreImages = true;
				}
				else driver.get(temp.getAttribute("href"));
			}
		}
		driver.close();
		driver.quit();
		highlights = highlights.replace("\"", "");
		aboutTheProperty= aboutTheProperty.replace("\"", "");
		thingsToDo = thingsToDo.replace("\"", "");
		roomTypes = roomTypes.replace("\"", "");
		inYourRoom = inYourRoom.replace("\"", "");
		internet = internet.replace("\"", "");
		services = services.replace("\"", "");
		officialDes = officialDes.replace("\"", "");
		addInfo = addInfo.replace("\"", "");
		alsoKnownAs = alsoKnownAs.replace("\"", "");
		hotelStyle = hotelStyle.replace("\"", "'");
		hotelName = hotelName.replace("\"", "'");
		address = address.replace("\"", "'");
		city = city.replace("\"", "'");
		area = area.replace("\"", "'");
		location = location.replace("\"", "'");
		country = country.replace("\"", "'");
		
		try {
			DBManager.insertIntoAmenities(id, highlights, aboutTheProperty, thingsToDo, roomTypes, inYourRoom, internet, services, officialDes, addInfo, alsoKnownAs, hotelStyle);
			DBManager.insertIntoBasics(id, hotelName, address, city, area, location, country, style, price, star, latitude, longitude, accommodation);
			DBManager.insertIntoReviews(id, numOfReviews, userRating);
			DBManager.insertIntoImagesTripadvisor(id, imageUrls);
		} catch (MySQLIntegrityConstraintViolationException e) {
			DBManager.deleteFromUrlsWhereUrl(url);
			run();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("End "+hotelName+" "+city+" "+country+" "+id);
	}
	private boolean contains(String[] styles, String temp) {
		for (int i=0; i<styles.length; i++) {
			if (styles[i].contentEquals(temp)) return true;
		}
		return false;
	}
	private int getAvaregePrice(String addInfo) {
		String priceRange = "";
		int num1;
		int num2;
		String[] eachLine = addInfo.split("\\n");
		for (int i=0; i<eachLine.length; i++) {
			if (eachLine[i].contains("Price Range:")) {
				priceRange = eachLine[i];
				priceRange = priceRange.substring(priceRange.indexOf(":")+2, priceRange.indexOf("(")-1);
				break;
			}
		}
		if (priceRange.isEmpty()) return 0;
		priceRange = priceRange.replace(",", "");
		if (priceRange.indexOf("-") < 0) {
			return Integer.parseInt(priceRange.substring(1));
		}
		num1 = Integer.parseInt(priceRange.substring(1,priceRange.indexOf("-")-1));
		num2 = Integer.parseInt(priceRange.substring(priceRange.indexOf("-")+3));
		return (num1+num2)/2;
	}
}
