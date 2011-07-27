package eu.grigis.gaetan.rc;

import com.google.android.c2dm.C2DMessaging;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

public class RCMain extends Activity implements OnSharedPreferenceChangeListener {
    /** Called when the activity is first created. */

	private SharedPreferences prefs;
	private String url;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        C2DMessaging.register(this, "loupzeur@gmail.com");
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        url=prefs.getString("SiteUrl","");/*Default URL is in the preferences.xml*/
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	menu.add(Menu.NONE,0,Menu.NONE,"Preferences").setAlphabeticShortcut('p');
    	return(super.onCreateOptionsMenu(menu));
    }
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
	    	case 0:startActivity(new Intent(getBaseContext(),EditPreferences.class));return true;
    	}
		return super.onOptionsItemSelected(item);
    }
}