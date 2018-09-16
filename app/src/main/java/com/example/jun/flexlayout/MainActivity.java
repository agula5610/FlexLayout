package com.example.jun.flexlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * 正向版本的flexlayout,用于添加一些信息
     * @param view
     */
    public void normalClick(View view) {

    }

    /**
     * 反向版的flexlayout,用于添加搜索记录
     * @param view
     */
    public void reverseClick(View view) {
    }
}
