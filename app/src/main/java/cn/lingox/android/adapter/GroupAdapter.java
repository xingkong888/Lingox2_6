package cn.lingox.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import cn.lingox.android.R;
import cn.lingox.android.activity.UserInfoActivity;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.utils.CircularImageView;

public class GroupAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<String> list;
    private Context context;

    public GroupAdapter(Context context, List<String> groups) {
        this.inflater = LayoutInflater.from(context);
        this.list = groups;
        this.context = context;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_path_joined_user_avatar, parent, false);
            holder = new ViewHolder();
            holder.photo = (CircularImageView) convertView.findViewById(R.id.path_joined_user_avatar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final User user = CacheHelper.getInstance().getUserInfoFromUsername(list.get(position));

        if (user != null) {
            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.photo, user.getAvatar(), "circular");
            final View.OnClickListener userClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = new Intent(context, UserInfoActivity.class);
                    mIntent.putExtra(UserInfoActivity.INTENT_USER_ID, user.getId());
                    context.startActivity(mIntent);
                }
            };
            holder.photo.setOnClickListener(userClickListener);
        }
        return convertView;
    }

    static class ViewHolder {
        CircularImageView photo;
    }
}