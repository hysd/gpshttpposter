package jp.woh.android.gpslogger;

import android.os.Bundle;
import java.util.Date;
import android.location.LocationManager;
import android.location.Location;
import android.location.LocationListener;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.MapController;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.PowerManager;
import android.net.ConnectivityManager;
import android.content.pm.ApplicationInfo;
import android.widget.Toast;

public class GPSMapActivity extends MapActivity  {

	private static String PROVIDER = LocationManager.GPS_PROVIDER;
	//	private static String PROVIDER = LocationManager.NETWORK_PROVIDER;

	private static final int MENU_ID_MARK = (Menu.FIRST + 1);

	private static final int SHOW_SUB_FORM = 0;
	private static final int DIALOG_YES_NO_MESSAGE = 1;

	long startTime = 0;
	long locationTime = 0;
	long postedDateTime = 0;

	LocationManager myLocationManager;
	LocationListener myLocationListener;
	MapView myMapView;
	MapController myMapController;
	MyLocationOverlay myLocationOverlay;

	GPSSetting myGPSSetting;
	GPSDatabase myGPSDatabase;
//	GPSHttpPost myGPSHttpPost;

	ConnectivityManager myConnectivityManager;

	PowerManager.WakeLock myWakeLock;

	String title;
	String description;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = getIntent();
		title = i.getStringExtra("title");
		description = i.getStringExtra("description");
		long startTimeParam = i.getLongExtra("starttime",new Date().getTime());

		Log.d("onCreate",title);
		if( startTime != 0 ) return;

		startTime = postedDateTime = startTimeParam;

		myGPSSetting = new GPSSetting(this);
		myGPSSetting.readConfig();

		myGPSDatabase = new GPSDatabase(this);

		if(!myGPSDatabase.existHistory(startTime)){
			myGPSDatabase.saveStartTime(startTime,title,description);
			if( myGPSSetting.usePost ){
				GPSHttpPost myGPSHttpPost = new GPSHttpPost(this,myGPSSetting);
				myGPSHttpPost.postStartTime(startTime,title,description);
			}
		}

		PowerManager myPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		myWakeLock = myGPSSetting.keepScreen ? 
			myPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "GPSLogger") :
			myPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GPSLogger");
		myWakeLock.acquire();

		startGettingLocation();
		setContentView(myMapView);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopGettingLocation();
		long nowTime = new Date().getTime();
		myGPSDatabase.saveEndTime(startTime,nowTime);
		if(myGPSSetting.usePost){
			GPSHttpPost myGPSHttpPost = new GPSHttpPost(this,myGPSSetting);
			postLocations(postedDateTime,nowTime);
			int rowCount = myGPSDatabase.getLocationCount(startTime, nowTime);
			myGPSHttpPost.postEndTime(startTime,nowTime,rowCount);
		}
		myGPSDatabase.close();
		if(myWakeLock != null) myWakeLock.release();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if( myGPSSetting.followMe ) followMe();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ID_MARK, Menu.NONE, "Mark");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ID_MARK:
			return saveMarker();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private String getMapKey(){
		boolean isDebug = false;
		String map_key = getResources().getString(R.string.map_key);
		try{
			isDebug = (this.getPackageManager().getApplicationInfo(this.getPackageName(), 0).flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE;
			if(isDebug) map_key = getResources().getString(R.string.map_key_debug);
		}catch(Exception e){
			isDebug = false;
		}
		return map_key;
	}

	private void startGettingLocation(){
		
		myMapView = new MapView(this, getMapKey());
		myMapView.setEnabled(true);
		myMapView.setClickable(true);
		myMapController = myMapView.getController();
		myMapController.setZoom(18);

		myLocationOverlay = new MyLocationOverlay(getApplicationContext(),myMapView);
		myLocationOverlay.onProviderEnabled(PROVIDER);
		myLocationOverlay.enableCompass();
		myLocationOverlay.enableMyLocation();

		myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		myLocationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				if(location == null) return;
				try{
					locationTime = location.getTime();
					myGPSDatabase.saveLocation(location);
					String message = "onLocationChanged: " + location.toString();
					Log.d("onLocationChanged",message);
					if( myGPSSetting.followMe ) followMe();
					if( myGPSSetting.usePost && locationTime - ( myGPSSetting.postInterval * 60000 ) > postedDateTime ){
						long prevPostedDateTime = postedDateTime;
						postedDateTime = locationTime;
						postLocations(prevPostedDateTime,locationTime);
					}
					locationTime = location.getTime();
				}catch(Exception e){
					e.printStackTrace();
				}
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};
		long saveInterval = myGPSSetting.saveInterval * 1000;
		float saveDistance = myGPSSetting.saveDistance;

		myLocationManager.requestLocationUpdates(PROVIDER,saveInterval, saveDistance, myLocationListener);
		myMapView.getOverlays().add(myLocationOverlay);
		myMapView.invalidate();

		Log.d("GPSLogger","saveInterval: " + saveInterval + " / saveDistance: " + saveDistance);
	}

	private void followMe(){
		GeoPoint geo = myLocationOverlay.getMyLocation();
		if(geo != null){
			myMapController.animateTo(geo);
			Log.d("GPSLogger","followMe: " + geo.toString());
		}
	}

	private void postLocations(long starttime,long endtime){
		String message = "Unknown Error";
		GPSHttpPost myGPSHttpPost = new GPSHttpPost(this,myGPSSetting);
		GPSHttpLocationsResponse response = myGPSHttpPost.postLocations(myGPSDatabase,myGPSSetting,starttime,endtime,startTime);
		if(response != null) message = response.getMessage();
		Log.d("GPSMapActivity","postLocations: " + message);
		if(message != "") Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private void stopGettingLocation(){
		myMapView.getOverlays().remove(myLocationOverlay);
		myLocationOverlay.disableCompass();
		myLocationOverlay.disableMyLocation();
		myLocationManager.removeUpdates(myLocationListener);
	}

	private boolean saveMarker(){
		Location location = myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location == null){
			Toast.makeText(this, "getLastKnownLocation Error.", Toast.LENGTH_LONG).show();
			return false;
		}

		Intent intent = new Intent(GPSMapActivity.this,GPSMarkerActivity.class);
		intent.putExtra("logtime", location.getTime());
		intent.putExtra("latitude", location.getLatitude());
		intent.putExtra("longitude", location.getLongitude());
		startActivityForResult(intent,SHOW_SUB_FORM);

		return true;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void finish()
	{
		showDialog(DIALOG_YES_NO_MESSAGE);
	}

	public void appEnd()
	{
		super.finish();
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
		case DIALOG_YES_NO_MESSAGE:
			return new AlertDialog.Builder(this)
			.setTitle(getText(R.string.map_close_confirm_title).toString())
			.setMessage(getText(R.string.map_close_confirm_description).toString())
			.setPositiveButton(getText(R.string.ok).toString(), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					appEnd();
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
