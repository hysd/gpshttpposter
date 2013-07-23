package jp.woh.android.gpslogger;

import android.preference.PreferenceActivity;
import android.os.Bundle;

public class GPSSettingActivity extends PreferenceActivity {  

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
	}
}
