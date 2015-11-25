package cn.lingox.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.activity.UserInfoActivity;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.utils.CircularImageView;

public class TravelLikeAdapter extends BaseAdapter {
    private static final String LOG_TAG = "LocalJoinedUsersAdapter";

    private Activity context;
    private LayoutInflater inflater;
    private ArrayList<User> userList;

    public TravelLikeAdapter(Activity context, ArrayList<User> uList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.userList = uList;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public User getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(User user) {
        userList.add(user);
    }

    public void removeItem(int position) {
        userList.remove(position);
    }

    public void removeItem(User user) {
        int i = -1;
        for (User u : userList) {
            if (u.getId().equals(user.getId())) {
                i = userList.indexOf(u);
                break;
            }
        }
        if (i != -1) {
            removeItem(i);
        } else {
            Log.e(LOG_TAG, "removeItem(): User not found");
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View listView = convertView;
        ViewHolder holder;
        User user = userList.get(position);
        if (listView == null) {
            listView = inflater.inflate(R.layout.row_path_joined_user_avatar, parent, false);
            holder = new ViewHolder();
            holder.photo = (CircularImageView) listView.findViewById(R.id.path_joined_user_avatar);
            listView.setTag(holder);
        } else {
            holder = (ViewHolder) listView.getTag();
        }
        UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.photo, user.getAvatar(), "");
        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LingoXApplication.getInstance().getSkip()) {
                    Intent mIntent = new Intent(context, UserInfoActivity.class);
                    mIntent.putExtra(UserInfoActivity.INTENT_USER_ID, getItem(position).getId());
                    context.startActivity(mIntent);
                }
            }
        });

        return listView;
    }

    static class ViewHolder {
        CircularImageView photo;
    }
}
