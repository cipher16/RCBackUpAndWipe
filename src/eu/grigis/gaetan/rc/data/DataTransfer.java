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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;


public class DataTransfer implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private HashMap<String,String> data;
	private String mail;
	private String sender;
	private String id;
	private String type;
	
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
}
