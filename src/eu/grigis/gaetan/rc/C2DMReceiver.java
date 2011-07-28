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
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.google.gson.Gson;

public class C2DMReceiver extends C2DMBaseReceiver {
	
	enum action {STATUS,WIPE,GEOLOC,RING}
	
	public C2DMReceiver() {
		super("dummy@google.com");
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
		if(action.matches("(wipe|geoloc|status|ring)"))
			launchAction(action.toUpperCase());
	}

	@Override
	public void onError(Context context, String errorId) {Log.e("C2DM", "Error occured!!!");}
	
	public void launchAction(String a)
	{
		HashMap<String, String> data = new HashMap<String, String>();
		
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
				data.put("Network Type", tm.getNetworkType()+"");
				data.put("Call State", tm.getCallState()+"");
				data.put("Data State", tm.getDataState()+"");
				data.put("Device ID", tm.getDeviceId()+"");
				data.put("Phone Number", tm.getLine1Number()+"");
				data.put("Network Country", tm.getNetworkCountryIso()+"");
				data.put("Network Operator Number", tm.getNetworkOperator()+"");
				data.put("Network Operator Name", tm.getNetworkOperatorName()+"");
				data.put("Phone Type", tm.getPhoneType()+"");
				data.put("Sim State", tm.getSimState()+"");
				
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
				String bestProvider = locationManager.getBestProvider(criteria, false);
				Log.e("C2DM", "Best Provider : "+bestProvider);
				Location location = locationManager.getLastKnownLocation(bestProvider);
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
		sendData(generateJson(data));
	}
	
	public String generateJson(HashMap<String, String> d)
	{
		Gson gson = new Gson();
		String json = gson.toJson(d);
		Log.e("C2DM", "JSON : "+json);
		return 	json;
	}

	public void sendData(String json)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Log.e("C2DM", "Action Url : "+pref.getString("SiteUrl", "gotNothing"));
		String url = pref.getString("SiteUrl", "gotNothing");
		String mail= pref.getString("MailAccount", "gotNothing");
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		HttpParams params = new BasicHttpParams();
		SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);

		HttpClient client = new DefaultHttpClient(mgr, params);
		HttpPost httppost = new HttpPost(url);
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("data", json));
	        nameValuePairs.add(new BasicNameValuePair("mail", mail));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(httppost);
			Log.e("C2DM", "Reason : "+response.getStatusLine().getReasonPhrase()+" Code : "+response.getStatusLine().getStatusCode());
		} catch (Exception e) {}
	}
}