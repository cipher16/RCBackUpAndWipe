package eu.grigis.gaetan.rc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.google.gson.Gson;

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
	private static boolean GPSLock = false;// lock GPS until the response is got
	private enum action {STATUS,WIPE,GEOLOC,RING,AUTH}
	public C2DMReceiver() {
		super("dummy@google.com");
	}
	
	@Override
	public void onRegistered(Context context, String registrationId)
			throws java.io.IOException {
		Log.e("C2DM", "Registration ID arrived: Fantastic!!!");
		Log.e("C2DM", registrationId);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		pref.edit().putString("RegistrationID", registrationId).commit();
		launchAction("AUTH");//to send link between phone and mail account
	};

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.e("C2DM", "Got the message : "+intent.getStringExtra("message"));
		String action = intent.getStringExtra("message");
		if(action.matches("(wipe|geoloc|status|ring)"))//don't need to send auth again
			launchAction(action.toUpperCase());
	}

	@Override
	public void onError(Context context, String errorId) {Log.e("C2DM", "Error occured!!!");}
	
	public void launchAction(String a)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		DataTransfer dt = new DataTransfer();
		dt.setType(a);
		dt.setId(pref.getString("RegistrationID", "gotNothing"));
		dt.setMail(pref.getString("MailAccount", "gotNothing"));
		dt.setSender(pref.getString("SenderAdress", "gotNothing"));
		
		HashMap<String, String> data = new HashMap<String, String>();
		dt.setData(data);
		switch (action.valueOf(a)) {
			case RING:
				try{
		        	Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		        	MediaPlayer mMediaPlayer = new MediaPlayer();
		        	mMediaPlayer.setDataSource(this, alert);
		        	AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		        	audioManager.setStreamVolume(AudioManager.MODE_RINGTONE, audioManager.getStreamMaxVolume(AudioManager.MODE_RINGTONE), AudioManager.FLAG_PLAY_SOUND);
					mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
					mMediaPlayer.prepare();
					mMediaPlayer.start();
					Log.e("C2DM", "Ring is running !!!");
				}catch(Exception e){}
			break;
			case STATUS:
				TelephonyManager tm=(TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
				data.put("Network Type", getNameOfEnum("networktype",tm.getNetworkType()));
				data.put("Call State", getNameOfEnum("callstate",tm.getCallState()));
				data.put("Data State", getNameOfEnum("datastate",tm.getDataState()));
				data.put("Device ID", tm.getDeviceId()+"");
				data.put("Phone Number", tm.getLine1Number()+"");
				data.put("Network Country", tm.getNetworkCountryIso()+"");
				data.put("Network Operator Number", tm.getNetworkOperator()+"");
				data.put("Network Operator Name", tm.getNetworkOperatorName()+"");
				data.put("Phone Type", getNameOfEnum("phonetype",tm.getPhoneType()));
				data.put("Sim State", getNameOfEnum("simstate",tm.getSimState()));
				
				Log.e("C2DM", "Network Type : "+tm.getNetworkType());
				Log.e("C2DM", "Call State : "+tm.getCallState());
				Log.e("C2DM", "Data State : "+tm.getDataState());
				Log.e("C2DM", "Device ID : "+tm.getDeviceId());
				Log.e("C2DM", "Phone Number : "+tm.getLine1Number());
				Log.e("C2DM", "Network Country : "+tm.getNetworkCountryIso());
				Log.e("C2DM", "Network Operator Numeric : "+tm.getNetworkOperator());
				Log.e("C2DM", "Network Operator Name : "+tm.getNetworkOperatorName());
				Log.e("C2DM", "Radio Used : "+tm.getPhoneType());
				Log.e("C2DM", "Sim State : "+tm.getSimState());
			break;
			case GEOLOC:
				LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				Criteria criteria = new Criteria();
				if(pref.getBoolean("UseGPS", false))
					criteria.setAccuracy(Criteria.ACCURACY_FINE);
				String bestProvider = locationManager.getBestProvider(criteria, true);
				Log.e("C2DM", "Best Provider : "+bestProvider);
				Location location = locationManager.getLastKnownLocation(bestProvider);
				
				/*if enabled getting fresh data is a priority*/
				if(pref.getBoolean("UseGPS", false)&&bestProvider.length()>0)
				{
					if(GPSLock)
						return;//send nothing
					GPSLock=true;
					Log.e("C2DM", "Using request location");
					//if no data available send data when available
//					locationManager.requestLocationUpdates(bestProvider, 1000, 0, new LocListener(dt));
					startThread(bestProvider, dt);
				}
				//send data even if we send it later with the 
				if(location!=null)
				{
					data.put("long", location.getLongitude()+"");
					data.put("lat", location.getLatitude()+"");
					data.put("speed", location.getSpeed()+"");
					data.put("altitude", location.getAltitude()+"");
					data.put("time", location.getTime()+"");
					data.put("provider", location.getProvider()+"");
					data.put("accuracy", location.getAccuracy()+"");
					Log.e("C2DM", "Location  Lat"+location.getLatitude()+" Long"+location.getLongitude());
				}
			break;
		}
		dt.setData(data);
		sendData(generateJson(dt));
	}
	
	public void startThread(final String provider,final DataTransfer dt)
	{
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				((LocationManager) getSystemService(LOCATION_SERVICE)).requestLocationUpdates(provider, 1000, 0, new LocListener(dt));
			}
		});
		th.setDaemon(true);
		th.run();
	}
	
	public String generateJson(DataTransfer dt)
	{
		Gson gson = new Gson();
		String json = gson.toJson(dt);
		Log.e("C2DM", "JSON : "+json);
		return 	json;
	}

	public void sendData(String json)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String url = pref.getString("SiteUrl", "gotNothing");
		String mail= pref.getString("MailAccount", "gotNothing");
		Log.e("C2DM", "Action Url : "+url+" Mail Adress : "+mail);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		HttpParams params = new BasicHttpParams();
		SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);

		HttpClient client = new DefaultHttpClient(mgr, params);
		HttpPost httppost = new HttpPost(url+"/rcbu/parser");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	        nameValuePairs.add(new BasicNameValuePair("data", json));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(httppost);
			Log.e("C2DM", "Reason : "+response.getStatusLine().getReasonPhrase()+" Code : "+response.getStatusLine().getStatusCode());
		} catch (Exception e) {}
	}
	
	public String getNameOfEnum(String name, int value)
	{
		HashMap<String, String> h = new HashMap<String, String>();
		h.put("networktype0", "Unknown");
		h.put("networktype1", "GPRS");
		h.put("networktype2", "EDGE");
		h.put("networktype3", "UMTS");
		h.put("networktype4", "CDMA");
		h.put("networktype5", "EVDO rev 0");
		h.put("networktype6", "EVDO rev A");
		h.put("networktype7", "1xRTT");
		h.put("networktype8", "HSDPA");
		h.put("networktype9", "HSUPA");
		h.put("networktype10", "HSPA");
		h.put("networktype11", "iDen");
		h.put("networktype12", "EVDO rev B");
		h.put("networktype13", "LTE");
		h.put("networktype14", "eHRPD");
		h.put("networktype15", "HSPA+");

		h.put("phonetype0", "None");
		h.put("phonetype1", "GSM");
		h.put("phonetype2", "CDMA");
		h.put("phonetype3", "SIP");
		
		h.put("callstate0", "Idle");
		h.put("callstate1", "Ringing");
		h.put("callstate2", "OffHook");

		h.put("dataactivity0", "None");
		h.put("dataactivity1", "In traffic");
		h.put("dataactivity2", "OffHook");
		h.put("dataactivity3", "In/Out traffic");
		h.put("dataactivity4", "Link Down");

		h.put("datastate0", "Disconnecting");
		h.put("datastate1", "Connecting");
		h.put("datastate2", "Connected");
		h.put("datastate3", "Suspended");

		h.put("simstate0", "Unknown");
		h.put("simstate1", "Absent");
		h.put("simstate2", "Pin Required");
		h.put("simstate3", "Puk Required");
		h.put("simstate4", "Locked");
		h.put("simstate5", "Ready");
		return h.get(name+value);
	}
	
	public class LocListener extends Handler implements LocationListener {
		private DataTransfer d;
		private int counter;
		
		public LocListener(DataTransfer dt) {
			counter=0;
			d=dt;
			this.sendMessageDelayed(new Message(), (long)5000);
		}
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			counter++;
			Log.e("C2DM", "Current counter : "+counter);
			if(counter>=5)
			{
				Log.e("C2DM", "Disabling");
				counter=0;
				((LocationManager) getSystemService(LOCATION_SERVICE)).removeUpdates(this);
			}
		}

		@Override
		public void onLocationChanged(Location location) {
			GPSLock=false;
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
			sendData(generateJson(d));
			
			((LocationManager) getSystemService(LOCATION_SERVICE)).removeUpdates(this);
			//stop update
		}

		@Override public void onProviderDisabled(String arg0) {//stop everything on DC
			((LocationManager) getSystemService(LOCATION_SERVICE)).removeUpdates(this);
		}

		@Override public void onProviderEnabled(String arg0) {}

		@Override public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			Log.e("C2DM", "Status Changed : "+arg0+" "+arg1);
		}
	}
}