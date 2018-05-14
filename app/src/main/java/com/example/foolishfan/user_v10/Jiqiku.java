package com.example.foolishfan.user_v10;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import static com.example.foolishfan.user_v10.GlobalConstant.REPORT_PATH;

public class Jiqiku extends AppCompatActivity {
    private Context context = Jiqiku.this;
    private ListView listView;
    private List<String> fileList;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jiqiku);
        fileList = FileUtils.getFileList(REPORT_PATH);
        listView = (ListView) findViewById(R.id.list_view);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(Jiqiku.this, android.R.layout.simple_list_item_1, fileList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //todo-debug
                Intent intent = new Intent(Jiqiku.this, ReportActivity.class);
                intent.putExtra("report_path", fileList.get(i));
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
                                File file = new File(REPORT_PATH + fileList.get(j));
                                if (file.delete()) {
                                    Toast.makeText(Jiqiku.this, "删除成功！", Toast.LENGTH_SHORT).show();
                                    fileList.remove(j);
                                    adapter.notifyDataSetChanged();
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
