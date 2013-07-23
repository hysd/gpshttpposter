package jp.woh.android.gpslogger;

import android.content.Intent;
import android.net.Uri;

public class GPSMail {

	public Intent getIntent(String title,String body,String filepath){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_SUBJECT, title);
		intent.putExtra(Intent.EXTRA_TEXT, body);
		intent.setType("application/vnd.google-earth.kml+xml");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filepath));
		return intent;
	}
}
