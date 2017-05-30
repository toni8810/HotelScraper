package hotel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class LongLatGetterThread extends Thread {
	private int id;
	private String url;
	private String address;
	private double longitude;
	private double latitude;
	private String name;
	private String phone;
	private String website;
	private ArrayList<String> photoRefs = new ArrayList<String>();
	//Moms api key = AIzaSyA3jVGBm19Yy68WsPZirYl5jh1D9xjOzB8
	//My api key = AIzaSyBpFjt8Z0n_g06deDBw6IIaYP4A9PW0P8A
	// employers api key: AIzaSyA0zlIBPxtGk-5963Yn_ZTGC5MxVGaLiMA
	private final String API_KEY = "AIzaSyA3jVGBm19Yy68WsPZirYl5jh1D9xjOzB8";
	public LongLatGetterThread(int id, String url,String address,String name, String phone, String website) {
		this.id = id;
		this.url = url;
		this.address = address;
		this.name = name;
		this.phone = phone;
		this.website = website;
	}
	public void run() {
		getLoationWithGoogleAPI();
		//getLoationWithGoogleAPIQuery();
		if (longitude != 0.0) {
			DBManager.updateLongAndLat(longitude, latitude, id);
			System.out.println(id+" long and lat Updated");
		}
		if (!phone.isEmpty() || !website.isEmpty()) {
			DBManager.updateWebsiteAndPhoneNumber(id, website, phone);
			System.out.println(id+" phone or website or both Updated");
		}
		if (photoRefs.size() > 0) {
			DBManager.insertIntoImageReferencesGoogle(id, photoRefs);
			System.out.println(id+" phote references inserted");
		} 
	
	}
	private void getLocationFromTripAdvisor() {
		HtmlUnitDriver driver = new HtmlUnitDriver(false);
		driver.get(url);
		try {
			WebElement temp = driver.findElement(By.id("NEARBY_TAB")).findElement(By.className("mapContainer"));
			longitude = Double.parseDouble(temp.getAttribute("data-lng"));
			latitude = Double.parseDouble(temp.getAttribute("data-lat"));
		}
		catch (NoSuchElementException nsee) {
			longitude = 0;
			latitude = 0;
		}
		driver.close();
		driver.quit();
		if (longitude != 0) {
			DBManager.updateLongAndLat(longitude, latitude, id);
			System.out.println(id+" Updated");
		}
	}
	private void getLoationWithGoogleAPI() {
		//https://maps.googleapis.com/maps/api/geocode/xml?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=AIzaSyBpFjt8Z0n_g06deDBw6IIaYP4A9PW0P8A
		String temp;
		try {
			String addressEncoded = URLEncoder.encode(address, "UTF-8");
			String googleUrl = "https://maps.googleapis.com/maps/api/geocode/xml?address="+addressEncoded+"&key="+API_KEY;
			URL url = new URL(googleUrl);
			URLConnection urlC = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
	                urlC.getInputStream(), "UTF-8"));
			String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	        	if (inputLine.contains("<status>ZERO_RESULTS</status>")) {
	        		longitude = 0;
	    			latitude = 0;
	    			//getLoationWithGoogleAPIQuery();
	    			break;
	    			
	        	}
	        	if (inputLine.contains("<status>OVER_QUERY_LIMIT</status>")) {
	        		longitude = 0;
	    			latitude = 0;
	    			System.out.println("Over the limit");
	    			break;
	        	}
	        	if (inputLine.contains("<status>REQUEST_DENIED</status>")) {
	        		longitude = 0;
	    			latitude = 0;
	    			System.out.println("Request Denied");
	    			break;
	        	}
	        	if (inputLine.contains("<status>INVALID_REQUEST</status>")) {
	        		longitude = 0;
	    			latitude = 0;
	    			System.out.println("Invalid Request");
	    			break;
	        	}
	        	if (inputLine.contains("<status>UNKNOWN_ERROR</status>")) {
	        		longitude = 0;
	    			latitude = 0;
	    			System.out.println("Unknown Error");
	    			break;
	        	}
	        	if (inputLine.contains("<lat>")) {
	        		temp = inputLine.substring(inputLine.indexOf('>')+1);
	        		temp = temp.substring(0, temp.indexOf('<'));
	        		latitude = Double.parseDouble(temp);
	        	}
	        	if (inputLine.contains("<lng>")) {
	        		temp = inputLine.substring(inputLine.indexOf('>')+1);
	        		temp = temp.substring(0, temp.indexOf('<'));
	        		longitude = Double.parseDouble(temp);
	        	}
	        	if (latitude != 0.0 && longitude != 0.0) break;
	        }
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void getLoationWithGoogleAPIQuery() {
		//https://maps.googleapis.com/maps/api/geocode/xml?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=AIzaSyBpFjt8Z0n_g06deDBw6IIaYP4A9PW0P8A
		String temp;
		try {
			String addressEncoded = URLEncoder.encode(name+" "+address, "UTF-8");
			String googleUrl = "https://maps.googleapis.com/maps/api/place/textsearch/xml?query="+addressEncoded+"&key="+API_KEY;
			URL url = new URL(googleUrl);
			URLConnection urlC = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
	                urlC.getInputStream(), "UTF-8"));
			String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	        	if (inputLine.contains("<status>ZERO_RESULTS</status>")) {
	        		longitude = 0;
	    			latitude = 0;
	    			break;
	    			
	        	}
	        	if (inputLine.contains("<status>OVER_QUERY_LIMIT</status>")) {
	        		longitude = 0;
	    			latitude = 0;
	    			System.out.println("Over the limit");
	    			break;
	        	}
	        	if (inputLine.contains("<status>REQUEST_DENIED</status>")) {
	        		longitude = 0;
	    			latitude = 0;
	    			System.out.println("Request Denied");
	    			break;
	        	}
	        	if (inputLine.contains("<status>INVALID_REQUEST</status>")) {
	        		longitude = 0;
	    			latitude = 0;
	    			System.out.println("Invalid Request");
	    			break;
	        	}
	        	if (inputLine.contains("<status>UNKNOWN_ERROR</status>")) {
	        		longitude = 0;
	    			latitude = 0;
	    			System.out.println("Unknown Error");
	    			break;
	        	}
	        	if (latitude == 0.0)
	        	if (inputLine.contains("<lat>")) {
	        		temp = inputLine.substring(inputLine.indexOf('>')+1);
	        		temp = temp.substring(0, temp.indexOf('<'));
	        		latitude = Double.parseDouble(temp);
	        	}
	        	if (longitude == 0.0)
	        	if (inputLine.contains("<lng>")) {
	        		temp = inputLine.substring(inputLine.indexOf('>')+1);
	        		temp = temp.substring(0, temp.indexOf('<'));
	        		longitude = Double.parseDouble(temp);
	        	}
	        	if (inputLine.contains("<place_id>")) {
	        		temp = inputLine.substring(inputLine.indexOf('>')+1);
	        		temp = temp.substring(0, temp.indexOf('<'));
	        		getPhoneAndWebsite(temp);
	        	}
	        	
	        	
	        }
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void getPhoneAndWebsite(String placeId) {
		String temp;
		try {
			String googleUrl = "https://maps.googleapis.com/maps/api/place/details/xml?placeid="+placeId+"&key="+API_KEY;
			URL url = new URL(googleUrl);
			URLConnection urlC = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
                urlC.getInputStream(), "UTF-8"));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains("<international_phone_number>")) {
					temp = inputLine.substring(inputLine.indexOf('>')+1);
	        		temp = temp.substring(0, temp.indexOf('<'));
	        		phone = temp;
				}
				if (inputLine.contains("<photo_reference>")) {
					temp = inputLine.substring(inputLine.indexOf('>')+1);
	        		temp = temp.substring(0, temp.indexOf('<'));
	        		photoRefs.add(temp);
				}
				if (inputLine.contains("<website>")) {
					temp = inputLine.substring(inputLine.indexOf('>')+1);
	        		temp = temp.substring(0, temp.indexOf('<'));
	        		website = temp;
				}
			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}
}
