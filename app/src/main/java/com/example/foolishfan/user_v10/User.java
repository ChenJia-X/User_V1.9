package com.example.foolishfan.user_v10;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class User extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //加载一个布局
        setContentView(R.layout.user);
        //找到按钮
        Button btn1 = (Button) findViewById(R.id.new_measure);
        //给button按钮设置一个点击事件
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(User.this, NewMeasure.class);
                startActivity(intent1);
            }
        });


        Button btn3 = (Button) findViewById(R.id.jiqiku);
        //给button按钮设置一个点击事件
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3 = new Intent(User.this, Jiqiku.class);
                startActivity(intent3);
            }
        });

        Button btn4 = (Button) findViewById(R.id.help);
        //给button按钮设置一个点击事件
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent4 = new Intent(User.this, Help.class);
                startActivity(intent4);
            }
        });

        Button btn5 = (Button) findViewById(R.id.returnback);
        //给button按钮设置一个点击事件
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent5 = new Intent(User.this, Login.class);
                startActivity(intent5);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}