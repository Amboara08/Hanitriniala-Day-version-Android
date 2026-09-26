package com.hanitriala.day;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ReminderReceiver extends BroadcastReceiver {
  private static final String CHANNEL_ID="hanitriala_cycle_reminders";
  private static final int NOTIFICATION_ID=2106;

  @Override public void onReceive(Context context,Intent intent){
    int days=intent.getIntExtra("days_before",2);
    String message=days==0?"Tes règles sont estimées aujourd’hui.":days==1?"Tes règles sont estimées dans environ 1 jour.":"Tes règles sont estimées dans environ "+days+" jours.";
    show(context,"Hanitriala Day",message+" Pense à te préparer.");
    ReminderScheduler.schedule(context);
  }

  public static void show(Context context,String title,String message){
    ensureChannel(context);
    Intent openApp=new Intent(context,MainActivity.class);
    openApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent openPendingIntent=PendingIntent.getActivity(context,2107,openApp,PendingIntent.FLAG_UPDATE_CURRENT);
    Notification.Builder builder=new Notification.Builder(context).setSmallIcon(R.drawable.ic_notification).setContentTitle(title).setContentText(message).setAutoCancel(true).setContentIntent(openPendingIntent).setWhen(System.currentTimeMillis()).setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE);
    if(Build.VERSION.SDK_INT>=26){try{Method method=builder.getClass().getMethod("setChannelId",String.class);method.invoke(builder,CHANNEL_ID);}catch(Exception ignored){}}
    NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    manager.notify(NOTIFICATION_ID,builder.build());
  }

  public static void ensureChannel(Context context){
    if(Build.VERSION.SDK_INT<26)return;
    try{
      NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
      Class<?> channelClass=Class.forName("android.app.NotificationChannel");
      Constructor<?> constructor=channelClass.getConstructor(String.class,CharSequence.class,int.class);
      Object channel=constructor.newInstance(CHANNEL_ID,"Rappels du cycle",Integer.valueOf(4));
      try{Method description=channelClass.getMethod("setDescription",String.class);description.invoke(channel,"Rappels des prochaines règles estimées");}catch(Exception ignored){}
      Method createChannel=manager.getClass().getMethod("createNotificationChannel",channelClass);
      createChannel.invoke(manager,channel);
    }catch(Exception ignored){}
  }
}
