package com.lemur.worktracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LogIn extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button button;

    SharedPreferences sharedPreferences;

    public static final String REMEMBERED_USER = "rememberedUser";

    public static String ip = "255.255.255.255"; // replace with server ip
    public static String port = "1433";
    public static String weirdClass = "net.sourceforge.jtds.jdbc.Driver";
    public static String database = "workTracker";
    public static String databaseUser = "appLogin";
    public static String databasePass = "helloThere";
    public static String url = "jdbc:jtds:sqlserver://"+ip+":"+port+";databaseName="+database+";user="+databaseUser+";password="+databasePass+";";

    public static Connection connection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
        sharedPreferences = getSharedPreferences(MainScreen.SHARED_PREFERENCES, MODE_PRIVATE);

        if(sharedPreferences.getString(REMEMBERED_USER, "noUser") == "noUser") {
            username = findViewById(R.id.username);
            password = findViewById(R.id.password);
            button = findViewById(R.id.button);

            connect();

            button.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View view) {
                    startDataCollection();
                    logInButton(view, sharedPreferences);
                }
            });
        }
        else{
            Intent intent = new Intent(LogIn.this, MainScreen.class);
            startActivity(intent);
        }
    }

    public void logInButton(View view, SharedPreferences sharedPreferences){
        if(connection!=null){
            try {
                //hashes the attempted password
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(password.getText().toString().getBytes(StandardCharsets.UTF_8));

                //queries database for the user
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("Select * from person where username = '" + username.getText().toString() + "'");

                //if user exists and password hash matches, open next screen
                if(resultSet.next() && resultSet.getString(2).equals(String.format("%064x", new BigInteger(1, hash)))){
                    Intent intent = new Intent(LogIn.this, MainScreen.class);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(REMEMBERED_USER, username.getText().toString());
                    editor.commit();
                    startActivity(intent);
                }
                else{
                    Toast.makeText(this, "Username or Password is incorrect", Toast.LENGTH_SHORT).show();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    public static void connect(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Class.forName(weirdClass);
            connection = DriverManager.getConnection(url);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startDataCollection(){
        //set time to 23:59
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        if(Calendar.getInstance().getTimeInMillis() > calendar.getTimeInMillis())
            calendar.add(Calendar.DATE, 1);

        //set intent to execute data collection every day
        Intent notifyIntent = new Intent(getApplicationContext(),MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 69, notifyIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

}
