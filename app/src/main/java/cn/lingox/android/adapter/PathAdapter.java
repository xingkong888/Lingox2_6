package cn.lingox.android.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qiniu.android.storage.UploadManager;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.PathTags;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.GetUser;
import cn.lingox.android.utils.FileUtil;
import cn.lingox.android.utils.ImageCache;

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
            holder.type1 = (TextView) convertView.findViewById(R.id.path_type_1);
            holder.type2 = (TextView) convertView.findViewById(R.id.path_type_2);
            holder.type3 = (TextView) convertView.findViewById(R.id.path_type_3);
            holder.layout = (RelativeLayout) convertView.findViewById(R.id.abc);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (!isFling) {
            final Path path = datas.get(position);
            holder.layout.setVisibility(View.VISIBLE);
            holder.type1.setVisibility(View.GONE);
            holder.type2.setVisibility(View.GONE);
            holder.type3.setVisibility(View.GONE);
            switch (path.getTags().size()) {
                case 1:
                    holder.type1.setText(tags.get(Integer.valueOf(path.getTags().get(0))).getTag());
                    holder.type1.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    holder.type1.setText(tags.get(Integer.valueOf(path.getTags().get(0))).getTag());
                    holder.type2.setText(tags.get(Integer.valueOf(path.getTags().get(1))).getTag());
                    holder.type1.setVisibility(View.VISIBLE);
                    holder.type2.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    holder.type1.setText(tags.get(Integer.valueOf(path.getTags().get(0))).getTag());
                    holder.type2.setText(tags.get(Integer.valueOf(path.getTags().get(1))).getTag());
                    holder.type3.setText(tags.get(Integer.valueOf(path.getTags().get(2))).getTag());
                    holder.type1.setVisibility(View.VISIBLE);
                    holder.type2.setVisibility(View.VISIBLE);
                    holder.type3.setVisibility(View.VISIBLE);
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
            holder.location.setText(path.getLocationString());
            switch (path.getType()) {
                case 1:
//                Log.d("天气","本地人");
                    holder.traveler.setVisibility(View.GONE);
                    holder.local.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    // Log.d("天气","旅行者");
                    holder.local.setVisibility(View.GONE);
                    holder.traveler.setVisibility(View.VISIBLE);
                    break;
            }
            holder.title.setSingleLine(true);
            holder.title.setEllipsize(TextUtils.TruncateAt.END);
            //TODO 暂时实现，TextView省略显示有问题
            String str = path.getTitle();
            if (str.length() >= 29) {
                str = str.substring(0, 22) + "...";
            }
            holder.title.setText(str);
            holder.acceptNumber.setText(String.valueOf(path.getAcceptedUsers().size()));
            holder.commentNumber.setText(String.valueOf(path.getComments().size()));
            holder.pathImg.setTag(path.getImage());
            if (ImageCache.getInstance().get(path.getImage()) != null && holder.pathImg.getTag().equals(path.getImage())) {
                holder.pathImg.setImageBitmap(ImageCache.getInstance().get(path.getImage()));
            } else {
                if (FileUtil.getImg(path.getImage()) == null && holder.pathImg.getTag().equals(path.getImage())) {
                    UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(holder.pathImg, path.getImage());
                } else if (holder.pathImg.getTag().equals(path.getImage())) {
                    holder.pathImg.setImageBitmap(FileUtil.getImg(path.getImage()));
                } else {
                    holder.pathImg.setImageResource(R.drawable.discover_default);
                }
            }
        } else {
            holder.title.setText("");
            holder.acceptNumber.setText("0");
            holder.commentNumber.setText("0");
            holder.location.setText("");
            holder.traveler.setVisibility(View.GONE);
            holder.local.setVisibility(View.GONE);
            holder.name.setText("");
            holder.pathImg.setImageResource(R.drawable.nearby_nopic_294dp);
            holder.avatar.setImageResource(R.drawable.nearby_nopic_294dp);
            holder.type1.setVisibility(View.GONE);
            holder.type2.setVisibility(View.GONE);
            holder.type3.setVisibility(View.GONE);
            holder.layout.setVisibility(View.GONE);
        }
        return convertView;
    }

    public void setIsFling(boolean isFling) {
        this.isFling = isFling;
    }

    private static class ViewHolder {
        TextView title;
        ImageView pathImg;
        TextView acceptNumber;
        TextView commentNumber;
        TextView location;
        TextView traveler;
        TextView local;
        TextView type1;
        TextView type2;
        TextView type3;
        TextView name, lalala;
        RelativeLayout layout;
        ImageView avatar;
    }
}
