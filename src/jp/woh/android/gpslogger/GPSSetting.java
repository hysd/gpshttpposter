package jp.woh.android.gpslogger;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class GPSSetting {
	
	private SharedPreferences sharedPreferences;
	
	public boolean keepScreen;
	public boolean followMe;
	public int saveInterval;
	public int saveDistance;

	public boolean usePost;
	public int postInterval;
	public int postTimeout;
	public String postUrl;
	public String postUserId;
	public String postPassword;
	public String postCharset;
	public int postLimit;
	public boolean postZip;

	public boolean useFtp;
	public String ftpHost;
	public int ftpPort;
	public String ftpRemoteDir;
	public String ftpFilename;
	public String ftpUserId;
	public String ftpPassword;
	public int ftpInterval;
	public int ftpTimeout;
	boolean ftpPassive;
	
	public GPSSetting(Context context) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	private int getSharedPreferencesInt(String name,int defaultValue){
		String value = sharedPreferences.getString(name, String.valueOf(defaultValue));
		if(value.length() == 0) return defaultValue;
		return Integer.parseInt(value);
	}
	
	public void readConfig() {
		saveInterval = getSharedPreferencesInt("saveInterval", 10);
		saveDistance = getSharedPreferencesInt("saveDistance", 100);
		keepScreen = sharedPreferences.getBoolean("keepScreen", false);
		followMe = sharedPreferences.getBoolean("followMe",true);

		usePost = sharedPreferences.getBoolean("usePost",false);
		postUrl = sharedPreferences.getString("postUrl", "");
		postUserId = sharedPreferences.getString("postUserId", "");
		postPassword = sharedPreferences.getString("postPassword", "");
		postInterval = getSharedPreferencesInt("postInterval", 10);
		postTimeout = getSharedPreferencesInt("postTimeout", 0);
		postLimit = getSharedPreferencesInt("postLimit", 500);
		postCharset = sharedPreferences.getString("postCharset", "UTF-8");
		postZip = sharedPreferences.getBoolean("postZip", false);
		
		useFtp = sharedPreferences.getBoolean("useFtp",false);
		ftpHost = sharedPreferences.getString("ftpHost", "");
		ftpPort = getSharedPreferencesInt("ftpPort", 21);
		ftpRemoteDir = sharedPreferences.getString("ftpRemoteDir", "");
		ftpFilename = sharedPreferences.getString("ftpFilename", "");
		ftpUserId = sharedPreferences.getString("ftpUserId", "");
		ftpPassword = sharedPreferences.getString("ftpPassword", "");
		ftpInterval = getSharedPreferencesInt("ftpInterval", 10);
		ftpTimeout = getSharedPreferencesInt("ftpTimeout", 0);
		ftpPassive = sharedPreferences.getBoolean("ftpPassive", true);
		
		Log.d("GPSLogger",
				"saveInterval: " + saveInterval +
				",followMe: " + String.valueOf(followMe) + 
				",usePost: " + String.valueOf(usePost) +
				",postInterval: " + postInterval +
				",useFtp: " + String.valueOf(useFtp) +
				",ftpInterval: " + ftpInterval 
		);
	}
	
}
