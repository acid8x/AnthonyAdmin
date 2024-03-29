package com.lepetitmaraicher.anthonyadmin;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.Connection;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.IConnectionInterface;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.InvalidSQLPacketException;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.MySQLConnException;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.MySQLException;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.Statement;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class DatabaseActivity extends AppCompatActivity implements View.OnClickListener {

    Button bOk3, bRetour3;
    LinearLayout progressLL3, llQuery, llResult;
    ImageView ivResult;
    EditText etQuery;
    LongOperation longOperation;
    InputMethodManager inputManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_database);
        setRequestedOrientation(MainActivity.activityInfo);
        inputManager = (InputMethodManager) DatabaseActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        etQuery = findViewById(R.id.etQueryDatabase);
        bOk3 = findViewById(R.id.bOkDatabase);
        bOk3.setOnClickListener(this);
        bRetour3 = findViewById(R.id.bRetourDatabase);
        bRetour3.setOnClickListener(this);
        progressLL3 = findViewById(R.id.llProgressDatabase);
        llQuery = findViewById(R.id.llQueryDatabase);
        llResult = findViewById(R.id.llResultDatabase);
        ivResult = findViewById(R.id.ivResultDatabase);
        longOperation = new LongOperation(this);
        etQuery.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    inputManager.toggleSoftInput(0, 0);
                    etQuery.clearFocus();
                    return true;
                }
                return false;
            }
        });
        etQuery.requestFocus();
    }

    public void onDone(boolean done) {
        if (!done) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ivResult.setImageResource(R.mipmap.no);
                    llResult.setVisibility(View.VISIBLE);
                    progressLL3.setVisibility(View.GONE);
                    llQuery.setVisibility(View.GONE);
                    bOk3.setVisibility(View.GONE);
                    bRetour3.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }, 1500);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ivResult.setImageResource(R.mipmap.yes);
                    llResult.setVisibility(View.VISIBLE);
                    progressLL3.setVisibility(View.GONE);
                    llQuery.setVisibility(View.GONE);
                    bOk3.setVisibility(View.GONE);
                    bRetour3.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setResult(RESULT_OK);
                            finish();
                        }
                    }, 1500);
                }
            });
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bRetourDatabase:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.bOkDatabase:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        llResult.setVisibility(View.GONE);
                        progressLL3.setVisibility(View.VISIBLE);
                        llQuery.setVisibility(View.GONE);
                        bOk3.setVisibility(View.GONE);
                        bRetour3.setVisibility(View.GONE);
                    }
                });
                String sendString = etQuery.getText().toString();
                longOperation.execute(sendString);
                break;
        }
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