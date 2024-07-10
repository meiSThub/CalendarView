/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/MiracleTimes-Dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haibin.calendarview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 年份布局选择View
 */
public final class YearRecyclerView extends RecyclerView {
    private static final String TAG = "YearRecyclerView";
    private CalendarViewDelegate mDelegate;
    private YearViewAdapter mAdapter;
    private OnMonthSelectedListener mListener;
    private Map<Integer, List<Integer>> mDateMonthMap;
    private int year;

    public YearRecyclerView(Context context) {
        this(context, null);
        try {
            Field field = Class.forName("com.mega.carlog.document.DataLoader").getField("DATE_MONTH_MAP");
            field.setAccessible(true);
            this.mDateMonthMap = (Map) field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public YearRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mAdapter = new YearViewAdapter(context);
        setLayoutManager(new GridLayoutManager(context, 4));
        setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, long itemId) {
                if (mListener != null && mDelegate != null) {
                    Month month = mAdapter.getItem(position);
                    if (month == null) {
                        return;
                    }
                    if (!CalendarUtil.isMonthInRange(month.getYear(), month.getMonth(),
                            mDelegate.getMinYear(), mDelegate.getMinYearMonth(),
                            mDelegate.getMaxYear(), mDelegate.getMaxYearMonth())) {
                        return;
                    }
                    mListener.onMonthSelected(month.getYear(), month.getMonth());
                    if (mDelegate.mYearViewChangeListener != null) {
                        mDelegate.mYearViewChangeListener.onYearViewChange(true);
                    }
                }
            }
        });
    }

    /**
     * 设置
     *
     * @param delegate delegate
     */
    final void setup(CalendarViewDelegate delegate) {
        this.mDelegate = delegate;
        this.mAdapter.setup(delegate);
    }

    /**
     * 初始化年视图
     *
     * @param year year
     */
    final void init(int year) {
        this.year = year;
        Calendar selectedCalendar = this.mDelegate.getSelectedMonth();
        int selectedYear = selectedCalendar.getYear();
        int selectedMonth = selectedCalendar.getMonth();
        java.util.Calendar date = java.util.Calendar.getInstance();
        for (int i = 1; i <= 12; i++) {
            date.set(year, i - 1, 1);
            int mDaysCount = CalendarUtil.getMonthDaysCount(year, i);
            Month month = new Month();
            month.setDiff(CalendarUtil.getMonthViewStartDiff(year, i, mDelegate.getWeekStart()));
            month.setCount(mDaysCount);
            month.setMonth(i);
            month.setYear(year);
            month.setHasData(hasData(year, i));
            month.setSelected(year == selectedYear && i == selectedMonth);
            mAdapter.addItem(month);
        }
    }

    /**
     * 更新选中的月份
     */
    public void update() {
        List<Month> items = this.mAdapter.getItems();
        Calendar selectedCalendar = this.mDelegate.getSelectedMonth();
        int selectedYear = selectedCalendar.getYear();
        int selectedMonth = selectedCalendar.getMonth();
        for (int i = 0; i < items.size(); i++) {
            Month month = items.get(i);
            month.setSelected(month.getYear() == selectedYear && month.getMonth() == selectedMonth);
        }
        notifyAdapterDataSetChanged();
    }

    /**
     * 指定月份是否有数据
     */
    private boolean hasData(int year, int month) {
        List<Integer> monthList;
        if (this.mDateMonthMap != null && this.mDateMonthMap.containsKey(Integer.valueOf(year)) && (monthList = this.mDateMonthMap.get(Integer.valueOf(year))) != null && monthList.contains(Integer.valueOf(month))) {
            return true;
        }
        return false;
    }

    /**
     * 更新周起始
     */
    final void updateWeekStart() {
        for (Month month : mAdapter.getItems()) {
            month.setDiff(CalendarUtil.getMonthViewStartDiff(month.getYear(), month.getMonth(), mDelegate.getWeekStart()));
        }
    }

    /**
     * 更新字体颜色大小
     */
    final void updateStyle() {
        for (int i = 0; i < getChildCount(); i++) {
            YearView view = (YearView) getChildAt(i);
            view.updateStyle();
            view.invalidate();
        }
    }

    /**
     * 月份选择事件
     *
     * @param listener listener
     */
    final void setOnMonthSelectedListener(OnMonthSelectedListener listener) {
        this.mListener = listener;
    }


    void notifyAdapterDataSetChanged() {
        if (getAdapter() == null) {
            return;
        }
        getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int height = MeasureSpec.getSize(heightSpec);
        int width = MeasureSpec.getSize(widthSpec);
        Log.i(TAG, "onMeasure: width=" + width + ";height=" + height);
        int itemWidth = (width - (mDelegate.getYearViewPaddingLeft() + mDelegate.getYearViewPaddingRight())) / 4
                - (mDelegate.mYearViewMonthItemMarginLeft + mDelegate.mYearViewMonthItemMarginRight);
        int itemHeight = height / 3 - mDelegate.mYearViewMonthItemMarginTop - mDelegate.mYearViewMonthItemMarginBottom;
        mAdapter.setYearViewSize(itemWidth, itemHeight);
        // mAdapter.setYearViewSize(120, 68);
    }

    interface OnMonthSelectedListener {
        void onMonthSelected(int year, int month);
    }
}
