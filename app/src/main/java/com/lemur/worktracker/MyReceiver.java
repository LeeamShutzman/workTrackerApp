package com.lemur.worktracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;

import static android.content.Context.ALARM_SERVICE;

public class MyReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainScreen.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        LogIn.connect();
        if(sharedPreferences.getLong(MainScreen.TOTAL_TIME, 0) > 0) {
            try {
                float total = (float) sharedPreferences.getLong(MainScreen.TOTAL_TIME, 0) / (float) 3600000.0;
                Statement statement = LogIn.connection.createStatement();
                statement.executeUpdate("Insert into days values('" + sharedPreferences.getString(LogIn.REMEMBERED_USER, "noUser") + "', "
                        + "cast('" + new Date(System.currentTimeMillis()).toString() + "' as date), "
                        + total + ")");
                editor.putLong(MainScreen.TOTAL_TIME, 0);
                editor.putFloat(MainScreen.ROUNDED_TOTAL, 0);
                editor.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

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
