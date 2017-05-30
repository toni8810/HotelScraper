package hotel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
	private static AtomicInteger idCounter = new AtomicInteger();

	public static int createID()
	{
	    return idCounter.getAndIncrement();
	}
	public static void setBase() {
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelrentaldetails","root","");
			 Statement s = c.createStatement();
			 ResultSet rs = s.executeQuery("SELECT id FROM urls ORDER BY id ASC")) {
			
			if (rs.last()) {
				idCounter.set(rs.getInt("id"));
				idCounter.getAndIncrement();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
