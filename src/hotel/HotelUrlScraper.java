package hotel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

public class HotelUrlScraper extends Thread {
	private String countryLink;
	private String countryName;
	public HotelUrlScraper(String countryLink, String countryName) {
		this.countryLink = countryLink;
		this.countryName = countryName;
	}
	public void run() {
		countryName = countryName.toLowerCase().replace(" ", "");
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelurls","root","");
			 Statement s = c.createStatement()) {
			
			s.executeUpdate("CREATE TABLE "+countryName+"_hotel (url varchar(200),PRIMARY KEY (url));");
			
		}
		catch (MySQLSyntaxErrorException msee) {
			return;
		}
		catch (SQLException e1) {
			e1.printStackTrace();
		}
		System.out.println(countryName+" starting");
		String temp;
		ArrayList<String> hotelUrls = new ArrayList<String>();
		HtmlUnitDriver driver = new HtmlUnitDriver(false);
		driver.get(countryLink);
		List<WebElement> tempList = null;
		while (ThreadController.okay == false) {
			try {
				sleep(1000*10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			//get .//*[@id='BODYCON']/table[2] as well
			tempList = driver.findElement(By.xpath(".//*[@id='BODYCON']/table[1]")).findElements(By.tagName("a"));
		}
		catch (NoSuchElementException nsee) {
			ThreadController.okay = false;
			JOptionPane.showConfirmDialog(null, "Human interaction needed\n"+countryLink);
			try {
				sleep(1000*60*2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			driver.get(countryLink);
			tempList = driver.findElement(By.xpath(".//*[@id='BODYCON']/table[1]")).findElements(By.tagName("a"));
			ThreadController.okay = true;
		}
		ArrayList<String> upperLevel = new ArrayList<String>();
		ArrayList<String> lowerLevel = new ArrayList<String>();
		List<WebElement> pageList;
		ArrayList<String> pageStringList = new ArrayList<>();
		//Level 1
		for (int i=0; i<tempList.size(); i++) {
			upperLevel.add(tempList.get(i).getAttribute("href"));
		}
		for (int i=0; i<6; i++) {
			for (int k=0; k<upperLevel.size(); k++) {
				try {
				driver.get(upperLevel.get(k));
				}
				catch (WebDriverException wde) {
					System.out.println(wde.getMessage());
					continue;
				}
				if (!driver.getCurrentUrl().startsWith("https://www.tripadvisor.co.uk/AllLocations")) {
					upperLevel.remove(k);
					continue;
				}
				
				try {
					tempList = driver.findElement(By.xpath(".//*[@id='BODYCON']/table[1]")).findElements(By.tagName("a"));
				}
				catch (NoSuchElementException nsee) {
					try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelurls","root","");
							 Statement s = c.createStatement()) {
							
							s.executeUpdate("DROP TABLE "+countryName+"_hotel");
							System.out.println(countryName+" dropped");
							
						}
						catch (SQLException e1) {
							e1.printStackTrace();
						}
					return;
				}
					try {
						pageStringList.clear();
						pageList = driver.findElement(By.xpath(".//*[@id='BODYCON']/div[2]")).findElements(By.tagName("a"));
						for (int m=0; m<pageList.size(); m++) {
							pageStringList.add(pageList.get(m).getAttribute("href"));
						}
					}
					catch (NoSuchElementException nsee) {
						pageStringList.add("one page");
					}
					for (int m=0; m<pageStringList.size(); m++) {
						for (int l=0; l<tempList.size(); l++) {
							temp = tempList.get(l).getAttribute("href");
							if (temp.startsWith("https://www.tripadvisor.co.uk/Hotel_Review")) {
								hotelUrls.add(temp);
								System.out.println(temp+" on page "+m);
								continue;
							}
							if (temp.startsWith("https://www.tripadvisor.co.uk/AllLocations")) {
								lowerLevel.add(temp);
							}
						}
						if (!pageStringList.get(m).contentEquals("one page")) {
							driver.get(pageStringList.get(m));
							tempList = driver.findElement(By.xpath(".//*[@id='BODYCON']/table[1]")).findElements(By.tagName("a"));
						}
					}
			}
			upperLevel.clear();
			upperLevel.addAll(lowerLevel);
			lowerLevel.clear();
		}
		driver.close();
		driver.quit();
		System.out.println(countryName+" Hotel urls "+hotelUrls.size());
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelurls","root","");
			 Statement s = c.createStatement()) {
			
			for (int i=0; i<hotelUrls.size(); i++) {
				try {
					s.executeUpdate("INSERT INTO "+countryName+"_hotel VALUES('"+hotelUrls.get(i)+"')");
				}
				catch(SQLException sqle) {
					if (sqle.getMessage().contains("Duplicate entry")) {
						
					}
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(countryName+" done");
	}
}
