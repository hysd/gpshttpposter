package jp.woh.android.gpslogger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GPSHistoryData {

	public static String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
	public static String FILE_FORMAT = "yyyyMMdd_HHmmss";
	
	private long starttime = 0;
	private long endtime = 0;
	private String title;
	private String description;
	
	public void setStarttime(long time){
		starttime = time;
	}
	
	public void setEndtime(long time){
		endtime = time;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	public long getStarttime(){
		return starttime;
	}
	
	public long getEndtime(){
		return endtime;
	}

	public String getTitle(){
		return title;
	}
	
	public String getDescription(){
		return description;
	}
	
	public String getStarttimeText(){
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		return sdf.format(new Date(starttime)).toString();
	}

	public String getEndtimeText(){
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		return endtime == 0 ? "" : sdf.format(new Date(endtime)).toString();
	}
	
}
