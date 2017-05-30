package hotel;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class GetImagesFromTripadvisor extends Thread {
	
	private String url;
	private int id;
	
	public GetImagesFromTripadvisor(String url,int id) {
		this.url = url;
		this.id = id;
	}
	
	public void run() {
		HtmlUnitDriver driver = new HtmlUnitDriver(false);
		String tempString;
		WebElement temp;
		ArrayList<String> imageUrls = new ArrayList<String>();
		boolean noMoreImages = false;
		driver.get(url);
		List<WebElement> tempList = driver.findElements(By.className("carouselPhoto"));
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
		DBManager.insertIntoImagesTripadvisor(id, imageUrls);
		System.out.print(id%10==0 ? id+" done\n" : id+" done, ");
	}
	
}
