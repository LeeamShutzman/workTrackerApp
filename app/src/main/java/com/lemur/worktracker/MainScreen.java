package com.lemur.worktracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.sql.Date;

public class MainScreen extends AppCompatActivity {

    public static final String SHARED_PREFERENCES = "sharedPreferences";
    public static final String CLOCKSTATUS = "clockStatus";
    public static final String TOTAL_TIME= "totalTime";
    public static final String START_TIME = "startTime";
    public static final String ROUNDED_TOTAL = "roundedTotal";
    public static final String WORKED_TODAY = "workedToday";
    public static final int ALARM_HOUR = 23;
    public static final int ALARM_MINUTE = 59;
    public static final int ALARM_SECOND = 0;
    private Button button;
    private ConstraintLayout background;
    private TextView hoursWorked;
    private TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        button = findViewById(R.id.toggleButton);
        background = findViewById(R.id.background);
        hoursWorked = findViewById(R.id.hoursWorked);
        message = findViewById(R.id.message);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);

        Log.d("hello", String.valueOf(sharedPreferences.getFloat(ROUNDED_TOTAL, 69)));
        hoursWorked.setText(String.valueOf(sharedPreferences.getFloat(ROUNDED_TOTAL, 0)));

        //if clock status is on, set background to green
        if(sharedPreferences.getBoolean(CLOCKSTATUS, false)) {
            background.setBackgroundColor(getResources().getColor(R.color.onTheClock));
        }
        //if clock status is off, set background to red
        else{
            background.setBackgroundColor(getResources().getColor(R.color.offTheClock));
        }

        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                setClockStatus();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setClockStatus(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor innerEditor = sharedPreferences.edit();
        //turn on the clock if it was previously off and the button was clicked
        if(!sharedPreferences.getBoolean(CLOCKSTATUS, false)) {
            message.setText("On the Clock");
            background.setBackgroundColor(getResources().getColor(R.color.onTheClock));
            innerEditor.putBoolean(CLOCKSTATUS, true); //indicate clock is on
            innerEditor.putLong(START_TIME, System.currentTimeMillis()); //record start time
            innerEditor.commit();
            Log.d("Start Time: ", (sharedPreferences.getLong(START_TIME, 0) + "milliseconds"));
        }
        //turn off the clock if it was previously on and the button was clicked
        else{
            //workedToday becomes true after the first work session of the day, making it so that this code is skipped on future presses of the button
            //alarm is set when the first work session of the day is stopped. workedToday is reset to false in MyReciever class when data is uploaded
            if(!sharedPreferences.getBoolean(WORKED_TODAY, false)){

                //set time to 23:59
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, ALARM_HOUR);
                calendar.set(Calendar.MINUTE, ALARM_MINUTE);
                calendar.set(Calendar.SECOND, ALARM_SECOND);
                if(Calendar.getInstance().getTimeInMillis() > calendar.getTimeInMillis())
                    calendar.add(Calendar.DATE, 1);

                //set intent to execute data collection today
                Intent notifyIntent = new Intent(getApplicationContext(),MyReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 69, notifyIntent, 0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                innerEditor.putBoolean(WORKED_TODAY, true);
                innerEditor.commit();
            }

            message.setText("Off the Clock");
            background.setBackgroundColor(getResources().getColor(R.color.offTheClock));
            innerEditor.putBoolean(CLOCKSTATUS, false); //indicate clock is off
            //add time to the hours worked that day

            innerEditor.putLong(TOTAL_TIME, (sharedPreferences.getLong(TOTAL_TIME, 0) + (System.currentTimeMillis() - sharedPreferences.getLong(START_TIME, 0))));
            innerEditor.commit();
            float roundedTotal = (float) sharedPreferences.getLong(TOTAL_TIME, 0)/ (float)3600000.0;
            innerEditor.putFloat(ROUNDED_TOTAL, Math.round(roundedTotal * (float) 1000.0)/(float) 1000.0);
            innerEditor.commit();
            hoursWorked.setText(String.valueOf(sharedPreferences.getFloat(ROUNDED_TOTAL, 0)));
            Log.d("test: ",new Date(System.currentTimeMillis()).toString());
        }
    }


}
