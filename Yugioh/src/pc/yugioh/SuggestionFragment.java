package pc.yugioh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class SuggestionFragment extends Fragment {
	
	private ArrayAdapter<String> adapter;
	private AutoCompleteTextView inputSearch;
	private DefaultHttpClient client;
	private Activity activity;
	
	private OnSuggestionListener mCallback;
	
	public interface OnSuggestionListener {
		public void onSuggestionSelected(String suggestion);
		public void initiateSearch(String search);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnSuggestionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnSuggestionListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        activity = getActivity();
        ArrayList<String> suggested_array = new ArrayList<String>();
        
        adapter = new ArrayAdapter<String>(activity,
        		android.R.layout.simple_list_item_1,
        		suggested_array);
        
        View view = inflater.inflate(R.layout.suggestion_fragment, container, false);
        ListView listView = (ListView) view.findViewById(R.id.suggestedList);
        inputSearch = (AutoCompleteTextView) view.findViewById(R.id.inputSearch);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				mCallback.onSuggestionSelected(adapter.getItem(position));
			}
        	
        });
        
        inputSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
				//What's the point of event.getKeyCode()?
				if (actionId == EditorInfo.IME_ACTION_DONE/* && event.getKeyCode() == KeyEvent.KEYCODE_ENTER*/) {
					//if (!event.isShiftPressed()) {
						String search = ((AutoCompleteTextView) view).getText().toString();
						if (search == null || search == "") {
							return false;
						}
						mCallback.initiateSearch(search);
						return true;
					//}
				}
				return false;
			}
        	
        });
        
        inputSearch.addTextChangedListener(new TextWatcher() {
       	 
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
            	
            	if (cs.length() < 3) {
            		return;
            	}
            	if (inputSearch.isPerformingCompletion()) {
                    return;
                }

            	client = new DefaultHttpClient();
            	ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        		NetworkInfo ni = cm.getActiveNetworkInfo();
        		
        		if (ni != null && ni.isConnected()) {
        			new QuerySuggestionsTask().execute(SuggestionFragment.this.client, cs.toString().replaceAll(" ", "_"));
        		} else {
        			//couldn't connect
        		}
            }
 
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {
                // TODO Auto-generated method stub
 
            }
 
            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
		return view;
	}
	
    private class QuerySuggestionsTask extends AsyncTask<Object, Integer, String[]> {

		@Override
		protected String[] doInBackground(Object... params) {
			JSONArray valArray;
			String[] suggestions;
			InputStream is;
			DefaultHttpClient client = (DefaultHttpClient) params[0];
			String cs = (String) params[1];
			String query = "http://yugioh.wikia.com/index.php?action=ajax&rs=getLinkSuggest&format=json&query=" + cs;
			try {
            	HttpGet get = new HttpGet(query);
				HttpResponse response = client.execute(get);
				HttpEntity entity = response.getEntity();
				
				if (entity != null) {
					is = entity.getContent();
					//convertStreamToString handles closing the is
					String result = convertStreamToString(is);
					JSONObject json = new JSONObject(result);
					//get the suggestions in an array
	                valArray = (json.toJSONArray(json.names())).getJSONArray(0);
	    			int size = valArray.length();
	    			suggestions = new String[size];
				    for (int i = 0; i < size; i++){ 
				    	suggestions[i] = valArray.getString(i);
					}
				    EntityUtils.consume(entity);
				} else {
					return null;
				}
				
			} catch (Exception e) {
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
