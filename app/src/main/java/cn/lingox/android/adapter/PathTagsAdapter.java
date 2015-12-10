package cn.lingox.android.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.entity.PathTags;

/**
 * 发布体验时的标签的适配器
 */
public class PathTagsAdapter extends BaseAdapter {
    private ArrayList<PathTags> datas;
    private Context context;
    private int type;

    public PathTagsAdapter(Context context, ArrayList<PathTags> datas, int type) {
        this.datas = datas;
        this.context = context;
        this.type = type;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.row_path_edit_3_item, null);
            viewHolder.box = (CheckBox) convertView.findViewById(R.id.path_edit_item);
            switch (type) {
                case 0://创建页面
                    viewHolder.box.setTextColor(Color.WHITE);
                    break;
                case 1://搜索页面
                    viewHolder.box.setTextColor(Color.BLACK);
                    break;
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        PathTags tag = datas.get(position);
        viewHolder.box.setText(tag.getTag());
        if (tag.getType() == 1) {
            viewHolder.box.setChecked(true);
        } else {
            viewHolder.box.setChecked(false);
        }
        return convertView;
    }

    static class ViewHolder {
        CheckBox box;
    }
}