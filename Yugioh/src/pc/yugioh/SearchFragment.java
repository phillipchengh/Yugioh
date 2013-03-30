package pc.yugioh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import pc.yugioh.SuggestionFragment.OnSuggestionListener;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class SearchFragment extends Fragment {

	private ArrayAdapter<String> adapter;
	private Activity activity;
	private DefaultHttpClient client;
	
	private OnSearchListener mCallback;
	
	public interface OnSearchListener {
		public void onSearchSelected(String search);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnSearchListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnSearchListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.search_fragment, container, false);
		activity = getActivity();

        ArrayList<String> suggested_array = new ArrayList<String>();
        
        adapter = new ArrayAdapter<String>(activity,
        		android.R.layout.simple_list_item_1,
        		suggested_array);

        ListView listView = (ListView) view.findViewById(R.id.searchList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				mCallback.onSearchSelected(adapter.getItem(position));
			}
        	
        });
		String search = getArguments().getString("Search");
		String[] words = search.split(" ");
		String term = "";
		//Open search yields more results if words are capitalized
		for (String w : words) {
			String word = w.toUpperCase();
			term += word.replace(word.substring(1), word.substring(1).toLowerCase()) + "%20";
		}
		String url = "http://yugioh.wikia.com/api.php?action=opensearch&limit=100&search=" + term;
		client = new DefaultHttpClient();
		ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		
		if (ni != null && ni.isConnected()) {
			new OpenSearchTask().execute(client, url, view);
		} else {
			//couldn't connect
		}
		return view;
	}
	
	private class OpenSearchTask extends AsyncTask<Object, Void, String[]> {

		private Integer size;
		private View view;
		
		@Override
		protected String[] doInBackground(Object... params) {
			DefaultHttpClient client = (DefaultHttpClient) params[0];
			String query = (String) params[1];
			view = (View) params[2];
			String[] suggestions;
			InputStream is;
			try {
            	HttpGet get = new HttpGet(query);
				HttpResponse response = client.execute(get);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					is = entity.getContent();
					//convertStreamToString handles closing the is
					String result = convertStreamToString(is);
					JSONArray json = new JSONArray(result); 
					JSONArray valArray = json.getJSONArray(1);
	    			int size = valArray.length();
	    			this.size = size;
	    			suggestions = new String[size];
				    for (int i = 0; i < size; i++){ 
				    	suggestions[i] = valArray.getString(i);
					}
				    EntityUtils.consume(entity);
				} else {
					return null;
				}
			} catch (Exception e) {
				Log.e("Search tag", "Exception on create", e);
				return null;
			} finally {
				client.getConnectionManager().shutdown();
			}
			return suggestions;
		}

		@Override
		protected void onPostExecute(String[] result) {
			ArrayList<String> suggestions = new ArrayList<String>(Arrays.asList(result)); 
			adapter.clear();
			for (String suggestion : suggestions) {
				adapter.add(suggestion);
                adapter.notifyDataSetChanged();
			}
			TextView textView = (TextView) view.findViewById(R.id.searchText);
			textView.setText("Found " + size.toString() + " results.");
		}
		
	}
	
	 private static String convertStreamToString(InputStream is) {
	        /*
	         * To convert the InputStream to String we use the BufferedReader.readLine()
	         * method. We iterate until the BufferedReader return null which means
	         * there's no more data to read. Each line will appended to a StringBuilder
	         * and returned as String.
	         */
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        StringBuilder sb = new StringBuilder();
	 
	        String line = null;
	        try {
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                is.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        return sb.toString();
	    }
}
