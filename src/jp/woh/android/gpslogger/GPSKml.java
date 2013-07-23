package jp.woh.android.gpslogger;

import android.database.Cursor;
import java.util.Date;
import java.io.File;
import java.io.OutputStreamWriter;  
import java.io.BufferedWriter;
import java.io.FileOutputStream;

public class GPSKml {

	public String getPlacemarkText(String id,String name,String description,String coordinates){
		return 
"<Placemark id=\"" + id + "\">\n" +
"<name><![CDATA[" + name + "]]></name>\n" +
"<description><![CDATA[" + description + "]]></description>\n" +
"<Point>\n" +
"<coordinates>\n" +
coordinates + "\n" +
"</coordinates>\n" +
"</Point>\n" +
"</Placemark>\n";
	}

	public String getRouteText(String id,String name,String description,String coordinates){
		return 
"<Placemark id=\"" + id + "\">\n" +
"<name><![CDATA[" + name + "]]></name>\n" +
"<description><![CDATA[" + description + "]]></description>\n" +
"<styleUrl>#style</styleUrl>\n" +
"<LineString>\n" +
"<tessellate>1</tessellate>\n" +
"<coordinates>\n" +
coordinates + "\n" +
"</coordinates>\n" +
"</LineString>\n" +
"</Placemark>\n";
	}
	
	public String getPlacemarkText(String id,String name,String description,Cursor locations){
		double latitude = locations.getDouble(GPSDatabase.LOCATION_LATITUDE);
		double longitude = locations.getDouble(GPSDatabase.LOCATION_LONGITUDE);
		return getPlacemarkText(id,name,description,longitude + ", " + latitude + ", 0");
	}
	
	public String getStartPoint(Cursor location){
		location.moveToFirst();
		int rowcount = location.getCount();
		if( rowcount == 0|| location.isNull(0) ) return "";
		return getPlacemarkText("start","Start","",location);
	}
	
	public String getEndPoint(Cursor location){
		location.moveToLast();
		int rowcount = location.getCount();
		if( rowcount == 0|| location.isNull(0) ) return "";
		return getPlacemarkText("end","End","",location);
	}
	
	public String getCoordinates(Cursor locations){
		locations.moveToFirst();	
		int rowcount = locations.getCount();
		if( rowcount == 0|| locations.isNull(0) ) return "";
		String coordinates = "";
		for (int i = 0; i < rowcount ; i++) {
			double latitude = locations.getDouble(GPSDatabase.LOCATION_LATITUDE);
			double longitude = locations.getDouble(GPSDatabase.LOCATION_LONGITUDE);
			double altitude = locations.getDouble(GPSDatabase.LOCATION_ALTITUDE);
			coordinates +=  longitude + "," + latitude + "," + altitude + "\n";
			locations.moveToNext();
		}
		return getRouteText("route","Route","",coordinates.trim());
	}
	
	public String getMarkerXML(Cursor markers){
		markers.moveToFirst();
		int rowcount = markers.getCount();
		if( rowcount == 0|| markers.isNull(0) ) return "";
		String xml = "";
		for (int i = 0; i < rowcount ; i++) {
			String logdate = new Date(markers.getLong(GPSDatabase.MARKER_ID)).toLocaleString();
			String title = markers.getString(GPSDatabase.MARKER_TITLE);
			String description = markers.getString(GPSDatabase.MARKER_DESCRIPTION);
			String cdata = "<p>" + description + "</p>" + "<p>" + logdate + "</p>";
			xml += getPlacemarkText("marker_" + i,title,cdata,markers);
			markers.moveToNext();
		}
		return xml;
	}
	
	public String getKML(GPSDatabase db,GPSHistoryData history){
		return getKML(db,history.getStarttime(),history.getEndtime(),history.getTitle(),history.getDescription());
	}
	
	public String getKML(GPSDatabase db,long starttime,long endtime,String title,String description){
		Cursor locations = db.getLocations(starttime, endtime);
		Cursor markers = db.getMarkers(starttime,endtime);

		String startXML = getStartPoint(locations);
		String endXML = getEndPoint(locations);
		String markerXML = getMarkerXML(markers);
		String routeXML = getCoordinates(locations);
		
		locations.close();
		locations = null;
		markers.close();
		markers = null;
		
		
		return 
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<kml xmlns=\"http://earth.google.com/kml/2.0\">\n" +
"<Document>\n" +
"<Style id=\"style\">\n" +
"<LineStyle>\n" +
"<color>990000FF</color>\n" +
"<width>5</width>\n" +
"</LineStyle>\n" +
"</Style>\n" +
	endXML + 
	markerXML +
	startXML +
	routeXML +
"</Document>\n" +
"</kml>\n";
	}
	
	public boolean outputKML(String filepath,GPSDatabase db,long starttime,long endtime,String title,String description){
		File file = new File(filepath);
//		file.createNewFile();
		return outputKML(file,db,starttime,endtime,title,description);
	}
	
	public boolean outputKML(File file,GPSDatabase db,long starttime,long endtime,String title,String description){
		String kml = getKML(db,starttime,endtime,title,description);
		
		FileOutputStream os = null;
		BufferedWriter bw = null;  
		try{
			os = new FileOutputStream(file.getPath());
			bw = new BufferedWriter(new OutputStreamWriter(os));
			bw.write(kml);
			bw.flush();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			if (bw != null)try{ bw.close(); }catch( Exception e ){}
		}
		
	}

}
