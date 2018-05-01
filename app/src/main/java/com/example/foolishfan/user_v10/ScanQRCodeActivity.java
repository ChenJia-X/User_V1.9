package com.example.foolishfan.user_v10;

import android.app.Activity;
import android.content.Intent;

import com.google.zxing.Result;
import com.vondear.rxtools.RxTool;
import com.vondear.rxtools.activity.ActivityScanerCode;

public class ScanQRCodeActivity extends ActivityScanerCode {
    private static final String TAG = "ScanQRCodeActivity";

    @Override
    public void handleDecode(Result result) {
        RxTool.init(this);
        super.handleDecode(result);
        Intent intent = new Intent();
        intent.putExtra("result", result.getText());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
