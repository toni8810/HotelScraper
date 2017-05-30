package hotel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;


public class DBManager {
	private static Connection c;
	private static Statement s;
	
	
	public static void establishConnection() {
		try {
			c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelrentaldetails11","root","");
			s = c.createStatement();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static boolean insertIntoUrls(String url, int id) {
		try {
			s.executeUpdate("INSERT INTO urls VALUES ('"+url+"',"+id+")");
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	public static void deleteFromUrls(int id) {
		try {
			s.executeUpdate("DELETE FROM urls WHERE id = "+id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void insertIntoAmenities(int id, String highlights, String aboutTheProperty, String thingsToDo, String roomTypes, String inYourRoom, String internet, String services, String officialDes, String addInfo, String alsoKnownAs, String hotelStyle) throws MySQLIntegrityConstraintViolationException, SQLException {
		
		s.executeUpdate("INSERT INTO amenities VALUES ("+id+",\""+highlights+"\",\""+aboutTheProperty+"\",\""+thingsToDo+"\",\""+roomTypes+"\",\""+inYourRoom+"\",\""+internet+"\",\""+services+"\",\""+officialDes+"\",\""+addInfo+"\",\""+alsoKnownAs+"\",\""+hotelStyle+"\")");
	}
	public static void insertIntoBasics(int id, String hotelName, String address, String city, String area, String location, String country, String style, int price, String star, double latitude, double longitude, String accommodation) throws MySQLIntegrityConstraintViolationException, SQLException {
		s.executeUpdate("INSERT INTO basics (id,name,address,city,area,location,country,style,priceInGBP,category,latitude,longitude,accomodation) "
				+ "VALUES ("+id+",\""+hotelName+"\",\""+address+"\",\""+city+"\",\""+area+"\",\""+location+"\",\""+country+"\",\""+style+"\","+price+",\""+star+"\","+latitude+","+longitude+",\""+accommodation+"\")");
	}
	public static void insertIntoReviews(int id, int numOfReviews, double userRating) throws MySQLIntegrityConstraintViolationException, SQLException {
		s.executeUpdate("INSERT INTO reviews VALUES ("+id+","+numOfReviews+","+userRating+")");
	}
	public static void deleteFromUrlsWhereUrl(String url) {
		try {
			s.executeUpdate("DELETE FROM urls WHERE url = '"+url+"'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static synchronized void insertIntoImages(int id,String[] images) {
		for (int i=0; i<images.length; i++) {
			try {
				s.executeUpdate("INSERT INTO images VALUES ("+id+",'"+images[i]+"')");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public static synchronized void insertIntoImagesTripadvisor(int id,ArrayList<String> images) {
		for (int i=0; i<images.size(); i++) {
			try {
				s.executeUpdate("INSERT INTO imagestripadvisor VALUES ("+id+",'"+images.get(i)+"')");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public static void updateWebsiteAndPhoneNumber(int id, String website, String phoneNumber) {
		try {
			s.executeUpdate("UPDATE basics SET phone='"+phoneNumber+"', website='"+website+"' WHERE id = "+id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void updateLongAndLat(double longitude, double latitude, int id) {
		try {
			s.executeUpdate("UPDATE basics SET latitude="+latitude+", longitude="+longitude+" WHERE id = "+id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static boolean isPhoneNumberUnknown(int id) {
		boolean returnBoolean = false;
		try {
			Statement sQuery = c.createStatement();
			ResultSet rs = sQuery.executeQuery("SELECT phone FROM basics WHERE id ="+id);
			rs.first();
			if (rs.getString("phone").contentEquals("unknown")) returnBoolean = true;
			else returnBoolean = false;
			rs.close();
			sQuery.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnBoolean;
	}
	public static void updatePhoneNumber(int id, String phoneNumber) {
		try {
			s.executeUpdate("UPDATE basics SET phone='"+phoneNumber+"' WHERE id = "+id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static synchronized void insertIntoImagesFromExpedia(int id, ArrayList<String> urls) {
		try {
			for (int i=0; i<urls.size(); i++) {
				s.executeUpdate("INSERT INTO imagesfromexpedia VALUES ("+id+",'"+urls.get(i)+"')");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void insertIntoExpediaUrls(int id, String url) {
		try {
			s.executeUpdate("INSERT INTO expediaurls VALUES ("+id+",'"+url+"')");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static synchronized void insertIntoImagesFromBooking(int id, ArrayList<String> urls) {
		try {
			for (int i=0; i<urls.size(); i++) {
				s.executeUpdate("INSERT INTO imagesfrombooking VALUES ("+id+",'"+urls.get(i)+"')");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static synchronized void insertIntoImageReferencesGoogle(int id, ArrayList<String> refs) {
		try {
			for (int i=0; i<refs.size(); i++) {
				s.executeUpdate("INSERT INTO imagereferencesgoogle VALUES ("+id+",'"+refs.get(i)+"')");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static synchronized void insertIntoBookingUrls(int id, String url) {
		try {
			s.executeUpdate("INSERT INTO bookingurls VALUES ("+id+",'"+url+"')");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void closeConnection() {
		try {
			s.close();
			c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
