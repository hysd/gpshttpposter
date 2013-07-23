package jp.woh.android.gpslogger;

import android.widget.ArrayAdapter;
import android.content.Context;
import java.util.List;
import android.view.View;  
import android.view.ViewGroup;  
import android.view.LayoutInflater;
import android.widget.TextView;

public class GPSHistoryArrayAdapter extends ArrayAdapter<GPSHistoryData> {
	private List<GPSHistoryData> items;
	private LayoutInflater inflater;
	
	public GPSHistoryArrayAdapter(Context context, int resourceId, List<GPSHistoryData> items) {
		super(context, resourceId, items);
		this.items = items;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {  
		if (convertView == null)
			convertView = inflater.inflate(R.layout.historyrow, null);  

		GPSHistoryData data = items.get(position);

		TextView editTitle = (TextView)convertView.findViewById(R.id.historyRowTitle);
		TextView editDescription = (TextView)convertView.findViewById(R.id.historyRowDescription); 
		TextView editDate = (TextView)convertView.findViewById(R.id.historyRowDate);
		
		String title = data.getTitle();
		String description = data.getDescription();
		String date = data.getStarttimeText() + " / " + data.getEndtimeText();
		
		editTitle.setText(title);
		editDescription.setText(description);
		editDate.setText(date);
		
		return convertView;
	}
}
