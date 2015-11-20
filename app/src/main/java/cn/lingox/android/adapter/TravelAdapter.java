package cn.lingox.android.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.utils.CircularImageView;

public class TravelAdapter extends BaseAdapter {
    private Activity context;
    private ArrayList<TravelEntity> datas;

    public TravelAdapter(Activity context, ArrayList<TravelEntity> list) {
        this.context = context;
        datas = new ArrayList<>();
        datas.addAll(list);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_travel, parent, false);
            holder = new ViewHolder();

            holder.avatar = (CircularImageView) convertView.findViewById(R.id.travel_avatar);
            holder.flg = (ImageView) convertView.findViewById(R.id.travel_country_flg);
            holder.name = (TextView) convertView.findViewById(R.id.travel_user_name);
            holder.location = (TextView) convertView.findViewById(R.id.travel_user_name);
            holder.describe = (TextView) convertView.findViewById(R.id.travel_describe);
            holder.tag = (TextView) convertView.findViewById(R.id.travel_tag);
            holder.time = (TextView) convertView.findViewById(R.id.travel_time);
            holder.replyNum = (TextView) convertView.findViewById(R.id.travel_reply_num);
            holder.likeNum = (TextView) convertView.findViewById(R.id.travel_like_num);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TravelEntity travelEntity = datas.get(position);
//        UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context,holder.avatar,"url","circular");
        holder.describe.setText(travelEntity.getDescribe());
        holder.location.setText(travelEntity.getTraveling());
        return convertView;
    }

    static class ViewHolder {
        ImageView flg;
        CircularImageView avatar;
        TextView name, location, describe, tag, time, replyNum, likeNum;
    }
}
