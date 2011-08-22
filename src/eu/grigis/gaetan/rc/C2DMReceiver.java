package eu.grigis.gaetan.rc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;

import eu.grigis.gaetan.rc.data.DataTransfer;

public class C2DMReceiver extends C2DMBaseReceiver {
	
	/**
	 * Status, send all known & usefull? data
	 * Wipe, wipe phone
	 * Geoloc, send geoloc data
	 * Ring, make the phone ring
	 * Auth, after C2DM registration, send regId with mail to the webapplication (to get the link between mail and phone)
	 * @author kikoolol
	 *
	 */
	public C2DMReceiver() {
		super("dummy@google.com");
	}
	
	@Override
	public void onRegistered(Context context, String registrationId)
			throws java.io.IOException {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		pref.edit().putString("RegistrationID", registrationId).commit();
		DataTransfer.launchAction("AUTH",getBaseContext());//to send link between phone and mail account
	};

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i("C2DM", "Got the message : "+intent.getStringExtra("message"));
		String action = intent.getStringExtra("message");
		if(action.matches("(wipe|geoloc|status|ring|lock.+)"))//don't need to send auth again
			DataTransfer.launchAction(action,getBaseContext());
	}

	@Override
	public void onError(Context context, String errorId) {Log.e("C2DM", "Error occured!!!");}
}