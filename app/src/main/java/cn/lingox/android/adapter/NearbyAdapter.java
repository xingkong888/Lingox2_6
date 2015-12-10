package cn.lingox.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.lingox.android.R;
import cn.lingox.android.activity.NearByFragment;
import cn.lingox.android.activity.UserInfoActivity;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Reference;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.ImageHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.LoadUserReferences;
import cn.lingox.android.utils.SkipDialog;

/**
 * 附近人的适配器
 */
public class NearbyAdapter extends BaseAdapter {
    //线程池
    private ExecutorService pool = Executors.newFixedThreadPool(5);
    private Activity context;
    private ArrayList<User> datas;

    public NearbyAdapter(final Activity context, ArrayList<User> list) {
        this.context = context;
        this.datas = list;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.row_search_include, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.nearby_name);
            holder.tag1 = (TextView) convertView.findViewById(R.id.nearby_tag1);
            holder.tag2 = (TextView) convertView.findViewById(R.id.nearby_tag2);
            holder.tag3 = (TextView) convertView.findViewById(R.id.nearby_tag3);
            holder.line = (TextView) convertView.findViewById(R.id.nearby_line);
            holder.commentNum = (TextView) convertView.findViewById(R.id.nearby_comment_);
            holder.comment = (TextView) convertView.findViewById(R.id.nearby_comment);
            holder.speak_ = (TextView) convertView.findViewById(R.id.nearby_speak_);
            holder.speak = (TextView) convertView.findViewById(R.id.nearby_speak);
            holder.countryAndCity = (TextView) convertView.findViewById(R.id.nearby_country);
            holder.point = (ImageView) convertView.findViewById(R.id.nearby_point);
            holder.flag = (ImageView) convertView.findViewById(R.id.nearby_countryImg);
            holder.avatar = (ImageView) convertView.findViewById(R.id.nearby_avatar);
            holder.lalala = (TextView) convertView.findViewById(R.id.asdfasdfasdf);
            holder.info = (LinearLayout) convertView.findViewById(R.id.abcdefg);
            holder.info2 = (LinearLayout) convertView.findViewById(R.id.mnbvcxz);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final User user = datas.get(position);
        if (position == (datas.size() - 1)) {
            holder.lalala.setVisibility(View.GONE);
        } else {
            holder.lalala.setVisibility(View.VISIBLE);
        }
        holder.avatar.setTag(user.getAvatar());
        UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar, user.getAvatar(), "crop");
        ImageHelper.getInstance().loadFlag(holder.flag, JsonHelper.getInstance().getCodeFromCountry(
                user.getCountry()
        ), 1);
        holder.name.setText(user.getNickname());
        //加载用户评论数
        new LoadUserReferences(user.getId(), new LoadUserReferences.Callback() {
            @Override
            public void onSuccess(ArrayList<Reference> list) {
                holder.comment.setText(String.valueOf(list.size()));
            }

            @Override
            public void onFail() {
                Toast.makeText(context, context.getString(R.string.fail_get_reference), Toast.LENGTH_LONG).show();
            }
        }).executeOnExecutor(pool);
        //判断语言是否为空
        if (!TextUtils.isEmpty(user.getSpeak())) {
            //不为空
            holder.speak_.setVisibility(View.VISIBLE);
            holder.speak.setVisibility(View.VISIBLE);
            holder.point.setVisibility(View.VISIBLE);
            if (user.getSpeak().length() > 10) {
                String str = user.getSpeak().substring(0, 7) + "...";
                holder.speak.setText(str);
            } else {
                holder.speak.setText(user.getSpeak());
            }
        } else {
            holder.speak_.setVisibility(View.GONE);
            holder.speak.setVisibility(View.GONE);
            holder.point.setVisibility(View.GONE);
        }
        //判断国家是否为空
        String str = user.getLocation();
        if (!str.isEmpty()) {
            holder.countryAndCity.setVisibility(View.VISIBLE);
            holder.countryAndCity.setText(String.format(context.getString(R.string.from), str));
            //设置分隔线长度
            holder.countryAndCity.measure(0, 0);
            holder.line.setWidth(holder.countryAndCity.getMeasuredWidth());
        } else {
            //设置分隔线长度
            holder.name.measure(0, 0);
            holder.line.setWidth(holder.name.getMeasuredWidth());
            holder.countryAndCity.setVisibility(View.GONE);
        }

        if (user.getLocalGuidey()) {
            holder.tag2.setVisibility(View.VISIBLE);
        } else {
            holder.tag2.setVisibility(View.GONE);
        }
        if (user.getHomeMeal()) {
            holder.tag1.setVisibility(View.VISIBLE);
        } else {
            holder.tag1.setVisibility(View.GONE);
        }
        if (user.getHomeStay()) {
            holder.tag3.setVisibility(View.VISIBLE);
        } else {
            holder.tag3.setVisibility(View.GONE);
        }
        holder.info.setVisibility(View.VISIBLE);
        holder.info2.setVisibility(View.VISIBLE);
        //给控件设置点击监听器-----点击整个item都有响应
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LingoXApplication.getInstance().getSkip()) {
                    SkipDialog.getDialog(context).show();
                } else {
                    MobclickAgent.onEvent(context, "click_members");

                    Intent intent = new Intent(context, UserInfoActivity.class);
                    intent.putExtra(UserInfoActivity.INTENT_USER_ID, user.getId());
                    context.startActivityForResult(intent, NearByFragment.VIEW_USER);
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView tag1, tag2, tag3, line, commentNum, comment, speak_, speak, countryAndCity, name, lalala;
        ImageView point, flag, avatar;
        LinearLayout info, info2;
    }
}
