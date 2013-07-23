package jp.woh.android.gpslogger;

import java.util.Date;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.Activity;

public class GPSMarkerActivity extends Activity {

	private Button saveBtn;

	private double latitude = 0;
	private double longitude = 0;

	private GPSSetting myGPSSetting;
	private GPSDatabase myGPSDatabase;
	private GPSHttpPost myGPSHttpPost;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.marker);
		
		myGPSSetting = new GPSSetting(this);
		myGPSSetting.readConfig();

		Intent i = getIntent();
		latitude = i.getDoubleExtra("latitude",0);
		longitude = i.getDoubleExtra("longitude",0);
		
		myGPSDatabase = new GPSDatabase(this);

		if(myGPSSetting.usePost)
			myGPSHttpPost = new GPSHttpPost(this,myGPSSetting);
		
		saveBtn = (Button) findViewById(R.id.saveBtn);
		saveBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				saveBtn_onClick();
			}
		});
	}
	
	private void saveBtn_onClick() {
		EditText markerTitle = (EditText) findViewById(R.id.markerTitle);
		EditText markerDescription = (EditText) findViewById(R.id.markerDescription);
		
		long id = new Date().getTime();
		String title = markerTitle.getText().toString();
		String description = markerDescription.getText().toString();
		
		myGPSDatabase.saveMarker(id, latitude, longitude, title, description);
		myGPSDatabase.close();
		
		if(myGPSSetting.usePost)
			myGPSHttpPost.postMarker(id, latitude, longitude, title, description);
		
		finish();
	} 		
}
