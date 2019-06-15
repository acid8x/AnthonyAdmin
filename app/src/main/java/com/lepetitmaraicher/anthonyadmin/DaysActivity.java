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

import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.Connection;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.InvalidSQLPacketException;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.MySQLConnException;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.MySQLException;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.IConnectionInterface;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.IResultInterface;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.MySQLRow;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.ResultSet;
import com.lepetitmaraicher.anthonyadmin.AndroidMySQLConnector.Statement;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.aigestudio.datepicker.views.DatePicker;

public class DaysActivity extends AppCompatActivity implements View.OnClickListener {

    ListView listView;
    String[] values, columnsNameNotCombined = new String[]{"Date","Heure","Nom","Job"}, columnsNameCombined = new String[]{"Date","Nom","Job","Quantite"};
    Button bRetour, bExcel;
    TextView tvDuplicate;
    Switch swDuplicate;
    String oui = "OUI", non = "NON";
    boolean firstRun = true;
    List<String> datePicked, sList, sListCombined;
    DatePicker picker;
    LongOperation longOperation;
    LinearLayout progressLL, datePickerLL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_dayspunchs);
        setRequestedOrientation(MainActivity.activityInfo);
        bRetour = findViewById(R.id.bRetourPunchs2);
        bRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
        bExcel = findViewById(R.id.bExcelPunchs2);
        bExcel.setOnClickListener(this);
        bExcel.setEnabled(false);
        tvDuplicate = findViewById(R.id.tvDuplicate2);
        swDuplicate = findViewById(R.id.swDuplicate2);
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
        listView = findViewById(R.id.listViewPunchs2);
        progressLL = findViewById(R.id.progressLLpunchs2);
        datePickerLL = findViewById(R.id.datePickerLL);
        longOperation = new LongOperation(this);
        picker = findViewById(R.id.main_dp);
        picker.setDate(getYear(),getMonth());
        picker.setOnDateSelectedListener(new DatePicker.OnDateSelectedListener() {
            @Override
            public void onDateSelected(List<String> date) { //onOkPressed
                datePicked = new ArrayList<>();
                for (String s : date) {
                    String[] separated = s.split("-");
                    if (separated[1].length() == 1) separated[1] = "0" + separated[1];
                    if (separated[2].length() == 1) separated[2] = "0" + separated[2];
                    String newDate = separated[0] + "-" + separated[1] + "-" + separated[2];
                    datePicked.add(newDate);
                }
                if (datePicked.size() > 0) {
                    StringBuilder query = new StringBuilder("SELECT * FROM punchs WHERE date IN ('" + datePicked.get(0) + "'");
                    if (datePicked.size() > 1) {
                        for (int i = 1; i < datePicked.size(); i++) {
                            query.append(",'").append(datePicked.get(i)).append("'");
                        }
                    }
                    query.append(")");
                    longOperation.execute(query.toString());
                }
            }
        });
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
                if (v.getId() == R.id.bRetourPunchs2) {
                    setResult(RESULT_OK);
                    finish();
                } else if (v.getId() == R.id.bExcelPunchs2) {
                    saveExcelFile();
                }
                b.setTextColor(Color.BLACK);
                b.setScaleX(1);
                b.setScaleY(1);
            }
        }, 650);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private int getYear() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.CANADA);
        String s = dateFormat.format(calendar.getTime());
        char[] chars = s.toCharArray();
        int i = 0;
        for (char c : chars) {
            i *= 10;
            i += (c-48);
        }
        return i;
    }

    private int getMonth() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM", Locale.CANADA);
        String s = dateFormat.format(calendar.getTime());
        char[] chars = s.toCharArray();
        int i = 0;
        for (char c : chars) {
            i *= 10;
            i += (c-48);
        }
        return i;
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
        // TODO: 2019-06-08 FileUriExposedException
    }

    private boolean isOdd( int val ) { return (val & 0x01) != 0; }

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

        private WeakReference<DaysActivity> employesActivityWeakReference;
        private Connection connection;

        LongOperation(DaysActivity employesActivityWeakReference) {
            this.employesActivityWeakReference = new WeakReference<>(employesActivityWeakReference);
        }

        @Override
        protected String doInBackground(final String... params) {
            connection = new Connection(Constants.IP, Constants.LOGIN, Constants.PASSWORD, 3306, Constants.DB, new IConnectionInterface() {

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
            employesActivityWeakReference.get().datePickerLL.setVisibility(View.GONE);
            employesActivityWeakReference.get().progressLL.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }
    }
}
