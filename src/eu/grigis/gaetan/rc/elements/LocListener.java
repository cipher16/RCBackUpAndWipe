package eu.grigis.gaetan.rc.elements;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import eu.grigis.gaetan.rc.data.DataTransfer;

public class LocListener implements LocationListener {
	private static String TAG="LocListener";
	
	private DataTransfer d;
	private Timer t;
	private Looper loop;
	private LocationManager lm;
	private SharedPreferences pref;
	
	public LocListener(DataTransfer dt,Looper l,LocationManager locMan,SharedPreferences spref) {
		d=dt;
		loop=l;
		t= new Timer();
		lm=locMan;
		pref=spref;
		
		t.schedule(new TimerTask() {@Override public void run() {stop(true);}}, Integer.valueOf(pref.getString("GPSTimeOut", "30"))*1000);
	}
	@Override
	public void onLocationChanged(Location location) {
		if(location==null)
			return;
		HashMap<String, String> data = d.getData();
		data.put("long", location.getLongitude()+"");
		data.put("lat", location.getLatitude()+"");
		data.put("speed", location.getSpeed()+"");
		data.put("altitude", location.getAltitude()+"");
		data.put("time", location.getTime()+"");
		data.put("provider", location.getProvider()+"");
		data.put("accuracy", location.getAccuracy()+"");
		DataTransfer.sendData(pref ,d);
		stop(false);
	}
	@Override public void onProviderDisabled(String arg0) {
		Log.i(TAG,"Provider disabled");
		stop(false);
	}
	@Override public void onProviderEnabled(String arg0) {/*nothing to do ... we are disabled now*/}
	@Override public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
	
	public void stop(boolean byTimer)
	{
		lm.removeUpdates(this);
		if(!byTimer)
			t.cancel();
		t.purge();
		loop.quit();
	}
}