package pc.yugioh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ArchseriesFragment extends Fragment {

	private ArrayAdapter<String> adapter;
	private Activity activity;
	
	private OnArchseriesListener mCallback;
	
	public interface OnArchseriesListener {
		public void onSelection(String selection);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnArchseriesListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnArchseriesListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.archseries_fragment, container, false);
		activity = getActivity();

        ArrayList<String> archseries_array = new ArrayList<String>();
        
        adapter = new ArrayAdapter<String>(activity,
        		android.R.layout.simple_list_item_1,
        		archseries_array);
        
        ArrayList<String> suggestions = new ArrayList<String>();

        ListView listView = (ListView) view.findViewById(R.id.archseriesList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				mCallback.onSelection(adapter.getItem(position));
			}
        	
        });
		String result = getArguments().getString("Archseries");
		BufferedReader rdf = new BufferedReader(new StringReader(result));
		String line;
		try {
			while ((line = rdf.readLine()) != null) {
				//First one is just the archseries
				if (line.contains("rdfs:label")) {
					break;
				}
			}
			while ((line = rdf.readLine()) != null) {
				//Ignore lists
				if (line.contains("rdfs:label")) {
					if (line.contains("List of")) {
						continue;
					} else {
						suggestions.add(getLabel(line));
			            break;
					}
				}
			}
			String label;
			while ((line = rdf.readLine()) != null) {
				if (line.contains("rdfs:label")) {
					String test = getLabel(line);
					if (test.contains("Archetype or series card list for")
						|| test.contains("List of ")) {
						break;
					} else {
						label = getLabel(line);
						if (!suggestions.contains(label)) {
							suggestions.add(label);
						}
					}
				}
			}
		} catch (IOException e) {
			Log.e("Archseries", "IO error", e);
		}
		adapter.addAll(suggestions);
		adapter.notifyDataSetChanged();
		TextView textView = (TextView) view.findViewById(R.id.archseriesText);
		textView.setText("Found " + suggestions.size() + " results.");
		return view;
	}
	
	public String getLabel(String line) {
		int start = "<rdfs:label>".length(); 
		int end = line.indexOf("</rdfs:label>", start);
		return line.substring(start+2, end);
	}
}
