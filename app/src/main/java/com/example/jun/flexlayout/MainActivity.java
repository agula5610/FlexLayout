package com.example.jun.flexlayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.luxiaochun.flexlayout.FlexLayout;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FlexLayout<String> flexLayout;
    String[] items = {"张三", "李四",
            "张三", "李四",
            "张三", "李四",
            "张三", "李四",
            "张三", "李四",
            "张三", "李四"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flexLayout = findViewById(R.id.flexlayout);
    }

    /**
     * 正向版本的flexlayout,用于添加一些信息
     *
     * @param view
     */
    public void normalClick(View view) {
        List<String> list = Arrays.asList(items);
        flexLayout.setMaxLimitedLine(2);
        flexLayout.loadBullets(list);
    }

    /**
     * 反向版的flexlayout,用于添加搜索记录
     *
     * @param view
     */
    public void reverseClick(View view) {
    }
}
