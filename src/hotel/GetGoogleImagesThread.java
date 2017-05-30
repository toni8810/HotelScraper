package hotel;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class GetGoogleImagesThread extends Thread {
	private String ref;
	private int id;
	public GetGoogleImagesThread(String ref, int id) {
		this.ref = ref;
		this.id = id;
	}
	public void run() {
		 try {
			URL url = new URL("https://maps.googleapis.com/maps/api/place/photo?maxwidth=1200&photoreference="+ref+"&key=AIzaSyBpFjt8Z0n_g06deDBw6IIaYP4A9PW0P8A");
			InputStream in = new BufferedInputStream(url.openStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1!=(n=in.read(buf)))
			 {
			    out.write(buf, 0, n);
			 }
			out.close();
			in.close();
			Random rand = new Random();
			String fileName = String.valueOf(rand.nextInt(1000000) + 1);
			File image = new File("c:/images/hotels/"+id);
			if(image.mkdir()) {
				try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelrentaldetails","root","");
					 Statement s = c.createStatement()) {
						
						s.executeUpdate("INSERT INTO images VALUES ("+id+",'downloaded')");
						System.out.println(id+" started");
						
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			image = new File("c:/images/hotels/"+id+"/"+fileName+".jpg");
			byte[] response = out.toByteArray();
			FileOutputStream fos = new FileOutputStream(image);
			fos.write(response);
			fos.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}
}
