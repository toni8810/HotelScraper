package rental;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import hotel.ThreadController;

public class RentalURLScraper extends Thread {
	private String countryLink;
	private String countryName;
	HtmlUnitDriver driver;
	ArrayList<String> rentalUrls = new ArrayList<String>();
	public RentalURLScraper(String countryLink, String countryName) {
		this.countryLink = countryLink;
		this.countryName = countryName;
	}

	public void run() {
		countryName = countryName.toLowerCase().replace(" ", "");
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelurls","root","");
			 Statement s = c.createStatement()) {
			
			s.executeUpdate("CREATE TABLE "+countryName+"_rental (url varchar(200),PRIMARY KEY (url));");
			
		}
		catch (MySQLSyntaxErrorException msee) {
			return;
		}
		catch (SQLException e1) {
			e1.printStackTrace();
		}
		System.out.println(countryName+" starting");
		String temp;
		driver = new HtmlUnitDriver(false);
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
			try {
				tempList = driver.findElement(By.xpath(".//*[@id='BODYCON']/table[1]")).findElements(By.tagName("a"));
				ThreadController.okay = true;
			}
			catch(NoSuchElementException nsee2) {
				tempList = driver.findElement(By.id("VR_BY_CITY")).findElements(By.tagName("a"));
				ThreadController.okay = true;
			}
			
		}
		ArrayList<String> upperLevel = new ArrayList<String>();
		ArrayList<String> lowerLevel = new ArrayList<String>();
		//Level 1
		for (int i=0; i<tempList.size(); i++) {
			upperLevel.add(tempList.get(i).getAttribute("href"));
		}
		for (int i=0; i<5; i++) {
			for (int k=0; k<upperLevel.size(); k++) {
				driver.get(upperLevel.get(k));
				if (!driver.getCurrentUrl().startsWith("https://www.tripadvisor.co.uk/AllLocations")) {
					if (!driver.getCurrentUrl().startsWith("https://www.tripadvisor.co.uk/VacationRentals")) {
						upperLevel.remove(k);
						continue;
					}
					
				}
				if (driver.getCurrentUrl().startsWith("https://www.tripadvisor.co.uk/AllLocations")) {
					try {
						tempList = driver.findElement(By.xpath(".//*[@id='BODYCON']/table[1]")).findElements(By.tagName("a"));
					}
					catch (NoSuchElementException nsee) {
						try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelurls","root","");
							 Statement s = c.createStatement()) {
							
							s.executeUpdate("DROP TABLE "+countryName+"_rental");
							System.out.println(countryName+" dropped");
							
							}
						catch (SQLException e1) {
							e1.printStackTrace();
						}
						return;
					}
					for (int l=0; l<tempList.size(); l++) {
						temp = tempList.get(l).getAttribute("href");
						if (temp.startsWith("https://www.tripadvisor.co.uk/VacationRentalReview")) {
							rentalUrls.add(temp);
							continue;
						}
						if (temp.startsWith("https://www.tripadvisor.co.uk/AllLocations")) {
							lowerLevel.add(temp);
							continue;
						}
						if (temp.startsWith("https://www.tripadvisor.co.uk/VacationRentals")) {
							setRentalsFromList(temp);
							driver.get(upperLevel.get(k));
							tempList = driver.findElement(By.xpath(".//*[@id='BODYCON']/table[1]")).findElements(By.tagName("a"));
						}
					}
				}
				else {
					setRentalsFromList(upperLevel.get(k));
				}
			}
			upperLevel.clear();
			upperLevel.addAll(lowerLevel);
			lowerLevel.clear();
		}
		driver.close();
		driver.quit();
		System.out.println(countryName+" rental urls "+rentalUrls.size());
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelurls","root","");
			 Statement s = c.createStatement()) {
			
			for (int i=0; i<rentalUrls.size(); i++) {
				try {
					s.executeUpdate("INSERT INTO "+countryName+"_rental VALUES('"+rentalUrls.get(i)+"')");
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
	private void setRentalsFromList(String temp) {
		driver.get(temp);
		List<WebElement> tempList = driver.findElements(By.className("shorterTitleText"));
		for (int i=0; i<tempList.size(); i++) {
			rentalUrls.add(tempList.get(i).getAttribute("href"));
		}
		
		
	}
	
}
