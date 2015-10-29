package cn.lingox.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
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
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.ImageHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.utils.FileUtil;
import cn.lingox.android.utils.ImageCache;
import cn.lingox.android.utils.SkipDialog;

public class NearbyAdapter extends BaseAdapter {
    //线程池
    private ExecutorService pool = Executors.newFixedThreadPool(5);
    private Activity context;
    private ArrayList<User> datas;

    private boolean isFling = false;

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
        if (!isFling) {
            final User user = datas.get(position);
            if (position == (datas.size() - 1)) {
                holder.lalala.setVisibility(View.GONE);
            } else {
                holder.lalala.setVisibility(View.VISIBLE);
            }
            holder.avatar.setTag(user.getAvatar());
            if (FileUtil.getImgName(user.getAvatar()).contentEquals("avatar.jpg")) {
                UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(holder.avatar, user.getAvatar());
            } else {
                if (ImageCache.getInstance().get(user.getAvatar()) != null && holder.avatar.getTag().equals(user.getAvatar())) {
                    holder.avatar.setImageBitmap(ImageCache.getInstance().get(user.getAvatar()));
                } else {
                    if (FileUtil.getImg(user.getAvatar()) == null && holder.avatar.getTag().equals(user.getAvatar())) {
                        UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(holder.avatar, user.getAvatar());
                    } else if (holder.avatar.getTag().equals(user.getAvatar())) {
                        holder.avatar.setImageBitmap(FileUtil.getImg(user.getAvatar()));
                    } else {
                        holder.avatar.setImageResource(R.drawable.nearby_nopic_294dp);
                    }
                }
            }
//            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar, user.getAvatar());
            ImageHelper.getInstance().loadFlag(holder.flag, JsonHelper.getInstance().getCodeFromCountry(
                    user.getCountry()
            ), 1);
            holder.name.setText(user.getNickname());
            //加载用户评论数
            new LoadUserReferences(holder.comment).executeOnExecutor(pool, user);
            //判断语言是否为空
            if (!TextUtils.isEmpty(user.getSpeak())) {
                //不为空
                holder.speak_.setVisibility(View.VISIBLE);
                holder.speak.setVisibility(View.VISIBLE);
                holder.point.setVisibility(View.VISIBLE);
                if (user.getSpeak().length() > 17) {
                    String str = user.getSpeak().substring(0, 12) + "...";
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
                holder.countryAndCity.setText(context.getString(R.string.from) + " " + str);
            } else {
                holder.countryAndCity.setVisibility(View.GONE);
            }
            //设置分隔线长度
            holder.countryAndCity.measure(0, 0);
            holder.line.setWidth(holder.countryAndCity.getMeasuredWidth());

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

            final View.OnClickListener userClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (LingoXApplication.getInstance().getSkip()) {
                        SkipDialog.getDialog(context).show();
                    } else {
                        MobclickAgent.onEvent(context, "click_members");

                        Intent intent = new Intent(context,
                                UserInfoActivity.class);
                        intent.putExtra(UserInfoActivity.INTENT_USER_ID,
                                user.getId());
                        context.startActivityForResult(intent, NearByFragment.VIEW_USER);
                    }
                }
            };
            convertView.setOnClickListener(userClickListener);
        } else {
            holder.avatar.setImageResource(R.drawable.nearby_nopic_294dp);
            holder.info.setVisibility(View.GONE);
            holder.info2.setVisibility(View.GONE);
        }
        return convertView;
    }

    public void setIsFling(boolean isFling) {
        this.isFling = isFling;
    }

    private static class ViewHolder {
        TextView tag1, tag2, tag3, line, commentNum, comment, speak_, speak, countryAndCity, name, lalala;
        ImageView point, flag, avatar;
        LinearLayout info, info2;
    }

    private class LoadUserReferences extends AsyncTask<User, String, Boolean> {
        private TextView textView;
        private int num = 0;

        public LoadUserReferences(TextView textView) {
            this.textView = textView;
        }

        @Override
        protected Boolean doInBackground(User... params) {
            boolean success = false;
            try {
                num = (ServerHelper.getInstance().getUsersReferences(params[0].getId())).size();
                success = true;
            } catch (Exception e) {
//                Log.e("user", "Exception caught: " + e.toString());
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                textView.setText("" + num);
            } else {
                Toast.makeText(context, context.getString(R.string.fail_get_reference), Toast.LENGTH_LONG).show();
            }
        }
    }
}
