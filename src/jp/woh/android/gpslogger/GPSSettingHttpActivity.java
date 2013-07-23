package jp.woh.android.gpslogger;

import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class GPSSettingHttpActivity extends PreferenceActivity{
	private static final int MENU_ID_TEST = (Menu.FIRST + 1);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settinghttp);
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
		if( config.postUrl.length() == 0 ){
			Toast.makeText(this, "Empty URL", Toast.LENGTH_LONG).show();
			return;
		}
		GPSHttpPost myGPSHttpPost = new GPSHttpPost (this,config);
		String responseBody = myGPSHttpPost.test(config.postTimeout);
		Toast.makeText(this, responseBody, Toast.LENGTH_LONG).show();
	}

}
