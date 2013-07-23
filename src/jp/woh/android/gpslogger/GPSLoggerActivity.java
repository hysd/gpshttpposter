package jp.woh.android.gpslogger;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;  
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View; 
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.LocationManager;

public class GPSLoggerActivity extends Activity {

	private static final int MENU_ID_HISTORY 		= (Menu.FIRST + 1);
	private static final int MENU_ID_SETTING 		= (Menu.FIRST + 2);
	private static final int MENU_ID_GPSCONFIG	= (Menu.FIRST + 3);

	private static final int SHOW_SUB_FORM = 0;
	private Button startBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		startBtn = (Button) findViewById(R.id.startBtn);
		startBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				startBtn_onClick(v);
			}
		});
		checkGpsService();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		checkGpsService();
		openOptionsMenu();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		String history_text = getText(R.string.history).toString();
		String setting_text = getText(R.string.setting).toString();
		String gps_setting_text = getText(R.string.gps_setting).toString();
		menu.add(Menu.NONE, MENU_ID_HISTORY, Menu.NONE, history_text);
		menu.add(Menu.NONE, MENU_ID_SETTING, Menu.NONE, setting_text);
		menu.add(Menu.NONE, MENU_ID_GPSCONFIG, Menu.NONE, gps_setting_text);
		menu.setGroupVisible(Menu.NONE, true);
		return super.onCreateOptionsMenu(menu);
	}	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		case MENU_ID_HISTORY:
			historyBtn_onClick();
			break;
		case MENU_ID_SETTING:
			settingBtn_onClick();
			break;
		case MENU_ID_GPSCONFIG:
			checkGpsBtn_onClick();
				break;
		}
		return true;
	}

	private void startBtn_onClick(View v) {
		Intent intent = new Intent(GPSLoggerActivity.this,GPSMapActivity.class);
		EditText editTitle = (EditText) findViewById(R.id.title);
		EditText editDescription = (EditText) findViewById(R.id.description);
		String title = editTitle.getText().toString();
		intent.putExtra("title", title);
		intent.putExtra("description", editDescription.getText().toString());
		intent.putExtra("starttime", new Date().getTime());
		startActivityForResult(intent,SHOW_SUB_FORM);
	}

	private void checkGpsBtn_onClick() {
		Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(callGPSSettingIntent);
	}

	private void historyBtn_onClick() {
		Intent intent = new Intent(GPSLoggerActivity.this,GPSHistoryActivity.class);
		startActivityForResult(intent,SHOW_SUB_FORM);
	}

	private void settingBtn_onClick() {
		Intent intent = new Intent(GPSLoggerActivity.this,GPSSettingActivity.class);
		startActivityForResult(intent,SHOW_SUB_FORM);
	}

	private void openGpsConfig(){
		String confirm_gps_title = getText(R.string.confirm).toString();
		String confirm_gps_message = getText(R.string.confirm_gps_message).toString();
		String cancel = getText(R.string.cancel).toString();

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(confirm_gps_message)
		.setCancelable(false)
		.setPositiveButton(confirm_gps_title, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(callGPSSettingIntent);
			}
		});
		alertDialogBuilder.setNegativeButton(cancel,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				dialog.cancel();
			}
		});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}
	
	private void checkGpsService() {
		LocationManager myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (!myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			openGpsConfig();
	}
}