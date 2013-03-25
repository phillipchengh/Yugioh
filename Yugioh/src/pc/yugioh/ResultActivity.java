package pc.yugioh;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.view.Menu;

public class ResultActivity extends Activity {
	
	private FragmentManager fm;
	private FragmentTransaction ft;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		
		//Determine if selection was a suggestion or search
		String s = getIntent().getStringExtra("Suggestion");
		if (s == null) {
			s = getIntent().getStringExtra("Search");
			if (s == null) {
				//No suggestion or search, so something went wrong...
				return;
			}
			setupSearchFragment(s);
		}
		setupContent(s);
	}
	
	private void setupContent(String selection) {
		//TODO Parse here to determine if selection was card/set/etc.
		//Difference between series vs. archetype?
		Bundle bundle = new Bundle();
		bundle.putString("gallery_name", selection);
		Fragment fragment = new GalleryFragment();
		fragment.setArguments(bundle);
        fm = getFragmentManager();
    	ft = fm.beginTransaction();
    	ft.add(R.id.resultLayout, fragment);
    	ft.commit();
	}

	private void setupSearchFragment(String search) {
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_result, menu);
		return true;
	}

}
