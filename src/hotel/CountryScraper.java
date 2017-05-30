package hotel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class CountryScraper {
	public void scrapeHotelCountries() {
		String temp;
		ArrayList<String> countries = new ArrayList<String>();
		ArrayList<String> countriesName = new ArrayList<String>();
		HtmlUnitDriver driver = new HtmlUnitDriver(false);
		driver.get("https://www.tripadvisor.co.uk/AllLocations-g1-c1-Hotels-World.html");
		List<WebElement> allATags = driver.findElements(By.tagName("a"));
		for (int i=0; i<allATags.size(); i++) {
			temp = allATags.get(i).getText();
			if (temp.startsWith("Lodging in")) {
				countries.add(allATags.get(i).getAttribute("href"));
				countriesName.add(temp.substring(11));
			}
		}
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelurls","root","");
			 Statement statement = c.createStatement()) {
			
			for (int i=0; i<countries.size(); i++) {
				statement.executeUpdate("INSERT INTO countrylist VALUES(\""+countries.get(i)+"\",\"hotel\",\""+countriesName.get(i)+"\")");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public void scrapeRentalCountries() {
		String temp;
		ArrayList<String> countries = new ArrayList<String>();
		ArrayList<String> countriesName = new ArrayList<String>();
		HtmlUnitDriver driver = new HtmlUnitDriver(false);
		driver.get("https://www.tripadvisor.co.uk/AllLocations-g1-c10-VacationRentals-World.html");
		List<WebElement> allATags = driver.findElements(By.tagName("a"));
		for (int i=0; i<allATags.size(); i++) {
			temp = allATags.get(i).getText();
			if (temp.startsWith("Holiday Rentals in")) {
				countries.add(allATags.get(i).getAttribute("href"));
				countriesName.add(temp.substring(19));
			}
		}
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelurls","root","");
			 Statement statement = c.createStatement()) {
			
			for (int i=0; i<countries.size(); i++) {
				statement.executeUpdate("INSERT INTO countrylist VALUES(\""+countries.get(i)+"\",\"rental\",\""+countriesName.get(i)+"\")");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
