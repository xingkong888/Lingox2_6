package cn.lingox.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.R;

/**
 * 选择区域
 */
public class ChooseAreaAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> datas;
    private boolean isEmpty = true;

    public ChooseAreaAdapter(Context context, ArrayList<String> datas, boolean isEmpty) {
        this.context = context;
        this.datas = datas;
        this.isEmpty = isEmpty;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && isEmpty) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        ViewHolder1 viewHolder1 = null;
        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                case 0:
                    convertView = LayoutInflater.from(context).inflate(R.layout.text_view, null);
                    viewHolder1 = new ViewHolder1();
                    viewHolder1.headItem = (TextView) convertView.findViewById(R.id.item);
                    convertView.setTag(viewHolder1);
                    break;
                case 1:
                    convertView = LayoutInflater.from(context).inflate(R.layout.textview, null);
                    viewHolder = new ViewHolder();
                    viewHolder.item = (TextView) convertView.findViewById(R.id.item);
                    convertView.setTag(viewHolder);
                    break;
            }
        } else {
            switch (type) {
                case 0:
                    viewHolder1 = (ViewHolder1) convertView.getTag();
                    break;
                case 1:
                    viewHolder = (ViewHolder) convertView.getTag();
                    break;
            }
        }
        switch (type) {
            case 0:
                viewHolder1.headItem.setText(datas.get(position));
                break;
            case 1:
                viewHolder.item.setText(datas.get(position));
                break;
        }
        return convertView;
    }

    public boolean getIsEmpty() {
        return isEmpty;
    }

    public void setIsEmpty(boolean isEmpty1) {
        isEmpty = isEmpty1;
    }

    static class ViewHolder {
        TextView item;
    }

    static class ViewHolder1 {
        TextView headItem;
    }
}
