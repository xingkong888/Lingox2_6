package cn.lingox.android.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.activity.MainActivity;

/**
 * Created by wangxinxing on 2016/1/18.
 * 自定义的spinner的适配器
 */
public class MySpinnerAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> list;


    public MySpinnerAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.myspinner_item, null);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.my_spinner_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (position == MainActivity.getObj().getClickPosition()) {
            //字体设为主色，背景加深
            viewHolder.textView.setTextColor(context.getResources().getColor(R.color.main_color));
            viewHolder.textView.setBackgroundColor(Color.argb(178, 0, 0, 0));
        } else {
            //字体白色，背景不变
            viewHolder.textView.setTextColor(Color.WHITE);
            viewHolder.textView.setBackgroundColor(Color.argb(140, 0, 0, 0));
        }
        viewHolder.textView.setText(list.get(position));
        viewHolder.textView.setTag(position);
        return convertView;
    }

    static class ViewHolder {
        TextView textView;
    }
}
