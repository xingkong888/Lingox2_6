package cn.lingox.android.adapter;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ImageHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.TimeHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.GetUser;
import cn.lingox.android.utils.CircularImageView;

/**
 * Travel的适配器
 */
public class TravelAdapter extends BaseAdapter {
    private Activity context;
    private ArrayList<TravelEntity> datas;

    //标识listview是否滑动 true滑动  false未滑动
    private boolean loading = false;

    public TravelAdapter(Activity context, ArrayList<TravelEntity> list) {
        this.context = context;
        datas = list;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_travel, null, false);
            holder = new ViewHolder();

            holder.avatar = (CircularImageView) convertView.findViewById(R.id.travel_avatar);
            holder.flg = (ImageView) convertView.findViewById(R.id.travel_country_flg);
            holder.name = (TextView) convertView.findViewById(R.id.travel_user_name);
//            holder.location = (TextView) convertView.findViewById(R.id.travel_location);
            holder.describe = (TextView) convertView.findViewById(R.id.travel_describe);
            holder.createTime = (TextView) convertView.findViewById(R.id.travel_create_time);
            holder.commentNum = (TextView) convertView.findViewById(R.id.travel_comment_num);
            holder.likeNum = (TextView) convertView.findViewById(R.id.travel_like_num);
            holder.time = (TextView) convertView.findViewById(R.id.travel_time);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        TravelEntity travelEntity = datas.get(position);
        if (!loading) {
            User user = CacheHelper.getInstance().getUserInfo(travelEntity.getUser_id());
            if (user == null) {
                new GetUser(travelEntity.getUser_id(), new GetUser.Callback() {
                    @Override
                    public void onSuccess(User user) {
                        //头像
                        UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar, user.getAvatar(), "circular");
                        //名字
                        holder.name.setText(user.getNicknameOrUsername());
                        //国旗
                        ImageHelper.getInstance().loadFlag(holder.flg, JsonHelper.getInstance().getCodeFromCountry(user.getCountry()), 2);
                    }

                    @Override
                    public void onFail() {

                    }
                }).execute();
            } else {
                //头像
                UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar, user.getAvatar(), "circular");
                //名字
                holder.name.setText(user.getNicknameOrUsername());
                //国旗
                ImageHelper.getInstance().loadFlag(holder.flg, JsonHelper.getInstance().getCodeFromCountry(user.getCountry()), 2);
            }
        } else {
            holder.avatar.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar));
        }
        //显示时间段
        String startTime = TimeHelper.getInstance().parseTimestampToDate(travelEntity.getStartTime());
        String endTime = TimeHelper.getInstance().parseTimestampToDate(travelEntity.getEndTime());
        holder.time.setText(new StringBuilder().append(startTime).append(" ~ ").append(endTime));
        //问题
        holder.describe.setText(travelEntity.getText());
//        //显示省份
//        holder.location.setText(travelEntity.getProvince().isEmpty() ? travelEntity.getCountry() : travelEntity.getProvince());
        //like的人数
        holder.likeNum.setText(String.valueOf(travelEntity.getLikeUsers().size()));
        //comment的人数
        holder.commentNum.setText(String.valueOf(travelEntity.getComments().size()));
        //发布时间距离当前时间
        holder.createTime.setText(
                TimeHelper.getInstance().parseTimestampToDate(travelEntity.getCreatedAt(), "TravelEntity"));
        return convertView;
    }

    /**
     * 设置listview是否在滑动
     *
     * @param loading true滑动 false空闲
     */
    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    static class ViewHolder {
        ImageView flg;
        CircularImageView avatar;
        TextView name, describe, time, createTime, commentNum, likeNum;
//        location 去掉了
    }
}
