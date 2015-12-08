package cn.lingox.android.utils;

import android.app.Activity;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;

/**
 * 创建体验的标签控件
 */
public class CreateTagView {
    /**
     * 标签之间的间距 px
     */
    private static final int itemMargins = 25;
    /**
     * 标签的行间距 px
     */
    private static final int lineMargins = 25;

    /**
     * 添加标签
     *
     * @param tags     标签集合
     * @param tagsView 显示控件
     * @param context  上下文
     */
    public static void addTagView(ArrayList<String> tags, ViewGroup tagsView, Activity context) {
        int width = LingoXApplication.getInstance().getWidth();

        tagsView.removeAllViews();
        int containerWidth = width - DpToPx.dip2px(context, 100);
        LayoutInflater inflater = context.getLayoutInflater();
        /** 用来测量字符的宽度 */
        Paint paint = new Paint();
        TextView textView = (TextView) inflater.inflate(R.layout.row_tag_include, null);
        int itemPadding = textView.getCompoundPaddingLeft() + textView.getCompoundPaddingRight();
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(0, 0, itemMargins, 0);
        paint.setTextSize(textView.getTextSize());
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        tagsView.addView(layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, lineMargins, 0, 0);
        /** 一行剩下的空间 **/
        int remainWidth = containerWidth;
        // 表示数组长度
        int length = tags.size();
        String text;
        float itemWidth;
        for (int i = 0; i < length; ++i) {
            text = tags.get(i);

            itemWidth = paint.measureText(text) + itemPadding;
            if (remainWidth - itemWidth > 25) {
                addItemView(inflater, layout, tvParams, text);
            } else {
                resetTextViewMarginsRight(layout);
                layout = new LinearLayout(context);
                layout.setLayoutParams(params);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                /** 将前面那一个textview加入新的一行 */
                addItemView(inflater, layout, tvParams, text);
                tagsView.addView(layout);
                remainWidth = containerWidth;
            }
            remainWidth = (int) (remainWidth - itemWidth + 0.5f) - itemMargins;
        }
        if (length > 0) {
            resetTextViewMarginsRight(layout);
        }
    }

    /*****************
     * 将每行最后一个textview的MarginsRight去掉
     *********************************/
    private static void resetTextViewMarginsRight(ViewGroup viewGroup) {
        final TextView tempTextView = (TextView) viewGroup.getChildAt(viewGroup.getChildCount() - 1);
        tempTextView.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private static void addItemView(LayoutInflater inflater, ViewGroup viewGroup, ViewGroup.LayoutParams params, final String text) {
        final TextView tvItem = (TextView) inflater.inflate(R.layout.row_tag_include, null);
        tvItem.setText(text);
        viewGroup.addView(tvItem, params);
    }
}