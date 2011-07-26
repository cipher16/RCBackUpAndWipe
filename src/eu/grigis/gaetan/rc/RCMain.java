package eu.grigis.gaetan.rc;

import com.google.android.c2dm.C2DMessaging;

import android.accounts.Account;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class RCMain extends Activity {
    /** Called when the activity is first created. */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        C2DMessaging.register(this, "loupzeur@gmail.com");
    }
}