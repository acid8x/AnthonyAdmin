package com.lepetitmaraicher.anthonyadmin;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import android.widget.Toast;

import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.Connection;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.IConnectionInterface;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.IResultInterface;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.InvalidSQLPacketException;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.MySQLConnException;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.MySQLException;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.MySQLRow;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.ResultSet;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.Statement;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DatabaseActivity extends AppCompatActivity implements View.OnClickListener {

    Button bEraseUsers, bErasePunchs, bRetour3;
    LinearLayout progressLL3, llDatabase;
    Drawable buttonBackground;
    LongOperation longOperation;
    String query = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_database);
        setRequestedOrientation(MainActivity.activityInfo);
        MainActivity.user = null;
        bEraseUsers = findViewById(R.id.bEraseUsers);
        bEraseUsers.setOnClickListener(this);
        bErasePunchs = findViewById(R.id.bErasePunchs);
        bErasePunchs.setOnClickListener(this);
        bRetour3 = findViewById(R.id.bRetour3);
        bRetour3.setOnClickListener(this);
        progressLL3 = findViewById(R.id.progressLL3);
        llDatabase = findViewById(R.id.llDatabase);
        longOperation = new LongOperation(this);
    }

    public void onDone(boolean done) {
        if (!done) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DatabaseActivity.this, "Operation impossible pour l'instant", Toast.LENGTH_SHORT).show();
                    progressLL3.setVisibility(View.GONE);
                    llDatabase.setVisibility(View.VISIBLE);
                }
            });
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    public void sendQuery(String queryStatement) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressLL3.setVisibility(View.VISIBLE);
                llDatabase.setVisibility(View.GONE);
            }
        });
        query = queryStatement;
        longOperation.execute(query);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onClick(final View v) {
        buttonBackground = v.getBackground();
        buttonBackground.setColorFilter(Color.argb(127,255,0,0), PorterDuff.Mode.MULTIPLY);
        v.setBackground(buttonBackground);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                buttonBackground.clearColorFilter();
                v.setBackground(buttonBackground);
                if (v.getId() == R.id.bEraseUsers) {
                    sendQuery("TRUNCATE TABLE users");
                } else if (v.getId() == R.id.bErasePunchs) {
                    sendQuery("TRUNCATE TABLE punchs");
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }, 650);
    }

    static class LongOperation extends AsyncTask<String, String, String> {

        private WeakReference<DatabaseActivity> employesActivityWeakReference;
        private Connection connection;

        LongOperation(DatabaseActivity employesActivityWeakReference) {
            this.employesActivityWeakReference = new WeakReference<>(employesActivityWeakReference);
        }

        @Override
        protected String doInBackground(final String... params) {
            connection = new Connection(Constants.IP, Constants.LOGIN, Constants.PASSWORD, 3306, Constants.DB, new IConnectionInterface() {

                @Override
                public void actionCompleted() {
                    Statement statement = connection.createStatement();
                    statement.execute(params[0], new IConnectionInterface() {
                        @Override
                        public void actionCompleted() {
                            employesActivityWeakReference.get().onDone(true);
                        }

                        @Override
                        public void handleInvalidSQLPacketException(InvalidSQLPacketException ex) {
                            ex.printStackTrace();
                            employesActivityWeakReference.get().onDone(false);
                        }

                        @Override
                        public void handleMySQLException(MySQLException ex) {
                            ex.printStackTrace();
                            employesActivityWeakReference.get().onDone(false);
                        }

                        @Override
                        public void handleIOException(IOException ex) {
                            ex.printStackTrace();
                            employesActivityWeakReference.get().onDone(false);
                        }

                        @Override
                        public void handleMySQLConnException(MySQLConnException ex) {
                            ex.printStackTrace();
                            employesActivityWeakReference.get().onDone(false);
                        }

                        @Override
                        public void handleException(Exception ex) {
                            ex.printStackTrace();
                            employesActivityWeakReference.get().onDone(false);
                        }
                    });
                }

                @Override
                public void handleInvalidSQLPacketException(InvalidSQLPacketException ex) {
                    ex.printStackTrace();
                    employesActivityWeakReference.get().onDone(false);
                }

                @Override
                public void handleMySQLException(MySQLException ex) {
                    ex.printStackTrace();
                    employesActivityWeakReference.get().onDone(false);
                }

                @Override
                public void handleIOException(IOException ex) {
                    ex.printStackTrace();
                    employesActivityWeakReference.get().onDone(false);
                }

                @Override
                public void handleMySQLConnException(MySQLConnException ex) {
                    ex.printStackTrace();
                    employesActivityWeakReference.get().onDone(false);
                }

                @Override
                public void handleException(Exception ex) {
                    ex.printStackTrace();
                    employesActivityWeakReference.get().onDone(false);
                }
            });
            return params[0];
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }

    }
}