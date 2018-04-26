package com.setting.dl.google.gmailapi.mail.gmail;

import android.content.Context;
import android.support.annotation.Nullable;

/**
 * Created by hsyn on 7.06.2017.
 * <p>
 * Gmail is a Acount
 */

class Account {
	
	private String  account;
	
	Account(Context context) {
		
		this.account = context.getSharedPreferences("gmail", Context.MODE_PRIVATE).getString("from", null);
		
	}
	
	@Nullable String getAccount() {return account;}
}
