package cn.lingox.android.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by wangxinxing on 2015/12/1.
 * 自定义viewPager，可实现自定义是否左右滑动
 */
public class MyViewPager extends ViewPager {
    private boolean isScrollable = true;

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置属性
     *
     * @param isScrollable false不可滑动，true可滑动
     */
    public void setScrollable(boolean isScrollable) {
        this.isScrollable = isScrollable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        //false  不能左右滑动
        return isScrollable && super.onInterceptTouchEvent(arg0);
    }
}
