package gov.ca.cpuc.fieldtest.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


class ResultsViewer extends ArrayAdapter<ResultsItem> {
	private ArrayList<ResultsItem> result = new ArrayList<ResultsItem>();
    private Context context;

    private static class ViewHolder {
		private TextView text;
		private ImageView image;
	}

	ResultsViewer(Context context, ArrayList<ResultsItem> result) {
		super(context, R.layout.testresultview, result);
		this.result = result;
		this.context = context;

	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context
    				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.testresultview, null);
            
            ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) v.findViewById(R.id.resultsValue);
			viewHolder.image = (ImageView) v.findViewById(R.id.resultsIcon);
			v.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) v.getTag();
		holder.text.setText(result.get(position).getDataText());
		holder.image.setImageResource(result.get(position).getDataImage());
		return (v);
	}

}
