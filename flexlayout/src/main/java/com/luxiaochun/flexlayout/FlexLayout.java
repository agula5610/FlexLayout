package com.luxiaochun.flexlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * ProjectName: JiuZhou(待优化)
 * PackageName: com.example.jun.jiuzhou.FlexLayout
 * Author: jun
 * Date: 2018-04-12 09:41
 */
public class FlexLayout<T> extends LinearLayout {
    public static final int FLEX_MODE_NORMAL = 0;//默认
    public static final int FLEX_MODE_REVERSE = 1; //反向
    private int flexMode = 0;
    private boolean editable = false;
    private FlexboxLayout flexbox_layout;
    private List<T> dataList = new ArrayList<>();
    private List<String> nameList = new ArrayList<>();
    private int maxLimited = -1;
    private Context mContext;
    private OnItemClickListener listener;

    public FlexLayout(Context context) {
        this(context, null);
    }

    public FlexLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(context);
    }

    private void initView(Context context) {
        flexbox_layout = new FlexboxLayout(context);
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.MATCH_PARENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
        addView(flexbox_layout, params);
    }

    /**
     * 添加数据(子弹)
     * 当是普通状态时，view从尾部添加
     * 当时搜索记录状态时，view从头部添加
     *
     * @param bean
     */
    public void addBullet(T bean, String name) {
        if (flexMode == 2) {
            flexbox_layout.removeAllViews();
            dataList.add(0, bean);
            nameList.add(0, name);
            for (int i = 0; i < dataList.size(); i++) {
                flexbox_layout.addView(createNewFlexItem(dataList.get(i), nameList.get(i)));
            }
        } else {
            flexbox_layout.addView(createNewFlexItem(bean, name));
            dataList.add(bean);
        }
    }

    /**
     * 添加数据(子弹)
     * 当是普通状态时，view从尾部添加
     * 当时搜索记录状态时，view从头部添加
     *
     * @param name
     */
    public void addBullet(String name) {
        if (flexMode == 2) {
            flexbox_layout.removeAllViews();
            nameList.add(0, name);
            for (int i = 0; i < dataList.size(); i++) {
                flexbox_layout.addView(createNewFlexItem(dataList.get(i), nameList.get(i)));
            }
        } else {
            flexbox_layout.addView(createNewFlexItem(name));
        }
    }

    /**
     * 一次性装弹
     *
     * @param nameList
     */
    public void loadBullets(List<String> nameList) {
        if (flexMode == 2) {
            flexbox_layout.removeAllViews();
            for (int i = 0; i < nameList.size(); i++) {
                flexbox_layout.addView(createNewFlexItem(nameList.get(i)));
            }
        } else {
            for (int i = 0; i < nameList.size(); i++) {
                flexbox_layout.addView(createNewFlexItem(nameList.get(i)));
            }
        }
        this.nameList.addAll(nameList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener<T> {
        void onClick(T bean);
    }

    private FlexItemView createNewFlexItem(final T bean, String name) {
        final FlexItemView<T> itemView = new FlexItemView(mContext, null);
        itemView.tvName.setText(name);
        itemView.llItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(bean);
                }
            }
        });
        itemView.imgDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                flexbox_layout.removeView(itemView);
            }
        });
        itemView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                for (int i = 0; i < flexbox_layout.getChildCount(); i++) {
                    if (flexbox_layout.getChildAt(i) instanceof FlexItemView) {
                        ((FlexItemView) flexbox_layout.getChildAt(i)).setEditable(true);
                    }
                }
                return false;
            }
        });
        itemView.setBean(bean);
        return itemView;
    }

    private FlexItemView createNewFlexItem(final String name) {
        final FlexItemView<String> itemView = new FlexItemView<>(mContext, null);
        itemView.tvName.setText(name);
        itemView.llItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editable) {
                    flexbox_layout.removeView(itemView);
                    nameList.remove(name);
                } else {
                    if (listener != null) {
                        listener.onClick(name);
                    }
                }
            }
        });
        itemView.llItem.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                editable = !editable;
                for (int i = 0; i < flexbox_layout.getChildCount(); i++) {
                    if (flexbox_layout.getChildAt(i) instanceof FlexItemView) {
                        ((FlexItemView) flexbox_layout.getChildAt(i)).setEditable(editable);
                    }
                }
                return true;
            }
        });
        return itemView;
    }

    public int getMaxLimited() {
        return maxLimited;
    }


    public void setMaxLimitedLine(int maxLimitedLine) {
        flexbox_layout.setLimitedLine(maxLimitedLine);
    }

    public int getFlexMode() {
        return flexMode;
    }

    public void setFlexMode(int flexMode) {
        this.flexMode = flexMode;
    }


    public void setMaxLimited(int maxLimited) {
        this.maxLimited = maxLimited;
    }

    /**
     * 获取全部数据
     *
     * @return
     */
    public List<T> getDataList() {
        return dataList;
    }

    /**
     * 获取全部数据
     *
     * @return
     */
    public List<String> getNameList() {
        return nameList;
    }
}
