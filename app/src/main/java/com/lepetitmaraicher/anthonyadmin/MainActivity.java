package com.lepetitmaraicher.anthonyadmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
    Button bEmployes, bDayPunchs, bEmployePunchs, bAllPunchs, bDaysPunchs, bQuitter, bDatabase;
    boolean doubleBackToExitPressedOnce = false, databaseEnable = false;
    PermissionsManager permissionsManager;
    Drawable buttonBackground;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(activityInfo);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
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
        float f = metrics.widthPixels/21;
        f/=metrics.scaledDensity;
        bEmployes = findViewById(R.id.bEmployes);
        bEmployes.setTextSize(f);
        bEmployes.setOnClickListener(this);
        bDayPunchs = findViewById(R.id.bDayPunchs);
        bDayPunchs.setTextSize(f);
        bDayPunchs.setOnClickListener(this);
        bDaysPunchs = findViewById(R.id.bDaysPunchs);
        bDaysPunchs.setTextSize(f);
        bDaysPunchs.setOnClickListener(this);
        bEmployePunchs = findViewById(R.id.bEmployePunchs);
        bEmployePunchs.setTextSize(f);
        bEmployePunchs.setOnClickListener(this);
        bAllPunchs = findViewById(R.id.bAllPunchs);
        bAllPunchs.setTextSize(f);
        bAllPunchs.setOnClickListener(this);
        bQuitter = findViewById(R.id.bQuitter);
        bQuitter.setTextSize(f);
        bQuitter.setOnClickListener(this);
        bDatabase = findViewById(R.id.bDatabase);
        bDatabase.setTextSize(f);
        bDatabase.setOnTouchListener(new TouchTimer() {
            @Override
            public void onTouchEnded(boolean time) {
                if (time) {
                    if (databaseEnable) {
                        databaseEnable = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bDatabase.setAlpha(0.15f);
                            }
                        });
                        startActivityForResult(new Intent(MainActivity.this, DatabaseActivity.class), 4);
                    } else {
                        databaseEnable = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bDatabase.setAlpha(1f);
                            }
                        });
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                databaseEnable = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        bDatabase.setAlpha(0.15f);
                                    }
                                });
                            }
                        },3000);
                    }
                }
            }
        });
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