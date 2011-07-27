package eu.grigis.gaetan.rc;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;

public class C2DMReceiver extends C2DMBaseReceiver {
	
	enum action {STATUS,WIPE,GEOLOC}
	
	public C2DMReceiver() {
		super("loupzeur@google.com");
	}

	@Override
	public void onRegistered(Context context, String registrationId)
			throws java.io.IOException {
		Log.e("C2DM", "Registration ID arrived: Fantastic!!!");
		Log.e("C2DM", registrationId);
	};

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.e("C2DM", "Got the message : "+intent.getStringExtra("message"));
		String action = intent.getStringExtra("message");
		if(action.matches("(wipe|geoloc|status)"))
			launchAction(action.toUpperCase());
	}

	@Override
	public void onError(Context context, String errorId) {Log.e("C2DM", "Error occured!!!");}
	
	public void launchAction(String a)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Log.e("C2DM", "Action Url : "+pref.getString("SiteUrl", "gotNothing"));
		
		switch (action.valueOf(a)) {
			case STATUS:
				TelephonyManager tm=(TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
				Log.e("C2DM", "Network Type : "+tm.getNetworkType());
			break;
			case GEOLOC:
				LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				Criteria criteria = new Criteria();
				String bestProvider = locationManager.getBestProvider(criteria, false);
				Log.e("C2DM", "Best Provider : "+bestProvider);
				Location location = locationManager.getLastKnownLocation(bestProvider);
				if(location!=null)
					Log.e("C2DM", "Location  Lat"+location.getLatitude()+" Long"+location.getLongitude());
			break;
		}
	}

}