package cn.lingox.android.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.PathTags;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.GetUser;

public class PathAdapter extends BaseAdapter {
    private Activity context;
    private ArrayList<Path> datas;
    private User user;
    private boolean isFling = false;
    private ArrayList<PathTags> tags;

    public PathAdapter(Activity context, ArrayList<Path> list) {
        this.context = context;
        this.datas = list;
        tags = LingoXApplication.getInstance().getDatas();
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
            convertView = LayoutInflater.from(context).inflate(R.layout.row_path_fragment, parent, false);
            holder = new ViewHolder();
            holder.avatar = (ImageView) convertView.findViewById(R.id.path_user_avatar);
            holder.name = (TextView) convertView.findViewById(R.id.path_user_name);
            holder.acceptNumber = (TextView) convertView.findViewById(R.id.path_people_num);
            holder.title = (TextView) convertView.findViewById(R.id.path_title);
            holder.pathImg = (ImageView) convertView.findViewById(R.id.path_bag);
            holder.commentNumber = (TextView) convertView.findViewById(R.id.path_comment_num);
            holder.location = (TextView) convertView.findViewById(R.id.path_location);
            holder.traveler = (TextView) convertView.findViewById(R.id.path_traveler);
            holder.local = (TextView) convertView.findViewById(R.id.path_local);
            holder.lalala = (TextView) convertView.findViewById(R.id.asdfasdfasdf);
            holder.tag1 = (TextView) convertView.findViewById(R.id.path_tag_1);
            holder.tag2 = (TextView) convertView.findViewById(R.id.path_tag_2);
            holder.tag3 = (TextView) convertView.findViewById(R.id.path_tag_3);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (!isFling) {
            final Path path = datas.get(position);
            holder.tag1.setVisibility(View.GONE);
            holder.tag2.setVisibility(View.GONE);
            holder.tag3.setVisibility(View.GONE);
            switch (path.getTags().size()) {
                case 1:
                    holder.tag1.setText(tags.get(Integer.valueOf(path.getTags().get(0))).getTag());
                    holder.tag1.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    holder.tag1.setText(tags.get(Integer.valueOf(path.getTags().get(0))).getTag());
                    holder.tag2.setText(tags.get(Integer.valueOf(path.getTags().get(1))).getTag());
                    holder.tag1.setVisibility(View.VISIBLE);
                    holder.tag2.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    holder.tag1.setText(tags.get(Integer.valueOf(path.getTags().get(0))).getTag());
                    holder.tag2.setText(tags.get(Integer.valueOf(path.getTags().get(1))).getTag());
                    holder.tag3.setText(tags.get(Integer.valueOf(path.getTags().get(2))).getTag());
                    holder.tag1.setVisibility(View.VISIBLE);
                    holder.tag2.setVisibility(View.VISIBLE);
                    holder.tag3.setVisibility(View.VISIBLE);
                    break;
            }
            if (LingoXApplication.getInstance().getSkip()) {
                new GetUser(path.getUserId(), new GetUser.Callback() {
                    @Override
                    public void onSuccess(User cbUser) {
                        user = cbUser;
                        CacheHelper.getInstance().addUserInfo(user);
                        String strName = user.getNickname();
                        holder.name.setText(strName);
                        //设置头像
                        UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar, user.getAvatar());
                    }

                    @Override
                    public void onFail() {
                        Toast.makeText(context, "User information download fail", Toast.LENGTH_SHORT).show();
                    }
                }).execute();
            } else {
                String strName = CacheHelper.getInstance().getUserInfo(path.getUserId()).getNicknameOrUsername();
                holder.name.setText(strName);
                UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar,
                        CacheHelper.getInstance().getUserInfo(path.getUserId()).getAvatar());
            }
//显示活动与自己位置的距离
//            holder.location.setText(path.getProvince()+" "+ DistanceHelper.distanceHelper(
//                    Double.valueOf(LingoXApplication.getInstance().getLatitude()),
//                    Double.valueOf(LingoXApplication.getInstance().getLongitude()),
//                    36, 116
////                    Double.valueOf(path.getLatitude()),Double.valueOf(path.getLocation())
//            )+"m ");
            holder.location.setText(path.getLocationString());
            switch (path.getType()) {
                case 1://本地人
                    holder.traveler.setVisibility(View.GONE);
                    holder.local.setVisibility(View.VISIBLE);
                    break;
                case 2://旅行者
                    holder.local.setVisibility(View.GONE);
                    holder.traveler.setVisibility(View.VISIBLE);
                    break;
            }
            holder.title.setSingleLine(true);
            holder.title.setEllipsize(TextUtils.TruncateAt.END);
            //TODO 暂时实现，TextView省略显示有问题
            String str = path.getTitle();
            holder.title.setText(str);
            holder.acceptNumber.setText(String.valueOf(path.getAcceptedUsers().size()));
            holder.commentNumber.setText(String.valueOf(path.getComments().size()));
            holder.pathImg.setTag(path.getImage());
            if (holder.pathImg.getTag().equals(path.getImage())) {
                UIHelper.getInstance().imageViewSetPossiblyEmptyUrl
                        (context, holder.pathImg, path.getImage21());
            }
        } else {
            holder.pathImg.setImageResource(R.drawable.nearby_nopic_294dp);
        }
        return convertView;
    }

    public void setIsFling(boolean isFling) {
        this.isFling = isFling;
    }

    static class ViewHolder {
        ImageView pathImg, avatar;
        TextView title, acceptNumber, commentNumber, location, traveler, local, tag1, tag2, tag3, name, lalala;
    }
}
