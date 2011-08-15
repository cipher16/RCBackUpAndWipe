package eu.grigis.gaetan.rc.elements;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import eu.grigis.gaetan.rc.data.DataTransfer;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

public class GPSTask extends AsyncTask<DataTransfer, Integer, Boolean> {
	private static String TAG = "GPSTask";
	private LocationManager lm;
	private SharedPreferences pref;
	private String provider;
	
	public GPSTask(LocationManager l,SharedPreferences p,String prov) {
		pref=p;
		lm=l;
		provider=prov;
	}
	
	@Override
	protected Boolean doInBackground(DataTransfer... params) {
		Looper.prepare();
		Looper l = Looper.myLooper();
		lm.requestLocationUpdates(provider, 1000, 0, new LocListener(params[0],l,lm,pref));
		Looper.loop();
		Log.i(TAG,"GPS task Stopped !!");
		return true;//doing the request
	}
}
