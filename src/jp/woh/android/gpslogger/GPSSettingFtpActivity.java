package jp.woh.android.gpslogger;

import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.util.Log;

public class GPSSettingFtpActivity extends PreferenceActivity{
	private static final int MENU_ID_TEST = (Menu.FIRST + 1);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settingftp);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ID_TEST, Menu.NONE, "Test");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		case MENU_ID_TEST:
			testBtn_click();
			return true;
		}
	}

	private void testBtn_click(){
		GPSSetting config = new GPSSetting(this);
		config.readConfig();

		Log.d("GPSLogger","testBtn_click: " + config.ftpUserId + ":" + config.ftpPassword + "@" + config.ftpHost);
		if( config.ftpHost.length() == 0 ){
			Toast.makeText(this, "FTP Host empty", Toast.LENGTH_LONG).show();
			return;
		}
		GPSFtpUpload myGPSFtpPost = new GPSFtpUpload ( config );
		String message = myGPSFtpPost.test();
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
}
