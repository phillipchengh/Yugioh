package pc.yugioh;

import pc.yugioh.SearchFragment.OnSearchListener;
import pc.yugioh.SuggestionFragment.OnSuggestionListener;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class MainActivity extends Activity implements OnSuggestionListener, OnSearchListener {

	private FragmentManager fm;
	private FragmentTransaction ft;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_main);
        fm = getFragmentManager();
    	ft = fm.beginTransaction();
    	ft.add(R.id.mainLayout, new SuggestionFragment());
    	ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public void initiateSearch(String search) {
		Bundle bundle = new Bundle();
		bundle.putString("Search", search);
		Fragment fragment = new SearchFragment();
		fragment.setArguments(bundle);
        fm = getFragmentManager();
    	ft = fm.beginTransaction();
    	ft.replace(R.id.mainLayout, fragment);
    	ft.addToBackStack(null);
    	ft.commit();
	}
	
	private void onSelection(String selection) {
		Intent intent = new Intent(this, ResultActivity.class);
		intent.putExtra("Selection", selection);
		startActivity(intent);
	}
	
	@Override
	public void onSuggestionSelected(String suggestion) {
		onSelection(suggestion);
	}

	@Override
	public void onSearchSelected(String search) {
		onSelection(search);
	}
}
