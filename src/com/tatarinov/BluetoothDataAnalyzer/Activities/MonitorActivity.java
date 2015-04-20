package com.tatarinov.BluetoothDataAnalyzer.Activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import com.tatarinov.BluetoothDataAnalyzer.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MonitorActivity extends Activity {

    private TextView tvMonitor;
    private ScrollView logContainer;
    private AsyncTask<Void, String, Void> logReadTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.monitor_data);
        tvMonitor = (TextView) findViewById(R.id.tvMonitor);
        logContainer = (ScrollView)findViewById(R.id.scrollView);

        logReadTask = new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Process process = Runtime.getRuntime().exec("logcat");
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.contains("DATA_READ:")) {
                            publishProgress(line);
                        }
                    }
                } catch (IOException e) {
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                tvMonitor.append(values[0] + "\n");
                logContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        logContainer.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        }.execute();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (logReadTask != null && logReadTask.getStatus() == AsyncTask.Status.RUNNING){
            logReadTask.cancel(true);
        }
    }
}
