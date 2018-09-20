package com.luxiaochun.flexlayout;

/**
 * ProjectName: JiuZhou
 * PackageName: com.example.jun.jiuzhou.FlexLauout
 * Author: jun
 * Date: 2018-09-13 15:32
 * Copyright: (C)HESC Co.,Ltd. 2016. All rights reserved.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.luxiaochun.flexlayout.R.styleable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlexboxLayout extends ViewGroup {
    private List<FlexLine> mFlexLines;
    private int limitedLine = -1;

    public int getLimitedLine() {
        return limitedLine;
    }

    public void setLimitedLine(int limitedLine) {
        this.limitedLine = limitedLine;
    }

    public FlexboxLayout(Context context) {
        this(context, null);
    }

    public FlexboxLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlexboxLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mFlexLines = new ArrayList();
        TypedArray a = context.obtainStyledAttributes(attrs, styleable.FlexboxLayout, defStyleAttr, 0);
        a.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.measureHorizontal(widthMeasureSpec, heightMeasureSpec);
    }

    private void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int childState = 0;
        this.mFlexLines.clear();
        int childCount = this.getChildCount();
        int paddingStart = ViewCompat.getPaddingStart(this);
        int paddingEnd = ViewCompat.getPaddingEnd(this);
        int largestHeightInRow = -2147483648;
        FlexLine flexLine = new FlexLine();
        flexLine.mainSize = paddingStart + paddingEnd;

        for (int i = 0; i < childCount; ++i) {
            View child = this.getChildAt(i);
            if (child == null) {
                this.addFlexLineIfLastFlexItem(i, childCount, flexLine);
            } else {
                FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) child.getLayoutParams();

                int childWidth = lp.width;

                int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, this.getPaddingLeft() + this.getPaddingRight() + lp.leftMargin + lp.rightMargin, childWidth);
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                childState = View.combineMeasuredStates(childState, ViewCompat.getMeasuredState(child));
                largestHeightInRow = Math.max(largestHeightInRow, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);

                if (this.isWrapRequired(widthMode, widthSize, flexLine.mainSize, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin)) {
//                    if (mFlexLines.size() == limitedLine - 1) {
//                        break;
//                    }
                    this.mFlexLines.add(flexLine);
                    flexLine = new FlexboxLayout.FlexLine();
                    flexLine.itemCount = 1;
                    flexLine.mainSize = paddingStart + paddingEnd;
                    largestHeightInRow = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                } else {
                    ++flexLine.itemCount;
                }
                flexLine.mainSize += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                flexLine.crossSize = Math.max(flexLine.crossSize, largestHeightInRow);

                this.addFlexLineIfLastFlexItem(i, childCount, flexLine);
            }
        }

        this.determineCrossSize(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom());
        this.stretchViews();
        this.setMeasuredDimensionForFlex(widthMeasureSpec, heightMeasureSpec, childState);
    }

    private void addFlexLineIfLastFlexItem(int childIndex, int childCount, FlexboxLayout.FlexLine flexLine) {
        if (childIndex == childCount - 1 && flexLine.itemCount != 0) {
            this.mFlexLines.add(flexLine);
        }
    }


    private void determineCrossSize(int heightMeasureSpec, int paddingAlongCrossAxis) {
        int mode;
        int size;
        mode = MeasureSpec.getMode(heightMeasureSpec);
        size = MeasureSpec.getSize(heightMeasureSpec);

        if (mode == MeasureSpec.EXACTLY) {
            int totalCrossSize = this.getSumOfCrossSize() + paddingAlongCrossAxis;
            if (this.mFlexLines.size() == 1) {
                (this.mFlexLines.get(0)).crossSize = size - paddingAlongCrossAxis;
            } else if (this.mFlexLines.size() >= 2 && totalCrossSize < size) {
                float spaceBetweenFlexLine;
                FlexboxLayout.FlexLine flexLine;
                spaceBetweenFlexLine = (float) (size - totalCrossSize) / (float) this.mFlexLines.size();
                float accumulatedError5 = 0.0F;

                for (int i = 0; i < this.mFlexLines.size(); ++i) {
                    flexLine = this.mFlexLines.get(i);
                    float newCrossSizeAsFloat = (float) flexLine.crossSize + spaceBetweenFlexLine;
                    if (i == this.mFlexLines.size() - 1) {
                        newCrossSizeAsFloat += accumulatedError5;
                        accumulatedError5 = 0.0F;
                    }

                    int newCrossSize = Math.round(newCrossSizeAsFloat);
                    accumulatedError5 += newCrossSizeAsFloat - (float) newCrossSize;
                    if (accumulatedError5 > 1.0F) {
                        ++newCrossSize;
                        --accumulatedError5;
                    } else if (accumulatedError5 < -1.0F) {
                        --newCrossSize;
                        ++accumulatedError5;
                    }

                    flexLine.crossSize = newCrossSize;
                }
            }
        }

    }

    private void stretchViews() {
        View view;
        int viewIndex = 0;
        Iterator var4 = this.mFlexLines.iterator();

        while (var4.hasNext()) {
            FlexboxLayout.FlexLine flexLine = (FlexboxLayout.FlexLine) var4.next();

            for (int i = 0; i < flexLine.itemCount; ++viewIndex) {
                view = this.getChildAt(viewIndex);
                this.stretchViewVertically(view, flexLine.crossSize);
                ++i;
            }
        }
    }

    private void stretchViewVertically(View view, int crossSize) {
        FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) view.getLayoutParams();
        int newHeight = crossSize - lp.topMargin - lp.bottomMargin;
        newHeight = Math.max(newHeight, 0);
        view.measure(MeasureSpec.makeMeasureSpec(view.getMeasuredWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
    }

    private void setMeasuredDimensionForFlex(int widthMeasureSpec, int heightMeasureSpec, int childState) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int calculatedMaxHeight;
        int calculatedMaxWidth;
        calculatedMaxHeight = this.getSumOfCrossSize() + this.getPaddingTop() + this.getPaddingBottom();
        calculatedMaxWidth = this.getLargestMainSize();

        int widthSizeAndState;
        switch (widthMode) {
            case -2147483648:
                if (widthSize < calculatedMaxWidth) {
                    childState = ViewCompat.combineMeasuredStates(childState, 16777216);
                } else {
                    widthSize = calculatedMaxWidth;
                }

                widthSizeAndState = ViewCompat.resolveSizeAndState(widthSize, widthMeasureSpec, childState);
                break;
            case 0:
                widthSizeAndState = ViewCompat.resolveSizeAndState(calculatedMaxWidth, widthMeasureSpec, childState);
                break;
            case 1073741824:
                if (widthSize < calculatedMaxWidth) {
                    childState = ViewCompat.combineMeasuredStates(childState, 16777216);
                }

                widthSizeAndState = ViewCompat.resolveSizeAndState(widthSize, widthMeasureSpec, childState);
                break;
            default:
                throw new IllegalStateException("Unknown width mode is set: " + widthMode);
        }

        int heightSizeAndState;
        switch (heightMode) {
            case -2147483648:
                if (heightSize < calculatedMaxHeight) {
                    childState = ViewCompat.combineMeasuredStates(childState, 256);
                } else {
                    heightSize = calculatedMaxHeight;
                }

                heightSizeAndState = ViewCompat.resolveSizeAndState(heightSize, heightMeasureSpec, childState);
                break;
            case 0:
                heightSizeAndState = ViewCompat.resolveSizeAndState(calculatedMaxHeight, heightMeasureSpec, childState);
                break;
            case 1073741824:
                if (heightSize < calculatedMaxHeight) {
                    childState = ViewCompat.combineMeasuredStates(childState, 256);
                }

                heightSizeAndState = ViewCompat.resolveSizeAndState(heightSize, heightMeasureSpec, childState);
                break;
            default:
                throw new IllegalStateException("Unknown height mode is set: " + heightMode);
        }

        this.setMeasuredDimension(widthSizeAndState, heightSizeAndState);
    }

    /**
     * 是否需要换行
     *
     * @param mode
     * @param maxSize
     * @param currentLength
     * @param childLength
     * @return
     */
    private boolean isWrapRequired(int mode, int maxSize, int currentLength, int childLength) {
        return (mode == 1073741824 || mode == -2147483648) && maxSize < currentLength + childLength;
    }

    private int getLargestMainSize() {
        int largestSize = -2147483648;

        FlexboxLayout.FlexLine flexLine;
        for (Iterator var2 = this.mFlexLines.iterator(); var2.hasNext(); largestSize = Math.max(largestSize, flexLine.mainSize)) {
            flexLine = (FlexboxLayout.FlexLine) var2.next();
        }

        return largestSize;
    }

    private int getSumOfCrossSize() {
        int sum = 0;

        FlexboxLayout.FlexLine flexLine;
        for (Iterator var2 = this.mFlexLines.iterator(); var2.hasNext(); sum += flexLine.crossSize) {
            flexLine = (FlexboxLayout.FlexLine) var2.next();
        }

        return sum;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        boolean isRtl;
        isRtl = layoutDirection == 1;
        this.layoutHorizontal(isRtl, left, top, right, bottom);
    }

    private void layoutHorizontal(boolean isRtl, int left, int top, int right, int bottom) {
        int paddingLeft = this.getPaddingLeft();
        int paddingRight = this.getPaddingRight();
        int currentViewIndex = 0;
        int height = bottom - top;
        int width = right - left;
        int childBottom = height - this.getPaddingBottom();
        int childTop = this.getPaddingTop();

        FlexboxLayout.FlexLine flexLine;
        for (Iterator var15 = this.mFlexLines.iterator(); var15.hasNext(); childBottom -= flexLine.crossSize) {
            flexLine = (FlexboxLayout.FlexLine) var15.next();
            float spaceBetweenItem = 0.0F;
            float childLeft;
            float childRight;
            childLeft = (float) paddingLeft;
            childRight = (float) (width - paddingRight);

            spaceBetweenItem = Math.max(spaceBetweenItem, 0.0F);

            for (int i = 0; i < flexLine.itemCount; ++i) {
                View child = this.getChildAt(currentViewIndex);
                if (child != null) {
                    if (child.getVisibility() == View.GONE) {
                        ++currentViewIndex;
                    } else {
                        FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) child.getLayoutParams();
                        childLeft += (float) lp.leftMargin;
                        childRight -= (float) lp.rightMargin;
                        if (isRtl) {
                            this.layoutSingleChildHorizontal(child, Math.round(childRight) - child.getMeasuredWidth(), childTop, Math.round(childRight), childTop + child.getMeasuredHeight());
                        } else {
                            this.layoutSingleChildHorizontal(child, Math.round(childLeft), childTop, Math.round(childLeft) + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
                        }

                        childLeft += (float) child.getMeasuredWidth() + spaceBetweenItem + (float) lp.rightMargin;
                        childRight -= (float) child.getMeasuredWidth() + spaceBetweenItem + (float) lp.leftMargin;
                        ++currentViewIndex;
                    }
                }
            }

            childTop += flexLine.crossSize;
        }

    }

    private void layoutSingleChildHorizontal(View view, int left, int top, int right, int bottom) {
        FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) view.getLayoutParams();
        view.layout(left, top + lp.topMargin, right, bottom + lp.topMargin);
    }

    public FlexboxLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FlexboxLayout.LayoutParams(this.getContext(), attrs);
    }

    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new FlexboxLayout.LayoutParams(p);
    }

    @Override
    protected FlexboxLayout.LayoutParams generateDefaultLayoutParams() {
        return new FlexboxLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    private static class FlexLine {
        int mainSize;
        int crossSize;
        int itemCount;
        List<Integer> indicesAlignSelfStretch;

        private FlexLine() {
            this.indicesAlignSelfStretch = new ArrayList();
        }
    }


    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, styleable.FlexboxLayout_Layout);
            a.recycle();
        }

        public LayoutParams(FlexboxLayout.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(int width, int height) {
            super(new ViewGroup.LayoutParams(width, height));
        }
    }
}
