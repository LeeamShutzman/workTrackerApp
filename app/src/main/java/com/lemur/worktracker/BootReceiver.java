package com.lemur.worktracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;

import androidx.annotation.RequiresApi;

import static android.content.Context.ALARM_SERVICE;

public class BootReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        //set time to 23:59
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        if(Calendar.getInstance().getTimeInMillis() > calendar.getTimeInMillis())
            calendar.add(Calendar.DATE, 1);

        //set intent to execute data collection every day
        Intent notifyIntent = new Intent(context,MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 69, notifyIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
