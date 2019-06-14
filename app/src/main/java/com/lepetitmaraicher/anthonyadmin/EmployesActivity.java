package com.lepetitmaraicher.anthonyadmin;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;

import com.BoardiesITSolutions.AndroidMySQLConnector.Connection;
import com.BoardiesITSolutions.AndroidMySQLConnector.Exceptions.InvalidSQLPacketException;
import com.BoardiesITSolutions.AndroidMySQLConnector.Exceptions.MySQLConnException;
import com.BoardiesITSolutions.AndroidMySQLConnector.Exceptions.MySQLException;
import com.BoardiesITSolutions.AndroidMySQLConnector.IConnectionInterface;
import com.BoardiesITSolutions.AndroidMySQLConnector.IResultInterface;
import com.BoardiesITSolutions.AndroidMySQLConnector.MySQLRow;
import com.BoardiesITSolutions.AndroidMySQLConnector.ResultSet;
import com.BoardiesITSolutions.AndroidMySQLConnector.Statement;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class EmployesActivity extends AppCompatActivity implements View.OnClickListener {

    List<User> users;
    ListView listView;
    int selectedId = -1;
    String[] values;
    Button bRetour, bOK;
    LinearLayout progressLL;
    Space space;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_employes);
        setRequestedOrientation(MainActivity.activityInfo);
        MainActivity.user = null;
        bRetour = findViewById(R.id.bRetour);
        bRetour.setOnClickListener(this);
        bOK = findViewById(R.id.bOK);
        bOK.setOnClickListener(this);
        progressLL = findViewById(R.id.progressLL);
        space = findViewById(R.id.spacer);
        listView = findViewById(R.id.listView);
        LongOperation longOperation = new LongOperation(this);
        longOperation.execute("SELECT * FROM users");
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bOK) {
            setResult(RESULT_OK);
            String user = values[selectedId];
            for (User u : users) {
                if (u.getBadgeName().equals(user)) {
                    MainActivity.user = u;
                    break;
                }
            }
        } else setResult(RESULT_CANCELED);
        finish();
    }

    public void setListView() {
        final AppCompatActivity activity = this;
        List<String> list = new ArrayList<>();
        for (User u : users) {
            list.add(u.getBadgeName());
        }
        values = list.toArray(new String[0]);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressLL.setVisibility(View.GONE);
                listView.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, android.R.id.text1, values));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (selectedId == -1) {
                            bOK.setVisibility(View.VISIBLE);
                            space.setVisibility(View.GONE);
                        }
                        selectedId = position;
                    }
                });
                listView.setVisibility(View.VISIBLE);
            }
        });
    }

    static class LongOperation extends AsyncTask<String, String, String> {

        private WeakReference<EmployesActivity> employesActivityWeakReference;
        private Connection connection;

        LongOperation(EmployesActivity employesActivityWeakReference) {
            this.employesActivityWeakReference = new WeakReference<>(employesActivityWeakReference);
        }

        @Override
        protected String doInBackground(final String... params) {
            connection = new Connection("acid8x.no-ip.biz", "anthony", "anthony", 3306, "anthony", new IConnectionInterface() {

                @Override
                public void actionCompleted() {
                    Statement statement = connection.createStatement();
                    statement.executeQuery(params[0], new IResultInterface() {
                        @Override
                        public void executionComplete(ResultSet resultSet) {
                            MySQLRow row;
                            employesActivityWeakReference.get().users = new ArrayList<>();
                            while ((row = resultSet.getNextRow()) != null) {
                                try {
                                    String badge = row.getString("badgeId");
                                    String name = row.getString("badgeName");
                                    String lastPunchString = row.getString("lastPunch");
                                    long lastPunch = Long.parseLong(lastPunchString);
                                    String currentJob = row.getString("currentJob");
                                    if (currentJob == null) currentJob = "";
                                    int isAdmin = row.getInt("isAdmin");
                                    User user = new User(badge, name, lastPunch, currentJob, isAdmin);
                                    employesActivityWeakReference.get().users.add(user);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            employesActivityWeakReference.get().setListView();
                        }

                        @Override
                        public void handleInvalidSQLPacketException(InvalidSQLPacketException ex) {
                            ex.printStackTrace();
                        }

                        @Override
                        public void handleMySQLException(MySQLException ex) {
                            ex.printStackTrace();
                        }

                        @Override
                        public void handleIOException(IOException ex) {
                            ex.printStackTrace();
                        }

                        @Override
                        public void handleMySQLConnException(MySQLConnException ex) {
                            ex.printStackTrace();
                        }

                        @Override
                        public void handleException(Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }

                @Override
                public void handleInvalidSQLPacketException(InvalidSQLPacketException ex) {
                    ex.printStackTrace();
                }

                @Override
                public void handleMySQLException(MySQLException ex) {
                    ex.printStackTrace();
                }

                @Override
                public void handleIOException(IOException ex) {
                    ex.printStackTrace();
                }

                @Override
                public void handleMySQLConnException(MySQLConnException ex) {
                    ex.printStackTrace();
                }

                @Override
                public void handleException(Exception ex) {
                    ex.printStackTrace();
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