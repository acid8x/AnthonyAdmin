package com.lepetitmaraicher.anthonyadmin;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.BoardiesITSolutions.AndroidMySQLConnector.Connection;
import com.BoardiesITSolutions.AndroidMySQLConnector.Exceptions.InvalidSQLPacketException;
import com.BoardiesITSolutions.AndroidMySQLConnector.Exceptions.MySQLConnException;
import com.BoardiesITSolutions.AndroidMySQLConnector.Exceptions.MySQLException;
import com.BoardiesITSolutions.AndroidMySQLConnector.IConnectionInterface;
import com.BoardiesITSolutions.AndroidMySQLConnector.IResultInterface;
import com.BoardiesITSolutions.AndroidMySQLConnector.MySQLRow;
import com.BoardiesITSolutions.AndroidMySQLConnector.ResultSet;
import com.BoardiesITSolutions.AndroidMySQLConnector.Statement;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PunchsActivity extends AppCompatActivity implements View.OnClickListener {

    ListView listView;
    String[] values, columnsNameNotCombined = new String[]{"Date","Heure","Nom","Job"}, columnsNameCombined = new String[]{"Date","Nom","Job","Quantite"};
    Button bRetour, bExcel;
    LinearLayout progressLL;
    TextView tvDuplicate;
    Switch swDuplicate;
    List<String> sList, sListCombined;
    String oui = "OUI", non = "NON";
    boolean firstRun = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        Intent i = getIntent();
        String query = "";
        if (i.hasExtra("QUERY")) query = i.getStringExtra("QUERY");
        else finish();
        setContentView(R.layout.activity_punchs);
        setRequestedOrientation(MainActivity.activityInfo);
        bRetour = findViewById(R.id.bRetourPunchs);
        bRetour.setOnClickListener(this);
        bExcel = findViewById(R.id.bExcelPunchs);
        bExcel.setOnClickListener(this);
        bExcel.setEnabled(false);
        tvDuplicate = findViewById(R.id.tvDuplicate);
        swDuplicate = findViewById(R.id.swDuplicate);
        swDuplicate.setEnabled(false);
        swDuplicate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tvDuplicate.setText(oui);
                    setListView(sListCombined);
                }
                else {
                    tvDuplicate.setText(non);
                    setListView(sList);
                }
            }
        });
        progressLL = findViewById(R.id.progressLLpunchs);
        listView = findViewById(R.id.listViewPunchs);
        LongOperation longOperation = new LongOperation(this);
        longOperation.execute(query);
    }

    public void saveExcelFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] columnsName;
                if (swDuplicate.isChecked()) columnsName = columnsNameCombined;
                else columnsName = columnsNameNotCombined;
                int columns = columnsName.length;
                int rows = values.length+1;

                int[] widths = new int[columns];

                Workbook wb = new HSSFWorkbook();

                CellStyle csTitle = wb.createCellStyle();
                csTitle.setFillForegroundColor(HSSFColor.ROSE.index);
                csTitle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

                CellStyle csEven = wb.createCellStyle();
                csEven.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
                csEven.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

                Cell cell;

                Sheet sheet1;
                sheet1 = wb.createSheet("Punchs");

                for (int r = 0; r < rows; r++) {
                    Row row = sheet1.createRow(r);
                    if (r > 0) {
                        String[] columnValue = new String[columns];
                        for (int i = 0; i < columns; i++) {
                            columnValue[i] = "";
                        }
                        String str = values[r-1];
                        char[] chars = str.toCharArray();
                        int index = 0;
                        for (char chr : chars) {
                            if (chr != ';') columnValue[index] += chr;
                            else index++;
                        }
                        for (int c = 0; c < columns; c++) {
                            if (columnValue[c].length() > widths[c]) widths[c] = columnValue[c].length();
                            cell = row.createCell(c);
                            cell.setCellValue(columnValue[c]);
                            if (!isOdd(r)) cell.setCellStyle(csEven);
                        }
                    } else {
                        for (int c = 0; c < columns; c++) {
                            widths[c] = 0;
                            cell = row.createCell(c);
                            cell.setCellValue(columnsName[c]);
                            cell.setCellStyle(csTitle);
                        }
                    }
                }

                for (int i = 0; i < columns; i++) {
                    sheet1.setColumnWidth(i,widths[i]*320);
                }

                String fileName = MainActivity.getDateTime() + "-punchs.xls";

                File file = new File(getApplicationContext().getExternalFilesDir(null), fileName);

                try (FileOutputStream os = new FileOutputStream(file)) {
                    wb.write(os);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                shareFile(file);
            }
        }).start();
    }

    private void shareFile(File file) {
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        intentShareFile.setType("application/excel");
        intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file.getAbsolutePath()));
        startActivity(Intent.createChooser(intentShareFile, "Share File"));
    }

    private boolean isOdd( int val ) { return (val & 0x01) != 0; }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onClick(final View v) {
        final Button b = findViewById(v.getId());
        b.setTextColor(Color.RED);
        b.setScaleX(1.2f);
        b.setScaleY(1.2f);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (v.getId() == R.id.bRetourPunchs) {
                    setResult(RESULT_OK);
                    finish();
                } else if (v.getId() == R.id.bExcelPunchs) {
                    saveExcelFile();
                }
                b.setTextColor(Color.BLACK);
                b.setScaleX(1);
                b.setScaleY(1);
            }
        }, 650);
    }

    public void setListView(List<String> list) {
        final AppCompatActivity activity = this;
        values = list.toArray(new String[0]);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (firstRun) {
                    firstRun = false;
                    progressLL.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    bExcel.setEnabled(true);
                    swDuplicate.setEnabled(true);
                }
                listView.setAdapter(new ArrayAdapter<>(activity, R.layout.list_item, android.R.id.text1, values));
            }
        });
    }

    static class LongOperation extends AsyncTask<String, String, String> {

        private WeakReference<PunchsActivity> employesActivityWeakReference;
        private Connection connection;

        LongOperation(PunchsActivity employesActivityWeakReference) {
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
                            employesActivityWeakReference.get().sList = new ArrayList<>();
                            List<String> sList = new ArrayList<>();
                            List<String> sListCombined = new ArrayList<>();
                            List<String> sListCombinedCount = new ArrayList<>();
                            while ((row = resultSet.getNextRow()) != null) {
                                try {
                                    String date = row.getString("date");
                                    String time = row.getString("time");
                                    String name = row.getString("name");
                                    String job = row.getString("job");
                                    sList.add(date+";"+time+";"+name+";"+job);
                                    sListCombined.add(date+";"+name+";"+job);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Map<String, Integer> map = new HashMap<>();
                            for (String i : sListCombined) {
                                Integer retrievedValue = map.get(i);
                                if (retrievedValue == null) map.put(i, 1);
                                else map.put(i, retrievedValue + 1);
                            }
                            for (String key : map.keySet()) {
                                sListCombinedCount.add(key+";"+map.get(key));
                            }
                            Collections.sort(sList);
                            Collections.sort(sListCombinedCount);
                            employesActivityWeakReference.get().sList = sList;
                            employesActivityWeakReference.get().sListCombined = sListCombinedCount;
                            employesActivityWeakReference.get().setListView(sList);
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