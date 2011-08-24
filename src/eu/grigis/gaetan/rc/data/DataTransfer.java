package eu.grigis.gaetan.rc.data;

import java.io.Serializable;
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

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
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

import com.google.gson.Gson;

import eu.grigis.gaetan.rc.elements.AdminDevice;
import eu.grigis.gaetan.rc.elements.GPSTask;


public class DataTransfer implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private HashMap<String,String> data;
	private String mail;
	private String sender;
	private String id;
	private String type;
	private enum action {STATUS,WIPE,GEOLOC,RING,AUTH,LOCK}
	public DataTransfer()
	{
		setMail("");
		setId("");
		setType("");
		setSender("");
	}
	
	public DataTransfer(String m,String s,String i,String t) {
		setMail(m);
		setId(i);
		setType(t);
		setSender(s);
	}
	
	public void setData(HashMap<String,String> data) {
		this.data = data;
	}

	public HashMap<String,String> getData() {
		return data;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getMail() {
		return mail;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSender() {
		return sender;
	}
	
	public static String generateJson(DataTransfer dt)
	{
		Gson gson = new Gson();
		String json = gson.toJson(dt);
		Log.e("DataTransfer", "JSON : "+json);
		return 	json;
	}
	
	public static void sendData(SharedPreferences pref,DataTransfer dt)
	{
		String json = generateJson(dt);
		String url = pref.getString("SiteUrl", "gotNothing");
		String mail= pref.getString("MailAccount", "gotNothing");
		Log.e("DataTransfer", "Action Url : "+url+" Mail Adress : "+mail);
		
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
	
	public static void launchAction(String a,Context context)
	{
		String pass="";
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		
		DataTransfer dt = new DataTransfer();
		dt.setType(a);
		dt.setId(pref.getString("RegistrationID", "gotNothing"));
		dt.setMail(pref.getString("MailAccount", "gotNothing"));
		dt.setSender(pref.getString("SenderAdress", "gotNothing"));
		
		HashMap<String, String> data = new HashMap<String, String>();
		dt.setData(data);
		if(a.startsWith("lock"))
		{
			pass=a.replace("lock", "");
			a=a.substring(0, 4);
		}
		switch (action.valueOf(a.toUpperCase())) {
			case LOCK:
				DevicePolicyManager dpm = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
				if(!dpm.resetPassword(pass, 0))
				{/*unable to change password*/
					dpm.setPasswordMinimumLength(new ComponentName(context,AdminDevice.class), 0);
					dpm.setPasswordQuality(new ComponentName(context,AdminDevice.class), DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
					dpm.resetPassword(pass, 1);
				}
				
				//if pass length > 0 lock if == just unlock
				if(pass.length()>0)
					dpm.lockNow();
			break;
			case RING:
				try{
		        	Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		        	MediaPlayer mMediaPlayer = new MediaPlayer();
		        	mMediaPlayer.setDataSource(context, alert);
		        	AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		        	audioManager.setStreamVolume(AudioManager.MODE_RINGTONE, audioManager.getStreamMaxVolume(AudioManager.MODE_RINGTONE), AudioManager.FLAG_PLAY_SOUND);
					mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
					mMediaPlayer.prepare();
					mMediaPlayer.start();
					Log.i("C2DM", "Ring is running !!!");
				}catch(Exception e){}
			break;
			case STATUS:
				TelephonyManager tm=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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
			break;
			case GEOLOC:
				LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
				Criteria criteria = new Criteria();
				if(pref.getBoolean("UseGPS", false))
					criteria.setAccuracy(Criteria.ACCURACY_FINE);
				String bestProvider = locationManager.getBestProvider(criteria, true);
				Log.i("C2DM", "Best Provider : "+bestProvider);
				
				//to send some data
				Location location = locationManager.getLastKnownLocation(bestProvider);
				
				/*if enabled getting fresh data is a priority*/
				if(bestProvider.length()>0)
				{
					Log.i("C2DM", "Using request location -> GPSTask");
					GPSTask g = new GPSTask(locationManager,pref,bestProvider);
					try {
						g.execute(dt);
						if(g.get())//if false ... upload some data
							return;
					} catch (Exception e) {}
					finally {
						Log.i("C2DM","End of the GPS part task part");
					}
				}
				//send data even if we send it later with the other one
				if(location!=null)
				{
					data.put("long", location.getLongitude()+"");
					data.put("lat", location.getLatitude()+"");
					data.put("speed", location.getSpeed()+"");
					data.put("altitude", location.getAltitude()+"");
					data.put("time", location.getTime()+"");
					data.put("provider", location.getProvider()+"");
					data.put("accuracy", location.getAccuracy()+"");
					Log.i("C2DM", "Location  Lat"+location.getLatitude()+" Long"+location.getLongitude());
				}
			break;
		}
		if(!a.equals("AUTH")&&(data==null||data.isEmpty()))
			return; //do not send empty data
		dt.setData(data);
		sendData(pref,dt);
	}
	
	public static String getNameOfEnum(String name, int value)
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
}
