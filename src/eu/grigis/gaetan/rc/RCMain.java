package eu.grigis.gaetan.rc;

import com.google.android.c2dm.C2DMessaging;

import eu.grigis.gaetan.rc.elements.AdminDevice;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
	private DevicePolicyManager dpm;
	private ComponentName adminName;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        Log.i("C2DM", "RegId : "+prefs.getString("RegistrationID", ""));
        if(prefs.getString("RegistrationID", "").length()==0&&prefs.getString("MailAccount", "").length()>0)
    	{
        	register();
    	}
        else
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.
        		setTitle(R.string.regOkTitle).
        		setMessage(getString(R.string.regOkSummary)+prefs.getString("SiteUrl", "")).
        		setPositiveButton("Ok", null).show();
        }
        if(prefs.getString("MailAccount", "").length()==0)
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.
        		setTitle(R.string.introTitle).
        		setMessage(getString(R.string.introSummary)+prefs.getString("SiteUrl", "")).
        		setPositiveButton("Ok", null).show();
        }
        
        /*DeviceAdmin part must run on main activity :s*/
        dpm=(DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
		adminName = new ComponentName(this,AdminDevice.class);
		
		if (!dpm.isAdminActive(adminName)) {
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,adminName);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,getString(R.string.adminRights));
			startActivityForResult(intent, 0);
		}
    }
    
	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		if(arg1.equals("MailAccount"))
		{
			Log.i("C2DM", "MailAccount Change : "+prefs.getString("MailAccount", ""));
			register();
		}
	}
	
	public void register()
	{
		//unregister before
		if(prefs.getString("MailAccount", "").length()==0)
		{
			Toast.makeText(this.getApplicationContext(), getString(R.string.noValidAccount), 5000).show();
			return;
		}
    	Log.i("C2DM", "RegID : "+prefs.getString("RegistrationID", ""));
    	Log.i("C2DM", "RegMail : "+prefs.getString("SenderAdress", ""));
    	Log.i("C2DM", "MailAccount : "+prefs.getString("MailAccount", ""));
        C2DMessaging.register(this, prefs.getString("SenderAdress", ""));
		Toast.makeText(this.getApplicationContext(), getString(R.string.registered)+prefs.getString("MailAccount", ""), 5000).show();
	}
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
//    	menu.add(Menu.NONE,0,Menu.NONE,"Refresh Registration ID");
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
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
        case 0:
            if (resultCode == Activity.RESULT_OK) {
                Log.i("DeviceAdminSample", "Administration enabled!");
            } else {
                Log.i("DeviceAdminSample", "Administration failed!");
            }
            return;
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}
}