package eu.grigis.gaetan.rc.elements;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class MailsAccountPreference extends ListPreference {

	public MailsAccountPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
		String[] strings=new String[accounts.length];
		
		int i=0;
		for (Account account : accounts) {
			strings[i++]=account.name;
		}
		setEntries(strings);
		setEntryValues(strings);
	}
	public MailsAccountPreference(Context context) {
		this(context, null);
	}
}
