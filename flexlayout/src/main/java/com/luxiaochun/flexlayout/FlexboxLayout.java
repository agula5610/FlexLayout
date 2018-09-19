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
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import com.luxiaochun.flexlayout.R.styleable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FlexboxLayout extends ViewGroup {
    private int[] mReorderedIndices;
    private SparseIntArray mOrderCache;
    private List<FlexLine> mFlexLines;
    private boolean[] mChildrenFrozen;
    private int limitedLine = -1;

    public int getLimitedLine() {
        return limitedLine ;
    }

    public void setLimitedLine(int limitedLine) {
        this.limitedLine = limitedLine ;
    }

    public FlexboxLayout(Context context) {
        this(context,null);
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
        if (this.isOrderChangedFromLastMeasurement()) {
            this.mReorderedIndices = this.createReorderedIndices();
        }

        if (this.mChildrenFrozen == null || this.mChildrenFrozen.length < this.getChildCount()) {
            this.mChildrenFrozen = new boolean[this.getChildCount()];
        }
        this.measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        Arrays.fill(this.mChildrenFrozen, false);
    }

    public View getReorderedChildAt(int index) {
        return index >= 0 && index < this.mReorderedIndices.length ? this.getChildAt(this.mReorderedIndices[index]) : null;
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        this.mReorderedIndices = this.createReorderedIndices(child, index, params);
        super.addView(child, index, params);
    }

    private int[] createReorderedIndices(View viewBeforeAdded, int indexForViewBeforeAdded, ViewGroup.LayoutParams paramsForViewBeforeAdded) {
        int childCount = this.getChildCount();
        List<Order> orders = this.createOrders(childCount);
        Order orderForViewToBeAdded = new Order();
        if (viewBeforeAdded != null && paramsForViewBeforeAdded instanceof FlexboxLayout.LayoutParams) {
            orderForViewToBeAdded.order = ((FlexboxLayout.LayoutParams) paramsForViewBeforeAdded).order;
        } else {
            orderForViewToBeAdded.order = 1;
        }

        if (indexForViewBeforeAdded != -1 && indexForViewBeforeAdded != childCount) {
            if (indexForViewBeforeAdded < this.getChildCount()) {
                orderForViewToBeAdded.index = indexForViewBeforeAdded;

                for (int i = indexForViewBeforeAdded; i < childCount; ++i) {
                    ++(orders.get(i)).index;
                }
            } else {
                orderForViewToBeAdded.index = childCount;
            }
        } else {
            orderForViewToBeAdded.index = childCount;
        }

        orders.add(orderForViewToBeAdded);
        return this.sortOrdersIntoReorderedIndices(childCount + 1, orders);
    }

    private int[] createReorderedIndices() {
        int childCount = this.getChildCount();
        List<Order> orders = this.createOrders(childCount);
        return this.sortOrdersIntoReorderedIndices(childCount, orders);
    }

    private int[] sortOrdersIntoReorderedIndices(int childCount, List<Order> orders) {
        Collections.sort(orders);
        if (this.mOrderCache == null) {
            this.mOrderCache = new SparseIntArray(childCount);
        }

        this.mOrderCache.clear();
        int[] reorderedIndices = new int[childCount];
        int i = 0;

        for (Iterator var5 = orders.iterator(); var5.hasNext(); ++i) {
            Order order = (Order) var5.next();
            reorderedIndices[i] = order.index;
            this.mOrderCache.append(i, order.order);
        }

        return reorderedIndices;
    }

    @NonNull
    private List<Order> createOrders(int childCount) {
        List<Order> orders = new ArrayList();

        for (int i = 0; i < childCount; ++i) {
            View child = this.getChildAt(i);
            FlexboxLayout.LayoutParams params = (FlexboxLayout.LayoutParams) child.getLayoutParams();
            Order order = new FlexboxLayout.Order();
            order.order = params.order;
            order.index = i;
            orders.add(order);
        }

        return orders;
    }

    /**
     * 最后一次测量后顺序是否改变
     *
     * @return
     */
    private boolean isOrderChangedFromLastMeasurement() {
        int childCount = this.getChildCount();
        if (this.mOrderCache == null) {
            this.mOrderCache = new SparseIntArray(childCount);
        }

        if (this.mOrderCache.size() != childCount) {
            return true;
        } else {
            for (int i = 0; i < childCount; ++i) {
                View view = this.getChildAt(i);
                if (view != null) {
                    FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) view.getLayoutParams();
                    if (lp.order != this.mOrderCache.get(i)) {
                        return true;
                    }
                }
            }
            return false;
        }
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
            View child = this.getReorderedChildAt(i);
            if (child == null) {
                this.addFlexLineIfLastFlexItem(i, childCount, flexLine);
            } else if (child.getVisibility() == View.GONE) {
                ++flexLine.itemCount;
                this.addFlexLineIfLastFlexItem(i, childCount, flexLine);
            } else {
                FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) child.getLayoutParams();
                if (lp.alignSelf == 4) {
                    flexLine.indicesAlignSelfStretch.add(i);
                }

                int childWidth = lp.width;
                if (lp.flexBasisPercent != -1.0F && widthMode == 1073741824) {
                    childWidth = Math.round((float) widthSize * lp.flexBasisPercent);
                }

                int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, this.getPaddingLeft() + this.getPaddingRight() + lp.leftMargin + lp.rightMargin, childWidth);
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                this.checkSizeConstraints(child);
                childState = ViewCompat.combineMeasuredStates(childState, ViewCompat.getMeasuredState(child));
                largestHeightInRow = Math.max(largestHeightInRow, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                if (limitedLine <= 0 || mFlexLines.size() < getLimitedLine() ) {
                    if (this.isWrapRequired(widthMode, widthSize, flexLine.mainSize, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin)) {
                        this.mFlexLines.add(flexLine);
                        flexLine = new FlexboxLayout.FlexLine();
                        flexLine.itemCount = 1;
                        flexLine.mainSize = paddingStart + paddingEnd;
                        largestHeightInRow = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                    } else {
                        ++flexLine.itemCount;
                    }
                    flexLine.mainSize += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                    flexLine.totalFlexGrow += lp.flexGrow;
                    flexLine.totalFlexShrink += lp.flexShrink;
                    flexLine.crossSize = Math.max(flexLine.crossSize, largestHeightInRow);
                    flexLine.maxBaseline = Math.max(flexLine.maxBaseline, child.getBaseline() + lp.topMargin);

                    this.addFlexLineIfLastFlexItem(i, childCount, flexLine);
                }
            }
        }

        this.determineMainSize(widthMeasureSpec);

        this.determineCrossSize(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom());
        this.stretchViews();
        this.setMeasuredDimensionForFlex(widthMeasureSpec, heightMeasureSpec, childState);
    }

    private void checkSizeConstraints(View view) {
        boolean needsMeasure = false;
        FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) view.getLayoutParams();
        int childWidth = view.getMeasuredWidth();
        int childHeight = view.getMeasuredHeight();
        if (view.getMeasuredWidth() < lp.minWidth) {
            needsMeasure = true;
            childWidth = lp.minWidth;
        } else if (view.getMeasuredWidth() > lp.maxWidth) {
            needsMeasure = true;
            childWidth = lp.maxWidth;
        }

        if (childHeight < lp.minHeight) {
            needsMeasure = true;
            childHeight = lp.minHeight;
        } else if (childHeight > lp.maxHeight) {
            needsMeasure = true;
            childHeight = lp.maxHeight;
        }

        if (needsMeasure) {
            view.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
        }

    }

    private void addFlexLineIfLastFlexItem(int childIndex, int childCount, FlexboxLayout.FlexLine flexLine) {
        if (childIndex == childCount - 1 && flexLine.itemCount != 0) {
            this.mFlexLines.add(flexLine);
        }

    }

    private void determineMainSize(int widthMeasureSpec) {
        int mainSize;
        int paddingAlongMainAxis;
        int childIndex;
        childIndex = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (childIndex == MeasureSpec.EXACTLY) {
            mainSize = widthSize;
        } else {
            mainSize = this.getLargestMainSize();
        }
        paddingAlongMainAxis = this.getPaddingLeft() + this.getPaddingRight();
        childIndex = 0;
        Iterator var10 = this.mFlexLines.iterator();

        while (var10.hasNext()) {
            FlexboxLayout.FlexLine flexLine = (FlexboxLayout.FlexLine) var10.next();
            if (flexLine.mainSize < mainSize) {
                childIndex = this.expandFlexItems(flexLine, mainSize, paddingAlongMainAxis, childIndex);
            } else {
                childIndex = this.shrinkFlexItems(flexLine, mainSize, paddingAlongMainAxis, childIndex);
            }
        }

    }

    private int expandFlexItems(FlexLine flexLine, int maxMainSize, int paddingAlongMainAxis, int startIndex) {
        int childIndex = startIndex;
        if (flexLine.totalFlexGrow > 0.0F && maxMainSize >= flexLine.mainSize) {
            int sizeBeforeExpand = flexLine.mainSize;
            boolean needsReexpand = false;
            float unitSpace = (float) (maxMainSize - flexLine.mainSize) / flexLine.totalFlexGrow;
            flexLine.mainSize = paddingAlongMainAxis;
            float accumulatedRoundError = 0.0F;

            for (int i = 0; i < flexLine.itemCount; ++i) {
                View child = this.getReorderedChildAt(childIndex);
                if (child != null) {
                    if (child.getVisibility() == View.GONE) {
                        ++childIndex;
                    } else {
                        FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) child.getLayoutParams();
                        float rawCalculatedWidth;
                        int newWidth;
                        if (!this.mChildrenFrozen[childIndex]) {
                            rawCalculatedWidth = (float) child.getMeasuredWidth() + unitSpace * lp.flexGrow;
                            if (i == flexLine.itemCount - 1) {
                                rawCalculatedWidth += accumulatedRoundError;
                                accumulatedRoundError = 0.0F;
                            }
                            newWidth = Math.round(rawCalculatedWidth);
                            if (newWidth > lp.maxWidth) {
                                needsReexpand = true;
                                newWidth = lp.maxWidth;
                                this.mChildrenFrozen[childIndex] = true;
                                flexLine.totalFlexGrow -= lp.flexGrow;
                            } else {
                                accumulatedRoundError += rawCalculatedWidth - (float) newWidth;
                                if ((double) accumulatedRoundError > 1.0D) {
                                    ++newWidth;
                                    accumulatedRoundError = (float) ((double) accumulatedRoundError - 1.0D);
                                } else if ((double) accumulatedRoundError < -1.0D) {
                                    --newWidth;
                                    accumulatedRoundError = (float) ((double) accumulatedRoundError + 1.0D);
                                }
                            }
                            child.measure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY));
                        }
                        flexLine.mainSize += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                        ++childIndex;
                    }
                }
            }

            if (needsReexpand && sizeBeforeExpand != flexLine.mainSize) {
                this.expandFlexItems(flexLine, maxMainSize, paddingAlongMainAxis, startIndex);
            }

            return childIndex;
        } else {
            childIndex = startIndex + flexLine.itemCount;
            return childIndex;
        }
    }

    private int shrinkFlexItems(FlexLine flexLine, int maxMainSize, int paddingAlongMainAxis, int startIndex) {
        int childIndex = startIndex;
        int sizeBeforeShrink = flexLine.mainSize;
        if (flexLine.totalFlexShrink > 0.0F && maxMainSize <= flexLine.mainSize) {
            boolean needsReshrink = false;
            float unitShrink = (float) (flexLine.mainSize - maxMainSize) / flexLine.totalFlexShrink;
            float accumulatedRoundError = 0.0F;
            flexLine.mainSize = paddingAlongMainAxis;

            for (int i = 0; i < flexLine.itemCount; ++i) {
                View child = this.getReorderedChildAt(childIndex);
                if (child != null) {
                    if (child.getVisibility() == View.GONE) {
                        ++childIndex;
                    } else {
                        FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) child.getLayoutParams();
                        float rawCalculatedWidth;
                        int newWidth;
                        if (!this.mChildrenFrozen[childIndex]) {
                            rawCalculatedWidth = (float) child.getMeasuredWidth() - unitShrink * lp.flexShrink;
                            if (i == flexLine.itemCount - 1) {
                                rawCalculatedWidth += accumulatedRoundError;
                                accumulatedRoundError = 0.0F;
                            }

                            newWidth = Math.round(rawCalculatedWidth);
                            if (newWidth < lp.minWidth) {
                                needsReshrink = true;
                                newWidth = lp.minWidth;
                                this.mChildrenFrozen[childIndex] = true;
                                flexLine.totalFlexShrink -= lp.flexShrink;
                            } else {
                                accumulatedRoundError += rawCalculatedWidth - (float) newWidth;
                                if ((double) accumulatedRoundError > 1.0D) {
                                    ++newWidth;
                                    --accumulatedRoundError;
                                } else if ((double) accumulatedRoundError < -1.0D) {
                                    --newWidth;
                                    ++accumulatedRoundError;
                                }
                            }

                            child.measure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY));
                        }

                        flexLine.mainSize += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

                        ++childIndex;
                    }
                }
            }

            if (needsReshrink && sizeBeforeShrink != flexLine.mainSize) {
                this.shrinkFlexItems(flexLine, maxMainSize, paddingAlongMainAxis, startIndex);
            }

            return childIndex;
        } else {
            childIndex = startIndex + flexLine.itemCount;
            return childIndex;
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
                view = this.getReorderedChildAt(viewIndex);
                FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) view.getLayoutParams();
                if (lp.alignSelf == -1 || lp.alignSelf == 4) {
                    this.stretchViewVertically(view, flexLine.crossSize);
                }

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
                View child = this.getReorderedChildAt(currentViewIndex);
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

    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof FlexboxLayout.LayoutParams;
    }

    public FlexboxLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FlexboxLayout.LayoutParams(this.getContext(), attrs);
    }

    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new FlexboxLayout.LayoutParams(p);
    }

    private static class FlexLine {
        int mainSize;
        int crossSize;
        int itemCount;
        float totalFlexGrow;
        float totalFlexShrink;
        int maxBaseline;
        List<Integer> indicesAlignSelfStretch;

        private FlexLine() {
            this.indicesAlignSelfStretch = new ArrayList();
        }
    }

    private static class Order implements Comparable<Order> {
        int index;
        int order;

        private Order() {
        }

        public int compareTo(@NonNull FlexboxLayout.Order another) {
            return this.order != another.order ? this.order - another.order : this.index - another.index;
        }

        public String toString() {
            return "Order{order=" + this.order + ", index=" + this.index + '}';
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        private static final int ORDER_DEFAULT = 1;
        private static final float FLEX_GROW_DEFAULT = 0.0F;
        private static final float FLEX_SHRINK_DEFAULT = 1.0F;
        public static final float FLEX_BASIS_PERCENT_DEFAULT = -1.0F;
        public static final int ALIGN_SELF_AUTO = -1;
        public static final int ALIGN_SELF_FLEX_START = 0;
        public static final int ALIGN_SELF_FLEX_END = 1;
        public static final int ALIGN_SELF_CENTER = 2;
        public static final int ALIGN_SELF_BASELINE = 3;
        public static final int ALIGN_SELF_STRETCH = 4;
        private static final int MAX_SIZE = 16777215;
        public int order = 1;
        public float flexGrow = 0.0F;
        public float flexShrink = 1.0F;
        public int alignSelf = -1;
        public float flexBasisPercent = -1.0F;
        public int minWidth;
        public int minHeight;
        public int maxWidth = 16777215;
        public int maxHeight = 16777215;
        public boolean wrapBefore;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, styleable.FlexboxLayout_Layout);
            this.order = a.getInt(styleable.FlexboxLayout_Layout_layout_order, 1);
            this.flexGrow = a.getFloat(styleable.FlexboxLayout_Layout_layout_flexGrow, 0.0F);
            this.flexShrink = a.getFloat(styleable.FlexboxLayout_Layout_layout_flexShrink, 1.0F);
            this.alignSelf = a.getInt(styleable.FlexboxLayout_Layout_layout_alignSelf, -1);
            this.flexBasisPercent = a.getFraction(styleable.FlexboxLayout_Layout_layout_flexBasisPercent, 1, 1, -1.0F);
            this.minWidth = a.getDimensionPixelSize(styleable.FlexboxLayout_Layout_layout_minWidth, 0);
            this.minHeight = a.getDimensionPixelSize(styleable.FlexboxLayout_Layout_layout_minHeight, 0);
            this.maxWidth = a.getDimensionPixelSize(styleable.FlexboxLayout_Layout_layout_maxWidth, 16777215);
            this.maxHeight = a.getDimensionPixelSize(styleable.FlexboxLayout_Layout_layout_maxHeight, 16777215);
            this.wrapBefore = a.getBoolean(styleable.FlexboxLayout_Layout_layout_wrapBefore, false);
            a.recycle();
        }

        public LayoutParams(FlexboxLayout.LayoutParams source) {
            super(source);
            this.order = source.order;
            this.flexGrow = source.flexGrow;
            this.flexShrink = source.flexShrink;
            this.alignSelf = source.alignSelf;
            this.flexBasisPercent = source.flexBasisPercent;
            this.minWidth = source.minWidth;
            this.minHeight = source.minHeight;
            this.maxWidth = source.maxWidth;
            this.maxHeight = source.maxHeight;
            this.wrapBefore = source.wrapBefore;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(int width, int height) {
            super(new ViewGroup.LayoutParams(width, height));
        }
    }
}
