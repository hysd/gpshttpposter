package jp.woh.android.gpslogger;

import android.content.Context;
import android.content.ContentValues;
import android.location.Location;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.util.Log;

public class GPSDatabase {
	DatabaseHelper myDatabaseHelper = null;

	public static final int LOCATION_ID 			= 0; // ID = Date::getTime()
	public static final int LOCATION_LATITUDE 	= 1;
	public static final int LOCATION_LONGITUDE 	= 2;
	public static final int LOCATION_ALTITUDE 	= 3;
	public static final int LOCATION_BEARING 		= 4;
	public static final int LOCATION_SPEED 		= 5;
	public static final int LOCATION_ACCURACY 	= 6;

	public static final int HISTORY_STARTTIME 	= 0;
	public static final int HISTORY_ENDTIME		= 1;
	public static final int HISTORY_TITLE			= 2;
	public static final int HISTORY_DESCRIPTION 	= 3;
	
	public static final int MARKER_ID			 	= 0;
	public static final int MARKER_LATITUDE	 	= 1;
	public static final int MARKER_LONGITUDE	 	= 2;
	public static final int MARKER_TITLE		 	= 3;
	public static final int MARKER_DESCRIPTION 	= 4;

	public class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, "db", null, 1);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			createAllTables(db);
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*
			db.execSQL("DROP TABLE IF EXISTS location;");
			onCreate(db);
		 */
		}
	}

	public GPSDatabase(Context context) {
		myDatabaseHelper = new DatabaseHelper(context);
		SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
		createAllTables(db);
	}

	public void saveStartTime(long starttime,String title,String description){
		SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("starttime", starttime);
		values.put("title", title);
		values.put("description", description);
		db.insert("history", null, values);
		Log.d("GPSDatabase.saveStartTime","starttime: " + starttime);
	}

	public void saveEndTime(long starttime, long endtime){
		SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("endtime", endtime);
		db.update("history", values, "starttime = ?",new String[]{ String.valueOf(starttime ) } );
		Log.d("GPSDatabase.saveEndTime","starttime: " + starttime);
	}

	public void saveLocation(Location location){
		SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id", location.getTime());
		values.put("latitude", location.getLatitude());
		values.put("longitude", location.getLongitude());
		values.put("altitude", location.getAltitude());
		values.put("bearing", location.getBearing());
		values.put("speed", location.getSpeed());
		values.put("accuracy", location.getAccuracy());
		try{
			db.insert("location", null, values);
		}catch(Exception e){
			e.printStackTrace();
		}
		Log.d("GPSDatabase.saveLocation","location:" + location.getLatitude() + "," + location.getLongitude());
	}

	public void saveMarker(long id,double latitude,double longitude,String title,String description){
		SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id", id);
		values.put("latitude", latitude);
		values.put("longitude", longitude);
		values.put("title",title);
		values.put("description",description);
		db.insert("marker", null, values);
		Log.d("GPSDatabase.saveMarker","location:" + latitude + "," + longitude);
	}
	
	public void updateTitle(long starttime, String title,String description){
		SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("description", description);
		db.update("history", values, "starttime = ?",new String[]{String.valueOf(starttime)});
		Log.d("GPSDatabase.saveEndTime","starttime: " + starttime);
	}

	public void delete(long starttime,long endtime){
		SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
		String sql = "DELETE FROM location WHERE id >= %d AND id <= %d";
		String query = String.format(sql,starttime,endtime);
		db.execSQL(query);
		sql = "DELETE FROM history WHERE starttime = %d";
		query = String.format(sql,starttime);
		db.execSQL(query);
		sql = "DELETE FROM marker WHERE id >= %d AND id <= %d";
		query = String.format(sql,starttime,endtime);
		db.execSQL(query);
		Log.d("GPSDatabase.delete","starttime: " + starttime);
	}

	public Cursor getLocations(long starttime, long endtime, int limit, int offset){
		SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
		String sql = "SELECT * FROM location WHERE id >= %d AND id <= %d LIMIT %d OFFSET %d";
		String query = String.format(sql,starttime,endtime,limit,offset);
		return db.rawQuery(query, null);
	}

	public Cursor getLocations(long starttime, long endtime){
		SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
		String sql = "SELECT * FROM location WHERE id >= %d AND id <= %d";
		String query = String.format(sql,starttime,endtime);
		return db.rawQuery(query, null);
	}

	public int getLocationCount(long starttime,long endtime){
		SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
		String sql = "SELECT COUNT(*) FROM location WHERE id >= %d AND id <= %d";
		String query = String.format(sql,starttime,endtime);
		Cursor rows = db.rawQuery(query, null);
		rows.moveToFirst();
		int ret = rows.getInt(0);
		rows.close();
		rows = null;
		return ret;
	}
	
	public Cursor getMarkers(long starttime,long endtime){
		SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
		String sql = "SELECT * FROM marker WHERE id >= %d AND id <= %d";
		String query = String.format(sql,starttime,endtime);
		return db.rawQuery(query, null);
	}
/*
	public Cursor getHistorys(long limit,long offset){
		SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
		String sql = "SELECT * FROM history ORDER BY starttime DESC LIMIT %d OFFSET %d";
		String query = String.format(sql,limit,offset);
		return db.rawQuery(query, null);
	}
*/
	public Cursor getAllHistorys(){
		SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
		String sql = "SELECT * FROM history ORDER BY starttime DESC";
		String query = String.format(sql);
		return db.rawQuery(query, null);
	}

	public boolean existHistory(long starttime){
		Cursor history = getHistory(starttime);
		boolean exists = history.getCount() != 0;
		history.close();
		history = null;
		return exists;
	}

	public Cursor getHistory(long starttime){
		SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
		String sql = "SELECT * FROM history WHERE starttime = %d";
		String query = String.format(sql,starttime);
		return db.rawQuery(query, null);
	}

	public long getLastHistory(long starttime){
		SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
		String sql = "SELECT MIN(starttime) FROM history WHERE starttime > %d";
		String query = String.format(sql,starttime);
		Cursor nexthistory = db.rawQuery(query, null);
		long endtime = 0;
		nexthistory.moveToFirst();
		if(nexthistory.isNull(0)){
			query = "SELECT MAX(ID) FROM location";
		}else{
			long nextstarttime = nexthistory.getLong(0);
			sql = "SELECT MAX(ID) FROM location WHERE ID < %d";
			query = String.format(sql,nextstarttime);
		}
		nexthistory.close();
		nexthistory = null;
		Cursor row = db.rawQuery(query, null);
		row.moveToFirst();
		endtime = row.getLong(0);
		row.close();
		row = null;
		Log.d("getLastHistory",
				DateFormat.format(GPSHistoryData.DATE_FORMAT,starttime).toString() + "," +
						DateFormat.format(GPSHistoryData.DATE_FORMAT,endtime).toString()
						);

		return starttime < endtime ? endtime : 0;
	}

	public void rebuildHistoryEndtime(){
		SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
		String historysql = "SELECT starttime FROM history WHERE endtime IS NULL OR endtime = 0";
		Cursor history = db.rawQuery(historysql, null);
		int rowcount = history.getCount();
		history.moveToFirst();
		for (int i = 0; i < rowcount ; i++) {
			long starttime = history.getLong(0);
			long endtime = getLastHistory(starttime);
			String sql = "UPDATE history SET endtime = %d WHERE starttime = %d";
			String query = String.format(sql,endtime,starttime);
			db.execSQL(query);
			Log.d("setHistoryEndtime",query);
			history.moveToNext();
		}
		history.close();
		history = null;
		db.close();
		db = null;
	}
	
	public void clearAllTables(){
		SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS location");
		db.execSQL("DROP TABLE IF EXISTS history");
		db.execSQL("DROP TABLE IF EXISTS marker");
		createAllTables(db);
	}
	
	public void createAllTables(SQLiteDatabase db){
		db.execSQL(
				"CREATE TABLE IF NOT EXISTS location (" +
						"id LONG PRIMARY KEY," +
						"latitude DOUBLE," +
						"longitude DOUBLE," +
						"altitude DOUBLE," +
						"bearing FLOAT," +
						"speed FLOAT," +
						"accuracy DOUBLE" +
						")"
				);
		db.execSQL(
				"CREATE TABLE IF NOT EXISTS history (" +
						"starttime LONG PRIMARY KEY," +
						"endtime LONG," +
						"title TEXT," +
						"description TEXT" +
						")"
				);
		db.execSQL(
				"CREATE TABLE IF NOT EXISTS marker (" +
						"id LONG PRIMARY KEY," +
						"latitude DOUBLE," +
						"longitude DOUBLE," +
						"title TEXT," +
						"description TEXT" +
						")"
				);
		db.execSQL("CREATE INDEX IF NOT EXISTS idx_endtime ON history(endtime)");
	}
	
	public void close(){
		SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
		db.close();
	}
}
