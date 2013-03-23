package pc.yugioh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {

	private ArrayAdapter<String> adapter;
	private AutoCompleteTextView inputSearch;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ArrayList<String> suggested_array = new ArrayList<String>();
        //
        adapter = new ArrayAdapter<String>(this,
        		android.R.layout.simple_list_item_1,
        		suggested_array);

        ListView listView = (ListView) findViewById(R.id.suggestedList);
        inputSearch = (AutoCompleteTextView) findViewById(R.id.inputSearch);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				//Do something when an item is clicked
				
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
            	String query = "http://yugioh.wikia.com/index.php?action=ajax&rs=getLinkSuggest&format=json&query=" + cs.toString();

            	HttpClient httpclient = new DefaultHttpClient();
            	HttpGet httpget = new HttpGet(query);
            	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        		NetworkInfo ni = cm.getActiveNetworkInfo();
        		
        		if (ni != null && ni.isConnected()) {
        			new QuerySuggestions().execute(httpclient, httpget);
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
    }

    private class QuerySuggestions extends AsyncTask<Object, Integer, String[]> {

		@Override
		protected String[] doInBackground(Object... params) {
			JSONArray valArray;
			String[] suggestions;
			InputStream is;
			try {
				HttpResponse response = ((HttpClient) params[0]).execute((HttpGet) params[1]);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					is = entity.getContent();
					String result = convertStreamToString(is);
					is.close();
					JSONObject json = new JSONObject(result);
	                valArray = (json.toJSONArray(json.names())).getJSONArray(0);
	    			int size = valArray.length();
	    			suggestions = new String[size];
				    for (int i = 0; i < size; i++){ 
				    	suggestions[i] = valArray.getString(i);
					}
				    entity.consumeContent();
				    
				} else {
					return null;
				}
			} catch (Exception e) {
				return null;
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
}
