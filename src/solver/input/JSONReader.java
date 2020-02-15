package solver.input;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import solver.util.Site;

public class JSONReader {

	
	public static String[] readMapIDsForDay(String url) {
		
		try {
			String inputLine = getJSONContent(url + "?" + Math.random()*5d);
			String[] result = inputLine.split(",");
			for(int i = 0; i<result.length; i++) {
				result[i]=result[i].replace("[", "").replace("]", "");
			}
			return result;
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
	
	
	private static String getJSONContent(String url) {
		try {
			URL oracle = new URL(url);
			HttpURLConnection yc = (HttpURLConnection) oracle.openConnection();
			yc.setRequestMethod("GET");
			yc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0");
			yc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String inputLine=in.readLine();
			in.close();
			return inputLine;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static MazeDescriptor readJSON(Site site, String id) {
		try {
			String inputLine = getJSONContent(site.urlStub + "/a/map/" + id + ".js");
//			if(site==Site.RED) {
//				return V2Parser.createDescriptor(site.toString(), inputLine);
//			}
			return V2Parser.createDescriptor(site.toString(), inputLine);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
