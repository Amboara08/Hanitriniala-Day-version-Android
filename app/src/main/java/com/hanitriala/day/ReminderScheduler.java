package com.hanitriala.day;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public final class ReminderScheduler {
  private static final int REQUEST_CODE=2105;
  private ReminderScheduler(){}

  public static void schedule(Context context){
    PeriodStore store=new PeriodStore(context);
    if(!"true".equals(store.get("reminder_enabled","false"))){cancel(context);return;}
    ArrayList<PeriodStore.Period> periods=store.all();
    if(periods.size()==0){cancel(context);return;}
    int daysBefore=readNumber(store.get("reminder_days","2"),2);
    int hour=readNumber(store.get("reminder_hour","8"),8);
    int minute=readNumber(store.get("reminder_minute","0"),0);
    int cycleLength=calculateCycleLength(store,periods);
    Calendar predicted=parseDate(periods.get(0).start),trigger=null,now=Calendar.getInstance();
    for(int i=0;i<36;i++){
      predicted.add(Calendar.DAY_OF_YEAR,cycleLength);
      trigger=(Calendar)predicted.clone();
      trigger.set(Calendar.HOUR_OF_DAY,hour);trigger.set(Calendar.MINUTE,minute);trigger.set(Calendar.SECOND,0);trigger.set(Calendar.MILLISECOND,0);
      trigger.add(Calendar.DAY_OF_YEAR,-daysBefore);
      if(trigger.after(now))break;
    }
    if(trigger==null||!trigger.after(now))return;
    ReminderReceiver.ensureChannel(context);
    Intent intent=new Intent(context,ReminderReceiver.class);intent.putExtra("days_before",daysBefore);
    PendingIntent pending=PendingIntent.getBroadcast(context,REQUEST_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    if(Build.VERSION.SDK_INT>=23){try{Method method=alarm.getClass().getMethod("setExactAndAllowWhileIdle",int.class,long.class,PendingIntent.class);method.invoke(alarm,AlarmManager.RTC_WAKEUP,trigger.getTimeInMillis(),pending);return;}catch(Exception ignored){}}
    if(Build.VERSION.SDK_INT>=19)alarm.setExact(AlarmManager.RTC_WAKEUP,trigger.getTimeInMillis(),pending);else alarm.set(AlarmManager.RTC_WAKEUP,trigger.getTimeInMillis(),pending);
  }

  public static void cancel(Context context){
    Intent intent=new Intent(context,ReminderReceiver.class);
    PendingIntent pending=PendingIntent.getBroadcast(context,REQUEST_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    alarm.cancel(pending);pending.cancel();
  }

  public static void test(Context context){ReminderReceiver.show(context,"Hanitriala Day","La notification de rappel fonctionne correctement.");}

  private static int calculateCycleLength(PeriodStore store,ArrayList<PeriodStore.Period> periods){
    ArrayList<PeriodStore.Period> ordered=new ArrayList<PeriodStore.Period>(periods);
    Collections.sort(ordered,new Comparator<PeriodStore.Period>(){public int compare(PeriodStore.Period first,PeriodStore.Period second){return first.start.compareTo(second.start);}});
    int total=0,count=0,firstIndex=Math.max(1,ordered.size()-6);
    for(int i=firstIndex;i<ordered.size();i++){int difference=daysBetween(ordered.get(i-1).start,ordered.get(i).start);if(difference>=15&&difference<=60){total+=difference;count++;}}
    return count>0?Math.round((float)total/count):readNumber(store.get("cycle","28"),28);
  }

  private static Calendar parseDate(String value){
    Calendar calendar=Calendar.getInstance();
    try{SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd",Locale.US);Date date=format.parse(value);calendar.setTime(date);}catch(Exception ignored){}
    calendar.set(Calendar.HOUR_OF_DAY,12);calendar.set(Calendar.MINUTE,0);calendar.set(Calendar.SECOND,0);calendar.set(Calendar.MILLISECOND,0);return calendar;
  }
  private static int daysBetween(String first,String second){return(int)((parseDate(second).getTimeInMillis()-parseDate(first).getTimeInMillis())/86400000L);}
  private static int readNumber(String value,int fallback){try{return Integer.parseInt(value);}catch(Exception ignored){return fallback;}}
}
