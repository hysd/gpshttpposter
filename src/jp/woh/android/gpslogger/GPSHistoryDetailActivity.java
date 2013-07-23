package jp.woh.android.gpslogger;

import android.app.Activity;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.database.Cursor;
import android.widget.TextView;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class GPSHistoryDetailActivity extends Activity {

	private static final int MENU_ID_HTTP   = (Menu.FIRST + 1);
	private static final int MENU_ID_FTP    = (Menu.FIRST + 2);
	private static final int MENU_ID_OUTPUT = (Menu.FIRST + 3);
	private static final int MENU_ID_RM_KML = (Menu.FIRST + 4);
	private static final int MENU_ID_DELETE = (Menu.FIRST + 5);

	private static final int DIALOG_HISTORY_DELETE	= 1;
	private static final int DIALOG_SEND_MAIL_FILE 	= 2;

	long starttime;
	long endtime;
	String title;
	String description;

	String filepath;
	MenuItem removeKmlMenu;

	EditText historyDetailTitle;
	EditText historyDetailDescription;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.historydetail);

		Intent i = getIntent();
		starttime = i.getLongExtra("starttime",0);

		GPSDatabase db = new GPSDatabase(this);
		Cursor history = db.getHistory(starttime);

		history.moveToFirst();

		endtime = history.getLong(GPSDatabase.HISTORY_ENDTIME);
		title = history.getString(GPSDatabase.HISTORY_TITLE);
		description = history.getString(GPSDatabase.HISTORY_DESCRIPTION);

		history.close();
		history = null;
		db.close();
		db = null;

		SimpleDateFormat sdf = new SimpleDateFormat(GPSHistoryData.DATE_FORMAT);
		TextView starttimeView = (TextView) findViewById(R.id.historyDetailStarttime);
		TextView endtimeView = (TextView) findViewById(R.id.historyDetailEndtime);

		starttimeView.setText(sdf.format(new Date(starttime)).toString());
		endtimeView.setText(sdf.format(new Date(endtime)).toString());

		historyDetailTitle = (EditText) findViewById(R.id.historyDetailTitle);
		historyDetailDescription = (EditText) findViewById(R.id.historyDetailDescription);
		historyDetailTitle.setText(title);
		historyDetailDescription.setText(description);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
		setKmlDisplay();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode != KeyEvent.KEYCODE_BACK){
			saveHistrory();
			return super.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}

	private void saveHistrory(){
		String title = historyDetailTitle.getText().toString();
		String description = historyDetailDescription.getText().toString();

		GPSDatabase db = new GPSDatabase(this);
		db.updateTitle(starttime, title, description);
		db.close();
	}

	private void setKmlDisplay(){
		File file = new File(getOutputFile());
		TextView filepathtext = (TextView) findViewById(R.id.historyDetailFilePathText);
		TextView filepathvalue = (TextView) findViewById(R.id.historyDetailFilePath);
		if(file != null && file.exists()){
			if(removeKmlMenu!= null) removeKmlMenu.setVisible(true);
			filepathtext.setText(getText(R.string.file).toString());
			filepathvalue.setText(file.getPath());
			Log.d("setKmlDisplay", "true");
		}else{
			if(removeKmlMenu!= null) removeKmlMenu.setVisible(false);
			filepathtext.setText("");
			filepathvalue.setText("");
			Log.d("setKmlDisplay", "false");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		String http = getText(R.string.history_detail_http_btn).toString();
		String ftp = getText(R.string.history_detail_ftp_btn).toString();
		String sd = getText(R.string.history_detail_sd_btn).toString();
		String sd_delete = getText(R.string.history_detail_sd_delete_btn).toString();
		String delete = getText(R.string.delete).toString();

		menu.add(Menu.NONE, MENU_ID_HTTP,   Menu.NONE, http);
		menu.add(Menu.NONE, MENU_ID_FTP,    Menu.NONE, ftp);
		menu.add(Menu.NONE, MENU_ID_OUTPUT, Menu.NONE, sd);
		menu.add(Menu.NONE, MENU_ID_RM_KML, Menu.NONE, sd_delete);
		menu.add(Menu.NONE, MENU_ID_DELETE, Menu.NONE, delete);

		removeKmlMenu = menu.getItem(3);
		setKmlDisplay();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		case MENU_ID_HTTP:
			httpbtn_click();
			break;
		case MENU_ID_FTP:
			ftpbtn_click();
			break;
		case MENU_ID_OUTPUT:
			outputbtn_click();
			setKmlDisplay();
			break;
		case MENU_ID_RM_KML:
			rmkmlbtn_click();
			setKmlDisplay();
			break;
		case MENU_ID_DELETE:
			deletebtn_click();
			break;			
		}
		return true;
	}
	/*
	@Override
	public void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    openOptionsMenu();
	}
	 */	
	private void httpbtn_click(){
		String title = historyDetailTitle.getText().toString();
		String description = historyDetailDescription.getText().toString();
		
//		GPSHttpLocationPost post = new GPSHttpLocationPost(this,starttime,endtime,title,description);
//		post.execute();
		GPSDatabase db = new GPSDatabase(this);
		GPSSetting config = new GPSSetting(this);
		config.readConfig();


		GPSHttpPost httpPost = new GPSHttpPost(this,config);
		if(!httpPost.isConnected()){
			Toast.makeText(this, "Network Error", Toast.LENGTH_LONG).show();
			return;
		}
		Cursor markers = db.getMarkers(starttime, endtime);

		db.updateTitle(starttime,title,description);

		Log.d("postStartTime", "start");
		httpPost.postStartTime(starttime,title,description);
		Log.d("postEndTime", "start");
		String message = "Unknown Error";
		int location_count = 0;

		GPSHttpLocationsResponse response = httpPost.postLocations(db,config,starttime,endtime,starttime);
		if(response == null){
			Toast.makeText(this, "Unknown Error", Toast.LENGTH_LONG).show();
			return;
		}
		location_count = response.getCount();
		message = response.getMessage();

		httpPost.postEndTime(starttime,endtime,location_count);
		Log.d("postMarkers", "start");
		httpPost.postMarkers(markers,config.postZip);

		markers.close();
		markers = null;
		db.close();
		db = null;

//		GPSHttpLocationPost locationpost = new GPSHttpLocationPost(this,starttime,endtime,starttime);
//		locationpost.execute();
		
	}

	private void ftpbtn_click(){
		GPSSetting config = new GPSSetting(this);
		config.readConfig();
		GPSDatabase db = new GPSDatabase(this);
		GPSKml kml = new GPSKml();
		SimpleDateFormat sdf = new SimpleDateFormat(GPSHistoryData.FILE_FORMAT);

		String filename = config.ftpFilename.length() == 0 ? sdf.format(new Date(starttime)).toString() + ".kml": config.ftpFilename;
		String data = kml.getKML(db,starttime, endtime, title, description);

		db.close();
		db = null;

		GPSFtpUpload ftpUpload = new GPSFtpUpload(config);
		String message = ftpUpload.upload(filename, data);

		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private String getOutputFile(){
		SimpleDateFormat sdf = new SimpleDateFormat(GPSHistoryData.FILE_FORMAT);
		String filename = sdf.format(new Date(starttime)).toString() + ".kml";
		String sddir = Environment.getExternalStorageDirectory() + "/gps_http_poster";
		File sdfile = new File(sddir);
		if(!sdfile.exists()) sdfile.mkdir();
		return sddir + "/" + filename;

	}

	private void sendMail(){
		try{
			GPSMail mail = new GPSMail();
			//				Intent intent = mail.getIntent(title, description, filepath);
			Intent intent = mail.getIntent(title, description,"file:" + filepath);
			startActivity(intent);
		}catch(Exception e){
			Toast.makeText(this, "Error! " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void outputbtn_click(){
		GPSDatabase db = new GPSDatabase(this);
		filepath = getOutputFile();
		GPSKml kml = new GPSKml();
		if(kml.outputKML(filepath,db, starttime, endtime, title, description)){
			Toast.makeText(this, "save to " + filepath, Toast.LENGTH_LONG).show();
			showDialog(DIALOG_SEND_MAIL_FILE);
		}else
			Toast.makeText(this, "Error! can't save " + filepath, Toast.LENGTH_LONG).show();
		db.close();
		db = null;
		Log.d("GPSLogger","sdpath: " + filepath);
	}

	private void rmkmlbtn_click(){
		String filepath = getOutputFile();
		File file = new File(filepath);
		if(file.exists()){
			file.delete();
			Toast.makeText(this, "Delete: " + filepath, Toast.LENGTH_LONG).show();
		}
	}

	private void deletebtn_click(){
		showDialog(DIALOG_HISTORY_DELETE);
	}

	private void deleteHistory(){
		GPSDatabase db = new GPSDatabase(this);
		db.delete(starttime, endtime);
		Toast.makeText(this, "delete", Toast.LENGTH_LONG).show();
		db.close();
		db = null;
		finish();
	}

	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
		case DIALOG_HISTORY_DELETE:
			return new AlertDialog.Builder(this)
			.setTitle(getText(R.string.history_detail_delete_title).toString())
			.setMessage(getText(R.string.history_detail_delete_description).toString())
			.setPositiveButton(getText(R.string.ok).toString(), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					deleteHistory();
				}
			})
			.setNegativeButton(getText(R.string.cancel).toString(), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton) {}
			})
			.create();
		case DIALOG_SEND_MAIL_FILE:
			return new AlertDialog.Builder(this)
			.setTitle(getText(R.string.history_detail_send_mail_title).toString())
			.setMessage(getText(R.string.history_detail_send_mail_description).toString())
			.setPositiveButton(getText(R.string.ok).toString(), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					sendMail();
				}
			})
			.setNegativeButton(getText(R.string.cancel).toString(), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton) {}
			})
			.create();
		}
		return super.onCreateDialog(id);
	}
}
