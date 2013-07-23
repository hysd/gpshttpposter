package jp.woh.android.gpslogger;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.widget.Toast;

public class GPSHttpLocationPost extends AsyncTask<String, Integer, Long> {
	ProgressDialog dialog;
	Context context;
	long starttime;
	long endtime;
	long firsttime;
	
	public GPSHttpLocationPost(Context context,long starttime,long endtime,long firsttime){
	    this.context = context;
	    this.starttime = starttime;
	    this.endtime = endtime;
	    this.firsttime = firsttime;
	}

	@Override
	protected void onPreExecute() {
	    dialog = new ProgressDialog(context);
	    dialog.setTitle("Please wait");
	    dialog.setMessage("Uploading data...");
	    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    dialog.setCancelable(true);
	    dialog.setMax(100);
	    dialog.setProgress(0);
	    dialog.show();
	}
	
	@Override
	protected Long doInBackground(String... params) {
		GPSDatabase db = new GPSDatabase(context);
		GPSSetting config = new GPSSetting(context);
		config.readConfig();
		GPSHttpPost httpPost = new GPSHttpPost(context,config);
		
		int limit = config.postLimit;
		int timeout = config.postTimeout;
		boolean zip = config.postZip;
		int offset = 0;
		int count = 0;
		int max = db.getLocationCount(starttime, endtime);
	    dialog.setMax(max);
		GPSHttpLocationsResponse locationsResponse = new GPSHttpLocationsResponse();
		while(true){
			Log.d("postLocations", "start");
			Cursor locations = db.getLocations(starttime, endtime,limit,offset);
			int locationsCount = locations.getCount();
			if( locationsCount == 0 ) break;
			count += locationsCount;
			Log.d("postLocations", "locations.getCount(): " + locations.getCount() + " / limit: " + limit);
			offset += limit;
			HttpResponse response = httpPost.postLocations(locations,firsttime,timeout,zip);
			int code = 0;
			try{
				code = response.getStatusLine().getStatusCode();
				locationsResponse.setCode(code);
				locationsResponse.setMessage(EntityUtils.toString(response.getEntity()));
			}catch(Exception e){}
			locations.close();
			locations = null;
			if( code != 200 || response == null || locationsCount < config.postLimit ) break;
			publishProgress(count);
		}
//		Toast.makeText(context, "Upload: " + count, Toast.LENGTH_LONG).show();
		locationsResponse.setCount(count);
		return Long.valueOf(count);
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		dialog.setProgress(values[0]);
	}
	
	@Override
	protected void onCancelled() {
		dialog.dismiss();
	}
	
	@Override
	protected void onPostExecute(Long result) {
		dialog.dismiss();
	}
}