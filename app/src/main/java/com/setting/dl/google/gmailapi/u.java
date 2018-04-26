package com.setting.dl.google.gmailapi;


import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.setting.dl.google.gmailapi.mail.gmail.Mail;

import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class u {
	
	public static final MyLogger log = MyLogger.jLog();
	public static void sendMessage(Context context, String title, String text) {
		
		if (BuildConfig.DEBUG) {
			
			createNotification(context, title, text);
		}
		else {
			
			if (isDeviceOnline(context)) Mail.send(context, title, text);
		}
	}
	
	
	
	public static String getDate(long milis) {
		
		return Time.whatTimeIsIt(new Date(milis));
		
		//return s("%te %<tB %<tY %<tA %<tH:%<tM:%<tS", date);
		
	}
	
	public static String getDate(String milis) {
		
		try {
			
			return Time.whatTimeIsIt(new Date(Long.valueOf(milis)));
			
		}
		catch (NumberFormatException e) {
			
			return milis;
		}
		
		//return s("%te %<tB %<tY %<tA %<tH:%<tM:%<tS", date);
		//SimpleDateFormat sdf  = new SimpleDateFormat(s("dd MMMM yyyy %tA HH:mm:ss", date), new Locale("tr"));
		//return sdf.format(date);
	}
	
	public static String getDate(Date date) {
		
		return Time.whatTimeIsIt(date);
		
		//return s("%te %<tB %<tY %<tA %<tH:%<tM:%<tS", date);
		//SimpleDateFormat dateFormat = new SimpleDateFormat(s("dd MMMM yyyy %tA HH:mm:ss", date), new Locale("tr"));
		//return dateFormat.format(date);
	}
	
	@NonNull
	public static String getDateLongString() {
		
		return String.valueOf(new Date().getTime());
	}
	
	static public String s(String msg, Object... params) {
		
		return String.format(new Locale("tr"), msg, params);
	}
	
	public static boolean isDeviceOnline(Context context) {
		
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (connMgr == null) return false;
		
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}
	
	public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
		
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		
		if (manager == null) return false;
		
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	public static Handler run(Runnable runnable, long delay) {
		
		Handler handler = new Handler(Looper.getMainLooper());
		
		handler.postDelayed(runnable, delay);
		return handler;
		
	}
	
	public static void runThread(final Runnable runnable) { new Thread(runnable).start(); }
	
	public static void runThread(final Runnable runnable, final int delay) {
		
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				runnable.run();
			}
		}, delay);
		
		
	}
	
	public static String dateStamp() {
		
		return getDate(new Date()) + "\n";
	}
	
	public static void createNotification(Context context, String title, String text) {
		
		String CHANNEL_ID = "0412";
		String CHANNEL_NAME = "xyz.channel";
		
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		if (mNotificationManager == null) return;
		
		
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(context, CHANNEL_ID)
						.setContentText(text).setStyle(new NotificationCompat.BigTextStyle())
						.setSmallIcon(R.mipmap.system)
						.setContentTitle(title)
						/*.setColor(Color.YELLOW)
						.setLights(Color.YELLOW, 500, 2000)
						.setVibrate(new long[]{0, 0, 0, 150})*/;
        
        /*
        mBuilder.setDefaults(
                Notification.DEFAULT_SOUND
                             );
        */
		
        
        
        
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			
			int                 importance          = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
		
			mBuilder.setChannelId(CHANNEL_ID);
			mNotificationManager.createNotificationChannel(notificationChannel);
		}
		
		mNotificationManager.notify(new Random().nextInt(), mBuilder.build());
  
		
		
	}
	
	
}





















