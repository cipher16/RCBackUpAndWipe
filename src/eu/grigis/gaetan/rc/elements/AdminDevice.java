package eu.grigis.gaetan.rc.elements;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AdminDevice extends DeviceAdminReceiver {
	@Override
	public void onEnabled(Context context, Intent intent) {
		super.onEnabled(context, intent);
		Log.e("ADMIN","Enabled");
	}
	@Override
	public void onDisabled(Context context, Intent intent) {
		super.onDisabled(context, intent);
		Log.e("ADMIN","Disabled");
	}
}
