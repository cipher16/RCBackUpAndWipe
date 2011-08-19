package eu.grigis.gaetan.rc;

import com.google.android.c2dm.C2DMessaging;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class RCMain extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    /** Called when the activity is first created. */

	private SharedPreferences prefs;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        Log.e("C2DM", "RegId : "+prefs.getString("RegistrationID", ""));
        if(prefs.getString("RegistrationID", "").length()==0&&prefs.getString("MailAccount", "").length()!=0)
    	{
        	register();
    	}
        if(prefs.getString("MailAccount", "").length()==0)
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.
        		setTitle(R.string.introTitle).
        		setMessage(getString(R.string.introSummary)+prefs.getString("SiteUrl", "")).
        		setPositiveButton("Ok", null).show();
        }
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		if(arg1.equals("MailAccount"))
		{
			Log.e("C2DM", "MailAccount Change : "+prefs.getString("MailAccount", ""));
			register();
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	menu.add(Menu.NONE,0,Menu.NONE,"Refresh Registration");
//    	menu.add(Menu.NONE,1,Menu.NONE,"UnRegister");
    	return(super.onCreateOptionsMenu(menu));
    }
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
	    	case 0:register();return true;
	    	case 1:C2DMessaging.unregister(getApplicationContext());return true;
    	}
		return super.onOptionsItemSelected(item);
    }
	
	public void register()
	{
		//unregister before
        if(prefs.getString("RegistrationID", "").length()>0)
        {
        	C2DMessaging.unregister(getApplicationContext());
        	Log.e("C2DM", "Unregistering");
        }
    	Log.e("C2DM", "RegID : "+prefs.getString("RegistrationID", ""));
    	Log.e("C2DM", "RegMail : "+prefs.getString("SenderAdress", ""));
    	Log.e("C2DM", "MailAccount : "+prefs.getString("MailAccount", ""));
        C2DMessaging.register(this, prefs.getString("SenderAdress", ""));
		Toast.makeText(this.getApplicationContext(), getString(R.string.registered)+prefs.getString("MailAccount", ""), 5000).show();
	}
}