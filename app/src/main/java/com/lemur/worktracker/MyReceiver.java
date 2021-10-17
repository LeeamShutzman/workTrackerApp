package com.lemur.worktracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;

public class MyReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainScreen.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Log.d("Reciever", "onReceive: Recieved");
        LogIn.connect();
        if(sharedPreferences.getLong(MainScreen.TOTAL_TIME, 0) > 0) {
            try {
                float total = (float) sharedPreferences.getLong(MainScreen.TOTAL_TIME, 0) / (float) 3600000.0;
                Statement statement = LogIn.connection.createStatement();
                statement.executeUpdate("Insert into days values('" + sharedPreferences.getString(LogIn.REMEMBERED_USER, "noUser") + "', "
                        + "cast('" + sharedPreferences.getString(MainScreen.CURRENT_DATE, new Date(System.currentTimeMillis()).toString()) + "' as date), "
                        + total + ")");
                editor.putLong(MainScreen.TOTAL_TIME, 0);
                editor.putFloat(MainScreen.ROUNDED_TOTAL, 0);
                editor.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        editor.putBoolean(MainScreen.WORKED_TODAY, false);
        editor.commit();

    }
}
