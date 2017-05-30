package hotel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


import rental.RentalScraperThread;

public class MainClass {
	
	public static void main(String[] args) throws InterruptedException, IOException {
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		/*int counter = 0;
		ArrayList<String> doneTables = new ArrayList<String>();
		IdGenerator.setBase();
		DBManager.establishConnection();
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelurls","root","");
			 Statement s = c.createStatement();
			 Statement sUpdate = c.createStatement();
			 ResultSet rs = s.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA='hotelurls' AND TABLE_NAME = 'spain_hotel' ")) {
			
			ExecutorService es = Executors.newFixedThreadPool(25);
			Statement s2 = c.createStatement();
			ResultSet rs2 = s2.executeQuery("SELECT name FROM hotelrentaldetails.tables_done");
			while (rs2.next()) {
				doneTables.add(rs2.getString("name"));
			}
			while(rs.next()) {
				if (counter > 20000) break;
				if (doneTables.contains(rs.getString("TABLE_NAME"))) continue;
				rs2 = s2.executeQuery("SELECT url FROM "+rs.getString("TABLE_NAME"));
				while (rs2.next()) {
					counter++;
					es.execute(new IndividualHotelDetailsScraperThread(rs2.getString("url")));
					Thread.sleep(500);
				}
				sUpdate.executeUpdate("INSERT INTO hotelrentaldetails.tables_done VALUES('"+rs.getString("TABLE_NAME")+"')");
			}
			rs2.close();
			s2.close();
			es.shutdown();
			es.awaitTermination(20, TimeUnit.HOURS);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		Thread.sleep(10000);
		DBManager.closeConnection();
		Thread.sleep(5000);
		Runtime.getRuntime().exec("shutdown -s -t 10"); */
		//scrapeRentals();
		/*for (int i=0; i<3; i++) {
			getWebsiteAndPhone();
			System.gc();
		} 
		Runtime.getRuntime().exec("shutdown -s -t 10"); */
		//scrapeYellowPages();
		//getImagesFromExpedia();
		//Runtime.getRuntime().exec("shutdown -s -t 10");
		getLongLatWithGoogleAPI();
		//getImagesWithGoogleAPI();
		//new hotels from 1056963
		//getHotelRentalURLs();
		//crawlNewHotels();
		//getImagesFromTripadvisor();
		
		
	}
	private static void getImagesFromTripadvisor() throws InterruptedException {
		DBManager.establishConnection();
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelrentaldetails","root","");
			 Statement s = c.createStatement();
			 ResultSet rs = s.executeQuery("SELECT * FROM urls WHERE id >= 2000 AND id < 3000 AND id NOT IN (SELECT id FROM imagesfrombooking) AND id NOT IN (SELECT id FROM imagesfromexpedia) ORDER BY id")) {
			
			ExecutorService es = Executors.newFixedThreadPool(10);
			while (rs.next()) {
				es.execute(new GetImagesFromTripadvisor(rs.getString("url"), rs.getInt("id")));
				Thread.sleep(200);
			}
			es.shutdown();
			es.awaitTermination(20, TimeUnit.HOURS);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread.sleep(5000);
		DBManager.closeConnection();
		Thread.sleep(10000);
		
		//Runtime.getRuntime().exec("shutdown -s -t 10");
	}
	private static void crawlNewHotels() throws InterruptedException, IOException {
		DBManager.establishConnection();
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelrentaldetails","root","");
			 Statement s = c.createStatement();
			 ResultSet rs = s.executeQuery("SELECT * FROM urls WHERE id >= 1240000 AND id < 1250000 ORDER BY id ")) {
			
				ExecutorService es = Executors.newFixedThreadPool(10);
				//15 threads 1.36/second
				while (rs.next()) {
					es.execute(new IndividualHotelDetailsScraperThread(rs.getString("url"), rs.getInt("id")));
					Thread.sleep(200);
				}
				es.shutdown();
				es.awaitTermination(20, TimeUnit.HOURS);
						
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		Thread.sleep(5000);
		DBManager.closeConnection();
		Thread.sleep(10000);
		
		//Runtime.getRuntime().exec("shutdown -s -t 10");
	}
	private static void getLongLatWithGoogleAPI() throws InterruptedException {
		DBManager.establishConnection();
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelrentaldetails11","root","");
			 Statement s = c.createStatement();
			 ResultSet rs = s.executeQuery("SELECT address,id,name,phone,website FROM basics WHERE latitude = 0 AND address != 'unknown' AND id > 1223000 AND id < 1243000 ORDER BY id ")) { //geo from 1223000 to 1243000 places from 1226226 to 1250000 
					ExecutorService es = Executors.newFixedThreadPool(2);
					while (rs.next()) {
						es.execute(new LongLatGetterThread(rs.getInt("id"), "", rs.getString("address"), rs.getString("name"), rs.getString("phone"), rs.getString("website")));
						Thread.sleep(200);
					}
					es.shutdown();
					es.awaitTermination(20, TimeUnit.HOURS);
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Thread.sleep(5000);
				DBManager.closeConnection();
				Thread.sleep(10000);
	}
	private static void getImagesFromExpedia() throws InterruptedException, IOException {
		ArrayList<Integer> expediaIds = new ArrayList<Integer>();
		DBManager.establishConnection();
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelrentaldetails","root","");
			Statement s = c.createStatement()) {
					
				ResultSet rs = s.executeQuery("SELECT id FROM expediaurls");
				while (rs.next()) {
					expediaIds.add(rs.getInt("id"));
				}
				rs = s.executeQuery("SELECT name,address,id,country,longitude FROM basics WHERE id > 1240482 AND id <= 1250482");
				ExecutorService es = Executors.newFixedThreadPool(5);
				while (rs.next()) {
					if (expediaIds.contains(rs.getInt("id"))) continue;
					es.execute(new GetImagesThread(rs.getInt("id"),rs.getString("name"),rs.getString("address"),rs.getString("country"),rs.getDouble("longitude")));
					Thread.sleep(200);
				}
				es.shutdown();
				es.awaitTermination(10, TimeUnit.HOURS);
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Thread.sleep(5000);
				DBManager.closeConnection();
				Thread.sleep(10000);
				Runtime.getRuntime().exec("shutdown -s -t 10");
	}
	private static void getWebsiteAndPhone() throws InterruptedException, IOException {
		//int counter = 0;
		DBManager.establishConnection();
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelrentaldetails","root","");
			 Statement s = c.createStatement();
			 Statement sUpdate = c.createStatement();
			//SELECT * FROM `basics` WHERE country = "Canada" AND (phone != "unknown" OR website != "unknown") 
			 ResultSet rs = s.executeQuery("SELECT name,address,id FROM basics WHERE phone='unknown' AND website='unknown' AND id > 97476 AND id < 102000 ORDER BY id ")) {
			//next 1 launch
			ExecutorService es = Executors.newFixedThreadPool(25);
			while (rs.next()) {
				//if (rs.getInt("id") < 200000) continue;
				//counter++;
				//if (counter > 40000) break;
				es.execute(new WebsiteAndPhoneNumber(rs.getString("name"), rs.getString("address"), rs.getInt("id")));
			}
			es.shutdown();
			es.awaitTermination(20, TimeUnit.HOURS);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread.sleep(5000);
		DBManager.closeConnection();
		Thread.sleep(10000);
		
		
	}
	private static void scrapeRentals() throws InterruptedException, IOException {
		int counter = 0;
		ArrayList<String> doneTables = new ArrayList<String>();
		IdGenerator.setBase();
		DBManager.establishConnection();
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelurls","root","");
			 Statement s = c.createStatement();
			 Statement sUpdate = c.createStatement();
			 ResultSet rs = s.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA='hotelurls' AND TABLE_NAME LIKE '%_rental' ")) {
			
			ExecutorService es = Executors.newFixedThreadPool(14);
			Statement s2 = c.createStatement();
			ResultSet rs2 = s2.executeQuery("SELECT name FROM hotelrentaldetails.tables_done");
			while (rs2.next()) {
				doneTables.add(rs2.getString("name"));
			}
			while(rs.next()) {
				if (counter > 20000) break;
				if (doneTables.contains(rs.getString("TABLE_NAME"))) continue;
				rs2 = s2.executeQuery("SELECT url FROM "+rs.getString("TABLE_NAME"));
				while (rs2.next()) {
					counter++;
					es.execute(new RentalScraperThread(rs2.getString("url")));
					Thread.sleep(500);
				}
				sUpdate.executeUpdate("INSERT INTO hotelrentaldetails.tables_done VALUES('"+rs.getString("TABLE_NAME")+"')");
			}
			rs2.close();
			s2.close();
			es.shutdown();
			es.awaitTermination(20, TimeUnit.HOURS);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		Thread.sleep(10000);
		DBManager.closeConnection();
		Thread.sleep(5000);
		Runtime.getRuntime().exec("shutdown -s -t 10");
	}
	private static void scrapeYellowPages() throws InterruptedException {
		DBManager.establishConnection();
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelrentaldetails","root","");
			 Statement s = c.createStatement();
			 ResultSet rs = s.executeQuery("SELECT id,name,address FROM basics WHERE country = 'England' AND phone='unknown' AND website='unknown' ")) {
			
			ExecutorService es = Executors.newFixedThreadPool(1);
			while(rs.next()) {
				es.execute(new YellowPagesScraper(rs.getString("name"), rs.getString("address"), rs.getInt("id")));
			}
			es.shutdown();
			es.awaitTermination(20, TimeUnit.HOURS);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		Thread.sleep(10000);
		DBManager.closeConnection();
		Thread.sleep(5000);
	}
	private static void getImagesWithGoogleAPI() {
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelrentaldetails","root","");
			 Statement s = c.createStatement();
			 ResultSet rs = s.executeQuery("SELECT * FROM imagereferencesgoogle WHERE id NOT IN (SELECT DISTINCT id FROM images)")) {
			
			ExecutorService es = Executors.newFixedThreadPool(5);
			
			while (rs.next()) {
				es.execute(new GetGoogleImagesThread(rs.getString("ref"), rs.getInt("id")));
				Thread.sleep(200);
			}
			
			es.shutdown();
			es.awaitTermination(10, TimeUnit.HOURS);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void getHotelRentalURLs() throws IOException {
		String[][] countries = new String[3][2];
		countries[0][0] = "https://www.tripadvisor.co.uk/AllLocations-g186216-c1-Hotels-United_Kingdom.html";
		countries[0][1] = "UnitedKingdom";
		countries[1][0] = "https://www.tripadvisor.co.uk/AllLocations-g191-c1-Hotels-United_States.html";
		countries[1][1] = "UnitedStates";
		countries[2][0] = "";
		countries[2][1] = "";
		for (int i=0; i<3; i++) {
			if (!countries[i][0].isEmpty()) {
				new HotelUrlScraper(countries[i][0], countries[i][1]).run();
				try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelurls","root","");
					 Statement s = c.createStatement();
					 ResultSet rs = s.executeQuery("SELECT * FROM hotelurls."+countries[i][1].toLowerCase()+"_hotel WHERE url NOT IN (SELECT url FROM hotelrentaldetails.urls)")) {
					
					DBManager.establishConnection();
					IdGenerator.setBase();
					
					while(rs.next()) {
						DBManager.insertIntoUrls(rs.getString("url"), IdGenerator.createID());
					}
					Thread.sleep(5000);
					DBManager.closeConnection();
					Thread.sleep(5000);
							 
						 } catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			}
		}
		Runtime.getRuntime().exec("shutdown -s -t 10");
	}

}
