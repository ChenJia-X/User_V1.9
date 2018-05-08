package com.example.foolishfan.user_v10;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class Jiqiku extends AppCompatActivity {
    private Context context = Jiqiku.this;
    private ListView listView;
    private List<String> fileList;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jiqiku);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/user_v1.9";
        fileList = FileUtils.getFileList(path);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new ArrayAdapter<>(Jiqiku.this, android.R.layout.simple_list_item_1, fileList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //todo-debug
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromFile(new File(fileList.get(i)));
                intent.setDataAndType(uri, "text/plain");
                startActivity(intent);
            }
        });
        //长按删除
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int j = i;
                new AlertDialog.Builder(context)
                        .setMessage("是否需要删除该报告？")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                File file = new File(fileList.get(j));
                                if (file.delete()) {
                                    Toast.makeText(Jiqiku.this, "删除成功！", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Jiqiku.this, "删除失败！", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setCancelable(true)
                        .show();
                return true;//todo-debug 这什么返回值？可能存在问题
            }
        });
    }
}
