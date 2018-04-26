package com.setting.dl.google.gmailapi;

import java.util.Date;


/**
 * Zamanı daha açık bir şekilde göstermek için
 *
 * */
public class Time {
    
    private String dayNight, whatTimeIsIt, month, hour, minute, day, year;
    
    
    public static String now() {
        
        return new Time(new Date()).getWhatTimeIsIt();
    }
    
    private Time(Date date) {
    
        day     = u.s("%tA", date);
        month   = u.s("%te %<tB", date);
        minute  = u.s("%tM", date);
        hour    = u.s("%tH", date);
        year    = u.s("%tY", date);
        
        dayNight = whatIsDayNight(Integer.valueOf(hour), Integer.valueOf(minute));
        whatTimeIsIt = whatIsOClock();
    }
    
    public static String whatTimeIsIt(Date date){
        
        return new Time(date).getWhatTimeIsIt();
        
    }
	
	public static String whatTimeIsIt(){
		
		return new Time(new Date()).getWhatTimeIsIt();
		
	}
    
    public static String whatTimeIsIt(long date){
        
        return new Time(new Date(date)).getWhatTimeIsIt();
        
    }

    protected String whatIsDayNight(int hour, int minute) {
        
        if (hour == 0) return "gece yarısı";
        
        
        if(hour >= 5 && hour <= 7) return "sabahın körü";
        if (hour >= 8 && hour <= 10) return "sabah";
        if(hour == 11 && minute <= 40) return "öğlene doğru";
        if (hour == 12) return "öğlen";
        if (hour >= 13 && hour < 17) return "öğleden sonra";
        if (hour == 17) return "akşam üstü";
        if (hour >= 18 && hour < 22) return "akşam";
        if (hour >= 22 && hour <= 23) return "gece";
        if (hour >= 3 && hour < 5) {
            
            return "sabaha karşı";
        }
        
        else if (hour >= 1 && hour <= 3) {
            
            return "gece yarısından sonra";
        }
        
        return "";
    }
    
    private String getWhatTimeIsIt() {
        
        return whatTimeIsIt;
    }
    
    public String getDayNight() {
        
        return dayNight;
    }
    
    private String whatIsOClock() {
        
        return u.s("%s %s %s %s saat %s:%s", month, year, day, dayNight, hour, minute);
    }
    
    
    
    
    
    
}
