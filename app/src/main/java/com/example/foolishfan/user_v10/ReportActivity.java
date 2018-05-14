package com.example.foolishfan.user_v10;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import static com.example.foolishfan.user_v10.FileUtils.readTxtFile;
import static com.example.foolishfan.user_v10.GlobalConstant.REPORT_PATH;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        String report_path = REPORT_PATH + getIntent().getStringExtra("report_path");
        TextView report = (TextView) findViewById(R.id.report);
        report.setText(readTxtFile(report_path));
    }
}
