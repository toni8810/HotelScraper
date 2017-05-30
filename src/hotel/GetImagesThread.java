package hotel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;


public class GetImagesThread extends Thread {
	private int id;
	private String hotelName;
	private String address;
	private String phone = "";
	private String country;
	private double longitude;
	private double latitude;
	StringBuilder a;
	public GetImagesThread(int id,String hotelName, String address,String country, double longitude) {
		this.id = id;
		this.hotelName = hotelName;
		this.address = address;
		this.country = country;
		this.longitude = longitude;
	}
	public void run() {
		boolean booking = false;
		String temp = "";
		ArrayList<String> urls = new ArrayList<String>();
		HtmlUnitDriver driver = new HtmlUnitDriver(false);
		hotelName = hotelName.replace("&", "and");
		hotelName = hotelName.replace("#", "");
		hotelName = hotelName.trim();
		String url = "http://www.bing.com/search?q=" +hotelName + " " + country;
		url = url.replace(" ", "+");
		url = url.replace(",", "%2C");
		driver.get(url);
		WebElement content;
		try {
			content = driver.findElement(By.id("b_results"));
		}
		catch (NoSuchElementException nsee) {
			System.out.println(url);
			nsee.printStackTrace();
			return;
		}
		List<WebElement> websiteUrls = content.findElements(By.className("b_algo"));
		for (int i=0; i<websiteUrls.size(); i++) {
			temp = websiteUrls.get(i).findElement(By.tagName("a")).getAttribute("href");
			if(temp.startsWith("http://www.booking.")) {
				if (compare(temp) == false) {
					temp = "";
					continue;
				}
				booking = true;
				break;
			}
			else if(temp.startsWith("https://www.expedia.")) {
				if (compareExpedia(temp) == false) {
					temp = "";
					continue;
				}
				booking = false;
				break;
			}
			else temp = "";
		}
		driver.close();
		driver.quit();
		if (!temp.isEmpty()) {
			if (booking == false) urls = getImageUrls(temp);
			else urls = getImageUrlsFromBooking(temp);
			if (DBManager.isPhoneNumberUnknown(id) == true) DBManager.updatePhoneNumber(id, phone);
			if (urls.size() > 0) {
				if (booking == false) {
					DBManager.insertIntoImagesFromExpedia(id, urls);
					DBManager.insertIntoExpediaUrls(id, temp);
				}
				else {
					DBManager.insertIntoImagesFromBooking(id,urls);
					DBManager.insertIntoBookingUrls(id, temp);
				}
				if (longitude > 0) DBManager.updateLongAndLat(longitude, latitude, id);
				System.out.println(hotelName);
				System.out.println(urls);
				System.out.println(phone);
			}
			
		}
		
	}
	private ArrayList<String> getImageUrlsFromBooking(String url) {
		ArrayList<String> imageUrls = new ArrayList<String>();
		String temp = "";
	    String inputLine;
	    String[] lines = a.toString().split("\n");
	    for (int i=0; i<lines.length; i++) {
	        	inputLine = lines[i];
	        	if (inputLine.contains("max400")) {
	        		temp = inputLine.substring(inputLine.indexOf('"')+1);
	        		temp = temp.substring(0, temp.indexOf('"'));
	        		//if max1024x768 is not available try 840x460 
	        		temp = temp.replace("max400", "max1024x768");
	        		imageUrls.add(temp);
	        	}
	        	
	        }
		return imageUrls;
	}
	private boolean compareExpedia(String url) {
		String hotelNameOnEx = "";
		String addressEx = "";
		int score = 0;
		boolean passed;
		try {
			URL yahoo = new URL(url);
			URLConnection yc = yahoo.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
	                yc.getInputStream(), "UTF-8"));
	        String inputLine;
	        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("code.html")));
	        while ((inputLine = in.readLine()) != null) {
	        	bw.append(inputLine+"\n");
	        	if (inputLine.contains("<h1 id=\"hotel-name\"")) {
	        		hotelNameOnEx = inputLine.substring(inputLine.indexOf('>')+1);
	        		try {
	        			hotelNameOnEx = hotelNameOnEx.substring(0, hotelNameOnEx.indexOf('<'));
	        		}
	        		catch (IndexOutOfBoundsException ioobe) {
	        			inputLine = in.readLine();
	        			inputLine = inputLine.substring(inputLine.indexOf('>')+1);
	        			inputLine = inputLine.substring(0, inputLine.indexOf('<'));
	        			hotelNameOnEx = inputLine;
	        		}
	        		
	        	}
	        	if (inputLine.contains("<span class=\"street-address\"")) {
	        		addressEx = inputLine.substring(inputLine.indexOf('>')+1);
	        		addressEx = addressEx.substring(0, addressEx.indexOf('<'));
	        		addressEx = addressEx.replace(",", "");
	        	}
	        }
	        bw.close();
		}
		catch (FileNotFoundException fnfe) {
			System.out.println("Url is not valid anymore at id: "+id);
			return false;
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		String[] hotelNameWords = hotelNameOnEx.split("\\s");
		for (int i=0; i<hotelNameWords.length; i++) {
			if (hotelName.toLowerCase().contains(hotelNameWords[i].toLowerCase())) score++;
		}
		if (hotelNameWords.length == 1 ||  score < (hotelNameWords.length/2 == 1 ? 2 : hotelNameWords.length/2) ) {
			passed = false;	
		}
		else passed = true;
		if (passed == true) {
			score = 0;
			String[] addressWords = addressEx.split("\\s");
			for (int i=0; i<addressWords.length; i++) {
				if (address.toLowerCase().contains(addressWords[i].toLowerCase())) score++;
			}
			if (score <= addressWords.length/2) {
				passed = false;	
			}
			else passed = true;
		}
		return passed;
	}
	private boolean compare(String url) {
		a = new StringBuilder();
		int score = 0;
		boolean passed = false;
		boolean hotelNameGotten = false;
		boolean addressGotten = false;
		String hotelNameOnBooking = "";
		String addressBooking = "";
			
		try {
			URL yahoo = new URL(url);
			URLConnection yc = yahoo.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
	                yc.getInputStream(), "UTF-8"));
	        String inputLine;
	        
	        while ((inputLine = in.readLine()) != null) {
	        	a.append(inputLine+"\n");
	        	
	        	if (!hotelNameOnBooking.isEmpty() && hotelNameGotten == false) {
	        		hotelNameOnBooking += inputLine;
	        		if (inputLine.contains("</span>")) {
	        			hotelNameOnBooking = hotelNameOnBooking.substring(hotelNameOnBooking.indexOf('>')+1);
	        			hotelNameOnBooking = hotelNameOnBooking.substring(0, hotelNameOnBooking.indexOf('<'));
	        			hotelNameGotten = true;
	        		}
	        	}
	        	if (hotelNameGotten == false && inputLine.contains("<span class=\"fn\" id=\"hp_hotel_name\">")) {
	        		hotelNameOnBooking = inputLine;
	        	}
	        	if (!addressBooking.isEmpty() && addressGotten == false) {
					addressBooking += inputLine;
	        		if (inputLine.contains("</span>")) {
	        			addressBooking = addressBooking.substring(addressBooking.indexOf('>')+1);
	        			addressBooking = addressBooking.substring(0, addressBooking.indexOf('<'));
	        			addressGotten = true;
	        			addressBooking = addressBooking.replace(",", "");
	        		}
	        	}
	        	if (addressGotten == false && inputLine.contains("<span class=\"hp_address_subtitle")) {
	        		addressBooking = inputLine;
	        	}
				
	        }
		}
		catch (FileNotFoundException fnfe) {
			System.out.println("Broken url: "+url);
			return false;
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] hotelNameWords = hotelNameOnBooking.split("\\s");
		for (int i=0; i<hotelNameWords.length; i++) {
			if (hotelName.toLowerCase().contains(hotelNameWords[i].toLowerCase())) score++;
		}
		if (hotelNameWords.length == 1 ||  score < (hotelNameWords.length/2 == 1 ? 2 : hotelNameWords.length/2)) {
			passed = false;	
		}
		else passed = true;
		if (passed == true) {
			score = 0;
			String[] addressWords = addressBooking.split("\\s");
			for (int i=0; i<addressWords.length; i++) {
				if (address.toLowerCase().contains(addressWords[i].toLowerCase())) score++;
			}
			if (score < addressWords.length/2) {
				passed = false;	
			}
			else passed = true;
		}
		return passed;
		
		
	}
	private ArrayList<String> getImageUrls(String url) {
		ArrayList<String> imageUrls = new ArrayList<String>();
		String temp = "";
		try {
			URL yahoo = new URL(url);
			URLConnection yc = yahoo.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
	                yc.getInputStream(), "UTF-8"));
	        String inputLine;
	        
	        while ((inputLine = in.readLine()) != null) {
	        	if (inputLine.contains("src=\"//images.trvl-media.com/hotels/")) {
	        		temp = inputLine.substring(inputLine.indexOf('"')+1);
	        		temp = temp.substring(0, temp.indexOf('"'));
	        		temp = "http:"+temp;
	        		System.out.println(temp);
	        		imageUrls.add(temp);
	        	}
	        	if (inputLine.contains("<span itemprop=\"telephone\"")) {
	        		temp = inputLine.substring(inputLine.indexOf('>')+1);
	        		temp = temp.substring(0,temp.indexOf('<'));
	        		phone = temp;
	        	}
	        	if (longitude == 0.0) {
	        		if (inputLine.contains("center=")) {
		        		temp = inputLine.substring(inputLine.indexOf("center=")+7);
		        		temp = temp.substring(0, temp.indexOf('&'));
		        		latitude = Double.parseDouble(temp.substring(0,temp.indexOf(',')));
		        		longitude = Double.parseDouble(temp.substring(temp.indexOf(',')+1));
		        	}
	        	}
	        }
	            
	        in.close();
		} catch (FileNotFoundException fnf) {
			
			System.out.println(url+" is not valid anymore");
			System.out.println("With id "+id);
			
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showConfirmDialog(null, "Whoops change ip!!");
			getImageUrls(url);
		}
		return imageUrls;
        
    }
}
