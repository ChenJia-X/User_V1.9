package com.example.foolishfan.user_v10;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static com.example.foolishfan.user_v10.GlobalConstant.REPORT_PATH;
import static com.example.foolishfan.user_v10.Utils.showToast;

public class ResultActivity extends AppCompatActivity {
    private EditText deviceNumber;
    private EditText deviation1;
    private EditText deviation2;
    private EditText deviation3;
    private EditText deviation4;
    private EditText tester;
    private EditText date;
    private Button button;

    private static final int REQUEST_WRITE_PERMISSION = 110;
    private String filePath;
    private String de1;
    private String de2;
    private String de3;
    private String de4;
    private String deviceNumberS;
    private String testerS;
    private String dt;

    public static void actionStart(Context context, Bundle bundle) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        getWidget();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            double[] results = bundle.getDoubleArray("result");
            de1 = String.valueOf(results[0]);
            de2 = String.valueOf(results[1]);
            de3 = String.valueOf(results[2]);
            de4 = String.valueOf(results[3]);
            deviceNumberS = bundle.getString("deviceNumber");
            testerS = bundle.getString("tester");
            dt = bundle.getString("date");

            deviation1.setText(de1);
            deviation2.setText(de2);
            deviation3.setText(de3);
            deviation4.setText(de4);
            deviceNumber.setText(deviceNumberS);
            tester.setText(testerS);
            date.setText(dt);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ResultActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ResultActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
                    return;
                }
                produceReport();
            }
        });
    }

    private void produceReport() {
        createDirectory();
        createFile();
        writeData();
    }

    private void writeData() {
        try {
            FileWriter fileWriter = new FileWriter(filePath, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.append("机器编号：").append(deviceNumberS).append("\n");
            bufferedWriter.append("前地脚垂直调整量：").append(de1).append("\n");
            bufferedWriter.append("后地脚垂直调整量：").append(de2).append("\n");
            bufferedWriter.append("前地脚水平调整量：").append(de3).append("\n");
            bufferedWriter.append("后地脚水平调整量：").append(de4).append("\n");
            bufferedWriter.append("测量人员：").append(testerS).append("\n");
            bufferedWriter.append("测量日期：").append(dt).append("\n");

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getWidget() {
        deviceNumber = (EditText) findViewById(R.id.editText2);
        deviation1 = (EditText) findViewById(R.id.editText3);
        deviation2 = (EditText) findViewById(R.id.editText4);
        deviation3 = (EditText) findViewById(R.id.editText5);
        deviation4 = (EditText) findViewById(R.id.editText6);
        tester = (EditText) findViewById(R.id.editText7);
        date = (EditText) findViewById(R.id.editText8);
        button = (Button) findViewById(R.id.button);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    produceReport();
                    return;
                }
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                        //用户勾选不再询问按钮
                        // 解释原因，并且引导用户至设置页手动授权
                        new AlertDialog.Builder(this)
                                .setMessage("需要开启权限，否则无法使用。请到权限管理页面手动给予权限")
                                .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //引导用户至设置页手动授权
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //引导用户手动授权，权限请求失败
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        //引导用户手动授权，权限请求失败
                                    }
                                })
                                .show();
                    }
                }
        }
    }

    /**
     * 创建单个文件
     */
    public void createFile() {
        filePath = REPORT_PATH + dt + ".txt";
        int result = FileUtils.CreateFile(filePath);
        showResult(result);
    }

    /**
     * 创建文件夹
     */
    public void createDirectory() {
        int result = FileUtils.createDir(REPORT_PATH);
        showResult(result);
    }

    /**
     * 显示结果
     *
     * @param result 结果码
     */
    private void showResult(int result) {
        switch (result) {
            case FileUtils.FLAG_SUCCESS:
                showToast(ResultActivity.this, "创建成功");
                break;
            case FileUtils.FLAG_EXISTS:
                showToast(ResultActivity.this, "文件已存在");
                break;
            case FileUtils.FLAG_FAILED:
                showToast(ResultActivity.this, "创建失败");
                break;
        }
    }

}
