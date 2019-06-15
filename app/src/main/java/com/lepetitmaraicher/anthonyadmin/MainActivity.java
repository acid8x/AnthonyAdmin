package com.lepetitmaraicher.anthonyadmin;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static int activityInfo = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    public static User user = null;
    Button bEmployes, bDayPunchs, bEmployePunchs, bAllPunchs, bDaysPunchs, bQuitter;
    boolean doubleBackToExitPressedOnce = false;
    PermissionsManager permissionsManager;
    Drawable buttonBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(activityInfo);
        permissionsManager = PermissionsManager.getInstance();
        permissionsManager.requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() { }
            @Override
            public void onDenied(String permission) {
                onDestroy("Cette permission est requise");
            }
        });
        if (Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        bEmployes = findViewById(R.id.bEmployes);
        bEmployes.setOnClickListener(this);
        bDayPunchs = findViewById(R.id.bDayPunchs);
        bDayPunchs.setOnClickListener(this);
        bDaysPunchs = findViewById(R.id.bDaysPunchs);
        bDaysPunchs.setOnClickListener(this);
        bEmployePunchs = findViewById(R.id.bEmployePunchs);
        bEmployePunchs.setOnClickListener(this);
        bAllPunchs = findViewById(R.id.bAllPunchs);
        bAllPunchs.setOnClickListener(this);
        bQuitter = findViewById(R.id.bQuitter);
        bQuitter.setOnClickListener(this);
    }

    public static String getDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd.HHmm", Locale.CANADA);
        return dateFormat.format(calendar.getTime());
    }

    public void onDestroy(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        onDestroy();
    }

    @Override
    protected void onDestroy() {
        finishAndRemoveTask();
        System.exit(0);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            onDestroy();
            return;
        }
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Appuyez sur RETOUR encore pour quitter", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    // TODO: 2019-06-03 START USER EDIT ACTIVITY
                }
                break;
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    Intent i = new Intent(MainActivity.this, PunchsActivity.class);
                    i.putExtra("QUERY", "SELECT * FROM punchs WHERE name='" + user.getBadgeName() + "'");
                    startActivityForResult(i, 2);
                }
                break;
        }
    }

    @Override
    public void onClick(final View v) {
        buttonBackground = v.getBackground();
        buttonBackground.setColorFilter(Color.argb(127,255,0,0), PorterDuff.Mode.MULTIPLY);
        v.setBackground(buttonBackground);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                buttonBackground.clearColorFilter();
                v.setBackground(buttonBackground);
                Intent i;
                switch (v.getId()) {
                    case R.id.bEmployes:
                        startActivityForResult(new Intent(MainActivity.this, EmployesActivity.class), 0);
                        break;
                    case R.id.bEmployePunchs:
                        startActivityForResult(new Intent(MainActivity.this, EmployesActivity.class), 1);
                        break;
                    case R.id.bDayPunchs:
                        i = new Intent(MainActivity.this, PunchsActivity.class);
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
                        String date = dateFormat.format(calendar.getTime());
                        i.putExtra("QUERY", "SELECT * FROM punchs WHERE date='" + date + "'");
                        startActivityForResult(i, 2);
                        break;
                    case R.id.bDaysPunchs:
                        startActivityForResult(new Intent(MainActivity.this, DaysActivity.class), 3);
                        break;
                    case R.id.bAllPunchs:
                        i = new Intent(MainActivity.this, PunchsActivity.class);
                        i.putExtra("QUERY", "SELECT * FROM punchs");
                        startActivityForResult(i, 2);
                        break;
                    case R.id.bQuitter:
                        onDestroy();
                        break;
                }
            }
        }, 650);
    }
}