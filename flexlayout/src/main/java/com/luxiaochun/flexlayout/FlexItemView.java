package com.luxiaochun.flexlayout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * ProjectName: JiuZhou
 * PackageName: com.example.jun.jiuzhou.FlexLayout
 * Author: jun
 * Date: 2018-04-12 09:41
 */
public class FlexItemView<T> extends LinearLayout {
    public LinearLayout llItem;
    public TextView tvName;
    public ImageView imgDelete;
    private T bean;
    //可编辑状态
    private boolean editable = false;

    public FlexItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.flexlayout_item, this, true);
        llItem = findViewById(R.id.flex_item_ll);
        tvName = findViewById(R.id.flex_item_name_tv);
        imgDelete = findViewById(R.id.flex_item_delete_img);
    }

    public T getBean() {
        return bean;
    }

    public void setBean(T bean) {
        this.bean = bean;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        if (editable) {
            imgDelete.setVisibility(View.VISIBLE);
            imgDelete.setEnabled(false);
        } else {
            imgDelete.setVisibility(View.GONE);
            imgDelete.setEnabled(true);
        }
    }
}
