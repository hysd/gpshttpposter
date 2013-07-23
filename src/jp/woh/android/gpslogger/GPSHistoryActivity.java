package jp.woh.android.gpslogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;  
import android.widget.AdapterView;
import android.widget.ListView;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import java.util.List;
import java.util.ArrayList;
import android.app.Activity;

public class GPSHistoryActivity extends Activity {

	private static final int MENU_ID_DELETEALL = (Menu.FIRST + 1);

	private static final int SHOW_SUB_FORM = 0;
	private static final int DIALOG_YES_NO_MESSAGE = 1;
	
	@Override  
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.history);
		initActivity();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode,resultCode,data);
		setHistory();
	}
	
	private void setHistory(){
		GPSDatabase db = new GPSDatabase(this);
		Cursor history = db.getAllHistorys();
		List<GPSHistoryData> datas = convert(history);
		GPSHistoryArrayAdapter adapter = new GPSHistoryArrayAdapter(this, android.R.layout.simple_list_item_1,datas);
		ListView listView = (ListView) findViewById(R.id.listview);
		listView.setAdapter(adapter);
		Log.d("setHistory","" + history.getCount());
		history.close();
		history = null;
		db.close();
		db = null;
	}

	public void initActivity(){
		GPSDatabase db = new GPSDatabase(this);
		db.rebuildHistoryEndtime();
		Cursor history = db.getAllHistorys();
		List<GPSHistoryData> datas = convert(history);
		GPSHistoryArrayAdapter adapter = new GPSHistoryArrayAdapter(this, android.R.layout.simple_list_item_1,datas);
		ListView listView = (ListView) findViewById(R.id.listview);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				listView.setSelection(position);
				GPSHistoryData item = (GPSHistoryData) listView.getItemAtPosition(position);
				Intent intent = new Intent(GPSHistoryActivity.this,GPSHistoryDetailActivity.class);  
				intent.putExtra("starttime", item.getStarttime());
				startActivityForResult(intent,SHOW_SUB_FORM);
				finish();
			}
		});
		
		Log.d("initActivity","" + history.getCount());
		history.close();
		history = null;
		db.close();
		db = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		String history_text = getText(R.string.history_delete_btn).toString();
		menu.add(Menu.NONE, MENU_ID_DELETEALL, Menu.NONE, history_text);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		case MENU_ID_DELETEALL:
			deletebtn_click();
			break;
		}
		return ret;
	}
	
	private void deletebtn_click(){
		showDialog(DIALOG_YES_NO_MESSAGE);
	}
	
	public List<GPSHistoryData> convert( Cursor history){
		ArrayList<GPSHistoryData> datas = new ArrayList<GPSHistoryData>();
		int rowcount = history.getCount();
		history.moveToFirst();
		for (int i = 0; i < rowcount ; i++) {
			GPSHistoryData historyData = new GPSHistoryData();

			historyData.setStarttime(history.getLong(0));
			historyData.setEndtime(history.getLong(1));
			historyData.setTitle(history.getString(2));
			historyData.setDescription(history.getString(3));

			datas.add(historyData);
			history.moveToNext();
		}
		return datas;
	}

	public boolean deleteAllHistory(){
		GPSDatabase db = new GPSDatabase(this);
		db.clearAllTables();
		db.close();
		finish();
		return true;
	}
	
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
		case DIALOG_YES_NO_MESSAGE:
			return new AlertDialog.Builder(this)
			.setTitle(getText(R.string.history_delete_title).toString())
			.setMessage(getText(R.string.history_delete_description).toString())
			.setPositiveButton(getText(R.string.ok).toString(), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					deleteAllHistory();
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
