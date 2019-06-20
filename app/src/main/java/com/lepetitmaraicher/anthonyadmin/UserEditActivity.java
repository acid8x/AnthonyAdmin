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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.Connection;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.IConnectionInterface;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.InvalidSQLPacketException;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.MySQLConnException;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.MySQLException;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.Statement;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UserEditActivity extends AppCompatActivity implements View.OnClickListener {

    LongOperation longOperation;
    LinearLayout progressLL4, llEditUser, resultLL;
    EditText etName;
    TextView tvId, tvJob, tvLast, tvOn, tvOff;
    Switch swAdmin;
    ImageView imageView;
    Button bBack, bOk, bEffacer;
    boolean isChange = false, adminStatus = false;
    String userName = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_useredit);
        setRequestedOrientation(MainActivity.activityInfo);
        if (MainActivity.user == null) {
            setResult(RESULT_CANCELED);
            finish();
        } else {
            userName = MainActivity.user.getBadgeName();
            adminStatus = MainActivity.user.isAdmin();
        }
        progressLL4 = findViewById(R.id.llProgressUserEdit);
        llEditUser = findViewById(R.id.llUserEdit);
        resultLL = findViewById(R.id.llResultDatabase);
        imageView = findViewById(R.id.ivResultDatabase);
        etName = findViewById(R.id.etQueryDatabase);
        etName.setText(MainActivity.user.getBadgeName());
        tvId = findViewById(R.id.tvIdUserEdit);
        tvId.setText(MainActivity.user.getBadgeId());
        tvJob = findViewById(R.id.tvJobUserEdit);
        tvJob.setText(MainActivity.user.getCurrentJob());
        tvLast = findViewById(R.id.tvLastUserEdit);
        tvLast.setText(getDate(MainActivity.user.getLastPunch()));
        tvOn = findViewById(R.id.tvOnUserEdit);
        tvOff = findViewById(R.id.tvOffUserEdit);
        swAdmin = findViewById(R.id.swAdminUserEdit);
        if (MainActivity.user.isAdmin()) {
            swAdmin.setChecked(true);
            tvOn.setVisibility(View.VISIBLE);
            tvOff.setVisibility(View.INVISIBLE);
        }
        bBack = findViewById(R.id.bRetourUserEdit);
        bBack.setOnClickListener(this);
        bOk = findViewById(R.id.bOkUserEdit);
        bOk.setOnClickListener(this);
        bEffacer = findViewById(R.id.bEffacerUserEdit);
        bEffacer.setOnClickListener(this);
        swAdmin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isChecked) {
                            tvOn.setVisibility(View.VISIBLE);
                            tvOff.setVisibility(View.INVISIBLE);
                        } else {
                            tvOn.setVisibility(View.INVISIBLE);
                            tvOff.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
        etName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager inputManager = (InputMethodManager) UserEditActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.toggleSoftInput(0, 0);
                    etName.clearFocus();
                    swAdmin.requestFocus();
                    return true;
                }
                return false;
            }
        });
        longOperation = new LongOperation(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bRetourUserEdit:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.bOkUserEdit:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!etName.getText().toString().equals(userName) || swAdmin.isChecked() != adminStatus) {
                            userName = etName.getText().toString();
                            adminStatus = swAdmin.isChecked();
                            isChange = true;
                        }
                        llEditUser.setVisibility(View.GONE);
                        resultLL.setVisibility(View.GONE);
                        progressLL4.setVisibility(View.VISIBLE);
                    }
                });
                if (isChange) {
                    String admin = "0";
                    if (adminStatus) admin = "1";
                    String sendString = "UPDATE users SET badgeName='" + userName + "',isAdmin='" + admin + "' WHERE badgeId='" + MainActivity.user.getBadgeId() + "'";
                    longOperation.execute(sendString);
                } else onDone(true);
                break;
            case R.id.bEffacerUserEdit:
                llEditUser.setVisibility(View.GONE);
                resultLL.setVisibility(View.GONE);
                progressLL4.setVisibility(View.VISIBLE);
                String sendString = "DELETE FROM users WHERE badgeId='" + MainActivity.user.getBadgeId() + "'";
                longOperation.execute(sendString);
                break;
        }
    }

    public static String getDate(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("(yyyy-MM-dd) HH:mm:ss", Locale.CANADA);
        return dateFormat.format(calendar.getTime());
    }

    public void onDone(boolean done) {
        if (!done) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageResource(R.mipmap.no);
                    resultLL.setVisibility(View.VISIBLE);
                    progressLL4.setVisibility(View.GONE);
                    llEditUser.setVisibility(View.GONE);
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
                    imageView.setImageResource(R.mipmap.yes);
                    resultLL.setVisibility(View.VISIBLE);
                    progressLL4.setVisibility(View.GONE);
                    llEditUser.setVisibility(View.GONE);
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

    static class LongOperation extends AsyncTask<String, String, String> {

        private WeakReference<UserEditActivity> employesActivityWeakReference;
        private Connection connection;

        LongOperation(UserEditActivity employesActivityWeakReference) {
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
