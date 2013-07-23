package jp.woh.android.gpslogger;

public class GPSHttpLocationsResponse {
	private int code = 0;
	private int count = 0;
	private String message = "";
	
	public void setCode(int code){
		this.code = code;
	}
	
	public void setCount(int count){
		this.count = count;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public int getCode(){
		return code;
	}
	
	public int getCount(){
		return count;
	}
	
	public String getMessage(){
		return message;
	}
	
}
