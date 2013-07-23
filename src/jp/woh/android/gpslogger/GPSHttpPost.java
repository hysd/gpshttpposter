package jp.woh.android.gpslogger;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.ArrayList;
import java.nio.charset.Charset;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.database.Cursor;
import android.util.Log;


public class GPSHttpPost {

	String postUrl = "";
	String postUserId = "";
	String postPassword = "";
	String postCharset = "UTF-8";
	Context context;

	private static String USER_AGENT = "Android GPSLogger ( +http://woh.jp/android/GPSLogger/ )";

	public GPSHttpPost(Context context,GPSSetting config){
		this.context = context;
		postUrl = config.postUrl;
		postUserId = config.postUserId;
		postPassword = config.postPassword;
		postCharset = config.postCharset;
	}
	public GPSHttpPost(Context context,String url, String userId, String userPassword,String charset){
		this.context = context;
		postUrl = url;
		postUserId = userId;
		postPassword = userPassword;
		postCharset = charset;
	}

	private ArrayList<NameValuePair> getDefaultNameValuePair(){
		ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("account", postUserId));
		nameValuePair.add(new BasicNameValuePair("password", postPassword));
		return nameValuePair;
	}

	public String test(int timeout) {
		if(!isConnected()) return "Network Error";
		ArrayList<NameValuePair> nameValuePair = getDefaultNameValuePair();
		nameValuePair.add(new BasicNameValuePair("cmd", "test"));
		Log.d("GPSHttpPost.postTest","test");
		UrlEncodedFormEntity entity;
		try{
			entity = new UrlEncodedFormEntity(nameValuePair,postCharset);
			return EntityUtils.toString(doPost(entity,timeout).getEntity());
		}catch(Exception e){
			return "unknown error";
		}
	}

	public String postStartTime( long starttime, String title, String description ) {
		if(!isConnected()) return "Network Error";
		ArrayList<NameValuePair> nameValuePair = getDefaultNameValuePair();
		nameValuePair.add(new BasicNameValuePair("cmd", "start"));
		nameValuePair.add(new BasicNameValuePair("starttime", String.valueOf(starttime)));
		nameValuePair.add(new BasicNameValuePair("title", title));
		nameValuePair.add(new BasicNameValuePair("description", description));
		Log.d("GPSHttpPost.postStartTime","starttime: " + starttime + " / " + title);
		UrlEncodedFormEntity entity;
		try{
			entity = new UrlEncodedFormEntity(nameValuePair,postCharset);
			return EntityUtils.toString(doPost(entity,0).getEntity());
		}catch(Exception e){
			return "unknown error";
		}
	}

	public String postEndTime( long starttime, long endtime, int count ) {
		if(!isConnected()) return "Network Error";
		ArrayList<NameValuePair> nameValuePair = getDefaultNameValuePair();
		nameValuePair.add(new BasicNameValuePair("cmd", "end"));
		nameValuePair.add(new BasicNameValuePair("starttime", String.valueOf(starttime)));
		nameValuePair.add(new BasicNameValuePair("endtime", String.valueOf(endtime)));
		nameValuePair.add(new BasicNameValuePair("logcount", String.valueOf(count)));
		Log.d("GPSHttpPost.postEndTime","time: " + starttime + "/" + endtime);
		UrlEncodedFormEntity entity;
		try{
			entity = new UrlEncodedFormEntity(nameValuePair,postCharset);
			return EntityUtils.toString(doPost(entity,0).getEntity());
		}catch(Exception e){
			return "unknown error";
		}
	}

	public GPSHttpLocationsResponse postLocations(GPSDatabase db, GPSSetting config,long starttime, long endtime,long firstTime){
		if(!isConnected()) return null;
		int limit = config.postLimit;
		int timeout = config.postTimeout;
		boolean zip = config.postZip;
		int offset = 0;
		int count = 0;
		GPSHttpLocationsResponse locationsResponse = new GPSHttpLocationsResponse();
		while(true){
			Log.d("postLocations", "start");
			Cursor locations = db.getLocations(starttime, endtime,limit,offset);
			int locationsCount = locations.getCount();
			if( locationsCount == 0 ) break;
			count += locationsCount;
			Log.d("postLocations", "locations.getCount(): " + locations.getCount() + " / limit: " + limit);
			offset += limit;
			HttpResponse response = postLocations(locations,firstTime,timeout,zip);
			if(response == null){
				locationsResponse.setMessage("Connection Lost: " + config.postUrl);
			}
			int code = response.getStatusLine().getStatusCode();
			locationsResponse.setCode(code);
			try{
				locationsResponse.setMessage(EntityUtils.toString(response.getEntity()));
			}catch(Exception e){}
			locations.close();
			locations = null;
			if( code != 200 || response == null || locationsCount < config.postLimit ) break;
		}
		locationsResponse.setCount(count);
		return locationsResponse;

	}

	public HttpResponse postLocations( Cursor location,long starttime, int timeout, boolean zip ) {
		if(!isConnected()) return null;
		String data = convertLocations(location);

		ArrayList<NameValuePair> nameValuePair = getDefaultNameValuePair();
		nameValuePair.add(new BasicNameValuePair("cmd", "locations"));
		nameValuePair.add(new BasicNameValuePair("starttime", String.valueOf(starttime)));
		Log.d("GPSHttpPost.postLocations","starttime: " + data);
		HttpEntity entity;
		try{
			if(zip){
				Log.d("zip","start");
				entity = zipArchiveEntity(nameValuePair,"log", data.getBytes());
				Log.d("zip","end");
			}else{
				nameValuePair.add(new BasicNameValuePair("log", data));
				entity = new UrlEncodedFormEntity(nameValuePair,postCharset);
			}
			return doPost(entity,timeout);
		}catch(Exception e){ return null; }
	}

	public String postMarker(long id,double latitude,double longitude,String title,String description ) {
		if(!isConnected()) return "Network Error";
		ArrayList<NameValuePair> nameValuePair = getDefaultNameValuePair();
		nameValuePair.add(new BasicNameValuePair("cmd", "marker"));
		nameValuePair.add(new BasicNameValuePair("logtime", String.valueOf(id)));
		nameValuePair.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
		nameValuePair.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));
		nameValuePair.add(new BasicNameValuePair("title", title));
		nameValuePair.add(new BasicNameValuePair("description", description));
		Log.d("GPSHttpPost.postMarker","time: " + latitude + "," + longitude + "/" + title);
		UrlEncodedFormEntity entity;
		try{
			entity = new UrlEncodedFormEntity(nameValuePair,postCharset);
			return EntityUtils.toString(doPost(entity,0).getEntity());
		}catch(Exception e){
			e.printStackTrace();
			return "unknown error";
		}
	}

	public String postMarkers( Cursor markers, boolean zip ){
		if(markers.getCount() == 0) return "no data";
		if(!isConnected()) return "Network Error";
		String data = convertMarkers(markers);

		ArrayList<NameValuePair> nameValuePair = getDefaultNameValuePair();
		nameValuePair.add(new BasicNameValuePair("cmd", "markers"));
		HttpEntity entity;
		try{
			if(zip){
				entity = zipArchiveEntity(nameValuePair,"log", data.getBytes());
			}else{
				nameValuePair.add(new BasicNameValuePair("log", data));
				entity = new UrlEncodedFormEntity(nameValuePair,postCharset);
			}
			return EntityUtils.toString(doPost(entity,0).getEntity());
		}catch(Exception e){ return "unknown error";}
	}

	private String convertLocations( Cursor locations ){
		int rowcount = locations.getCount();
		String data = new String();
		locations.moveToFirst();
		for (int i = 0; i < rowcount ; i++) {
			data +=
					locations.getLong(GPSDatabase.LOCATION_ID) + "," +
							locations.getDouble(GPSDatabase.LOCATION_LATITUDE) + "," +
							locations.getDouble(GPSDatabase.LOCATION_LONGITUDE) + "," +
							locations.getDouble(GPSDatabase.LOCATION_ALTITUDE) + "," +
							locations.getFloat(GPSDatabase.LOCATION_BEARING) + "," +
							locations.getFloat(GPSDatabase.LOCATION_SPEED) + "," +
							locations.getFloat(GPSDatabase.LOCATION_ACCURACY) + "\n"
							;
			locations.moveToNext();
		}
		return data;
	}

	private String convertMarkers( Cursor markers ){
		int rowcount = markers.getCount();
		String data = new String();
		markers.moveToFirst();
		for (int i = 0; i < rowcount ; i++) {
			data += markers.getLong(GPSDatabase.MARKER_ID) + "," +
					markers.getDouble(GPSDatabase.MARKER_LATITUDE) + "," +
					markers.getDouble(GPSDatabase.MARKER_LONGITUDE) + "," +
					"\"" + markers.getString(GPSDatabase.MARKER_TITLE).replace("\"", "\\\"") + "\"," +
					"\"" + markers.getString(GPSDatabase.MARKER_DESCRIPTION).replace("\"", "\\\"")  + "\"\n"
					;
			markers.moveToNext();
		}
		return data;
	}

	private InputStreamBody getZipMimePart(String zipname,byte[] zipdata) throws IOException{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(os);
		ZipEntry entry = new ZipEntry(zipname);
		zip.putNextEntry(entry);
		zip.write(new String(zipdata,postCharset).getBytes());
		zip.closeEntry();
		zip.close();
		os.close();
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		return new InputStreamBody(is, zipname + ".zip");
	}

	private MultipartEntity zipArchiveEntity
	(ArrayList<NameValuePair> nameValuePair,String zipname,byte[] zipdata) throws IOException{
		//		MultipartEntity mimeEntity = new MultipartEntity(HttpMultipartMode.STRICT);
		MultipartEntity mimeEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		mimeEntity.addPart("compress",new StringBody("zip", Charset.forName(postCharset)));
		for(int i = 0; i < nameValuePair.size(); i++){
			NameValuePair item = nameValuePair.get(i);
			String name = item.getName();
			String value = item.getValue();
			Log.d("zipArchiveEntity",name + "=" + value);
			mimeEntity.addPart(name,new StringBody(value, Charset.forName(postCharset)));
		}
		mimeEntity.addPart("file",getZipMimePart(zipname, zipdata));
		return mimeEntity;
	}

	public HttpResponse doPost( HttpEntity httpEntity, int timeout ) throws ClientProtocolException,IOException{
		HttpClient httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		if(timeout != 0){
			HttpConnectionParams.setConnectionTimeout(params, timeout * 1000);
			HttpConnectionParams.setSoTimeout(params, timeout * 1000);
			Log.d("GPSHttpPost.doPost","timeout: " + timeout);
		}

		HttpClient httpclient = new DefaultHttpClient(params);
		HttpPost httpPost = new HttpPost(postUrl);
		httpPost.setHeader("User-Agent",USER_AGENT);
		httpPost.setEntity(httpEntity);
		return httpclient.execute(httpPost);
		//		HttpResponse response = httpclient.execute(httpPost);
		//		return EntityUtils.toString(response.getEntity());
	}

	public boolean isConnected(){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnected();
	}

}
