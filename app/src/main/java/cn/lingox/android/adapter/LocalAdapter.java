package cn.lingox.android.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.PathTags;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.GetUser;

/**
 * local的适配器
 */
public class LocalAdapter extends BaseAdapter {
    //格式化距离，保留小数点后两位
    private final DecimalFormat format = new DecimalFormat("##.00");
    private Activity context;
    private ArrayList<Path> datas;
    private ArrayList<PathTags> tags;
    private float[] results = new float[1];
    //标识listview是否滑动 true滑动  false未滑动
    // 用于数据加载的控制
    private boolean loading = false;

    public LocalAdapter(Activity context, ArrayList<Path> list) {
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_path_fragment, parent, false);
            holder = new ViewHolder();
            holder.avatar = (ImageView) convertView.findViewById(R.id.path_user_avatar);
            holder.acceptNumber = (TextView) convertView.findViewById(R.id.path_people_num);
            holder.title = (TextView) convertView.findViewById(R.id.path_title);
            holder.pathImg = (ImageView) convertView.findViewById(R.id.path_bag);
            holder.favourite = (ImageView) convertView.findViewById(R.id.path_favourite);
            holder.commentNumber = (TextView) convertView.findViewById(R.id.path_comment_num);
            holder.location = (TextView) convertView.findViewById(R.id.path_location);
            holder.lalala = (TextView) convertView.findViewById(R.id.asdfasdfasdf);
            holder.tag1 = (TextView) convertView.findViewById(R.id.path_tag_1);
            holder.tag2 = (TextView) convertView.findViewById(R.id.path_tag_2);
            holder.tag3 = (TextView) convertView.findViewById(R.id.path_tag_3);
            holder.address = "";
            holder.distance = 0;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
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
        if (!loading) {
            final User user = CacheHelper.getInstance().getUserInfo(path.getUserId());
            if (user == null) {
                new GetUser(path.getUserId(), new GetUser.Callback() {
                    @Override
                    public void onSuccess(User cbUser) {
                        CacheHelper.getInstance().addUserInfo(cbUser);
                        //设置头像
                        UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar, cbUser.getAvatar(), "circular");
                    }

                    @Override
                    public void onFail() {
                        Toast.makeText(context, "User information download fail", Toast.LENGTH_SHORT).show();
                    }
                }).execute();
            } else {
                UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar, user.getAvatar(), "circular");
            }
        } else {
            //设置默认头像
            holder.avatar.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar));
        }
        //只显示国家城市和距离；如果距离超过多少米，那么就无需显示
        //显示活动与自己位置的距离
        //如果省份为空，则只显示国家，否则显示国家和省份
        holder.location.setTag(path.getTitle());
        holder.address = path.getProvince().isEmpty() ? path.getChosenCountry() : (path.getChosenCountry() + ", " + path.getProvince());
        if (!LingoXApplication.getInstance().getLatitude().isEmpty() &&
                !LingoXApplication.getInstance().getLongitude().isEmpty() &&
                !path.getLatitude().isEmpty() &&
                !path.getLongitude().isEmpty() &&
                !"0".equals(path.getLatitude()) &&
                !"0".equals(path.getLongitude())) {
            Location.distanceBetween(
                    Double.valueOf(LingoXApplication.getInstance().getLatitude()),
                    Double.valueOf(LingoXApplication.getInstance().getLongitude()),
                    Double.valueOf(path.getLatitude()),
                    Double.valueOf(path.getLongitude())
                    , results);
            holder.distance = results[0];
            if (holder.distance < 1000) {
                //小于1km
                if (path.getTitle().equals(holder.location.getTag())) {
                    holder.location.setText(String.format(context.getString(R.string.distance), holder.address, format.format(holder.distance), "m"));
                }
            } else if (holder.distance >= 1000 && holder.distance < 500 * 1000) {
                //若距离大于1km且小于500km
                if (path.getTitle().equals(holder.location.getTag())) {
                    holder.location.setText(String.format(context.getString(R.string.distance), holder.address, format.format(holder.distance / 1000f), "km"));
                }
            } else {
                //距离大于500km
                if (path.getTitle().equals(holder.location.getTag())) {
                    holder.location.setText(holder.address);
                }
            }
        } else {
            if (path.getTitle().equals(holder.location.getTag())) {
                holder.location.setText(holder.address);
            }
        }
        //暂时实现，TextView省略显示有问题
        String str = path.getTitle();
        if (str.length() > 27) {
            str = str.substring(0, 25) + "…";
        }
        holder.title.setText(str);
        holder.acceptNumber.setText(String.valueOf(path.getAcceptedUsers().size()));
        holder.commentNumber.setText(String.valueOf(path.getComments().size()));
        holder.pathImg.setTag(path.getImage());
        if (holder.pathImg.getTag().equals(path.getImage())) {
            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(
                    context, holder.pathImg, path.getImage21(), "original");
        }

        if (!LingoXApplication.getInstance().getSkip() && path.hasUserAccepted(CacheHelper.getInstance().getSelfInfo().getId())) {
            holder.favourite.setImageResource(R.drawable.active_like_24dp);
            holder.favourite.setTag(1);
        } else {
            holder.favourite.setImageResource(R.drawable.active_dislike_24dp);
            holder.favourite.setTag(0);
        }
        holder.favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((int) holder.favourite.getTag() == 1) {
                    new UnAcceptPath(holder.favourite, holder.acceptNumber, path).execute();
                } else {
                    new AcceptPath(holder.favourite, holder.acceptNumber, path).execute();
                }
            }
        });

        return convertView;
    }

    /**
     * 设置listView是否在滑动
     *
     * @param loading true滑动 false空闲
     */
    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    static class ViewHolder {
        String address;
        float distance;
        ImageView pathImg, avatar, favourite;
        TextView title, acceptNumber, commentNumber, location, tag1, tag2, tag3, lalala;
    }

    private class AcceptPath extends AsyncTask<Void, Void, Boolean> {
        private ImageView view;
        private TextView num;
        private Path path;
        private ProgressDialog pd;

        public AcceptPath(ImageView view, TextView num, Path path) {
            this.view = view;
            this.num = num;
            this.path = path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            view.setClickable(false);
            pd = new ProgressDialog(context);
            pd.setMessage("Are being added to the favorite.");
            pd.show();
            pd.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ServerHelper.getInstance().acceptPath(path.getId(), CacheHelper.getInstance().getSelfInfo().getId());
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (success) {
                path.addAcceptedUser(CacheHelper.getInstance().getSelfInfo());
                num.setText(String.valueOf((Integer.parseInt(num.getText().toString()) + 1)));
                view.setImageResource(R.drawable.active_like_24dp);
                view.setTag(1);
                Toast.makeText(context, "Added to favorite list", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.fail_jion), Toast.LENGTH_SHORT).show();
            }
            view.setClickable(true);
        }
    }

    private class UnAcceptPath extends AsyncTask<Void, Void, Boolean> {

        private ImageView view;
        private TextView num;
        private Path path;
        private ProgressDialog pd;

        public UnAcceptPath(ImageView view, TextView num, Path path) {
            this.view = view;
            this.num = num;
            this.path = path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            view.setClickable(false);
            pd = new ProgressDialog(context);
            pd.setMessage("Favorite is cancelled.");
            pd.show();
            pd.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ServerHelper.getInstance().unAcceptPath(path.getId(), CacheHelper.getInstance().getSelfInfo().getId());
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (success) {
                path.removeAcceptedUser(CacheHelper.getInstance().getSelfInfo());
                view.setImageResource(R.drawable.active_dislike_24dp);
                view.setTag(0);
                num.setText(String.valueOf(path.getAcceptedUsers().size()));
            } else {
                Toast.makeText(context, context.getString(R.string.fail_jion), Toast.LENGTH_SHORT).show();
            }
            view.setClickable(true);
        }
    }
}
