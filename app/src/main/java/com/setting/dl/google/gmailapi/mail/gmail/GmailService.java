package com.setting.dl.google.gmailapi.mail.gmail;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.setting.dl.google.gmailapi.MainActivity;

import java.util.Arrays;

/**
 * Created by hsyn on 7.06.2017.
 * <p>
 * GmailService is a account
 */

class GmailService extends Account {
    
    private       Gmail   mService;
    private final Context context;
    
    private GmailService(Context context) {
        super(context);
        this.context = context;
        setupService();
    }
    
    private void setupService() {
        
        GoogleAccountCredential mCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(MainActivity.SCOPES)).setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccountName(getAccount());
        
        HttpTransport transport   = AndroidHttp.newCompatibleTransport();
        JsonFactory   jsonFactory = JacksonFactory.getDefaultInstance();
        
        mService = new Gmail.Builder(transport, jsonFactory, mCredential).setApplicationName("Gmail").build();
        
    }
    
    private Gmail getService() {return mService;}
    
    static Gmail getGmailService(Context context) {
        
        return new GmailService(context).getService();
    }
}
