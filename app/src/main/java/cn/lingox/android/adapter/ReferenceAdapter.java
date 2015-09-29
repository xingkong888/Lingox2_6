package cn.lingox.android.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.entity.Reference;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.UIHelper;

public class ReferenceAdapter extends ArrayAdapter<Reference> {
    // Data Elements
    private Activity context;
    private ArrayList<Reference> referenceList;


    public ReferenceAdapter(Activity context, ArrayList<Reference> rList) {
        super(context, R.layout.row_reference, rList);
        this.context = context;
        this.referenceList = rList;
    }

    @Override
    public int getCount() {
        return referenceList.size();
    }

    @Override
    public Reference getItem(int position) {
        return referenceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;

        Reference reference = referenceList.get(position);

        if (rowView == null) {
            rowView = LayoutInflater.from(context).inflate(
                    R.layout.row_reference, parent, false);
            holder = new ViewHolder();
            holder.avatar = (ImageView) rowView.findViewById(R.id.avatar_reference);
            holder.name = (TextView) rowView.findViewById(R.id.name_reference);
            holder.time = (TextView) rowView.findViewById(R.id.time_reference);
            holder.content = (TextView) rowView.findViewById(R.id.content_reference);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        final User user = CacheHelper.getInstance().getUserInfo(reference.getUserSrcId());
        if (user != null) {
            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar,
                    user.getAvatar());
            holder.name.setText(user.getNickname());

            UIHelper.getInstance().textViewSetPossiblyNullString(holder.time, JsonHelper.getInstance().parseSailsJSDate(reference.getUpdatedAt(), 0));

        }

        holder.content.setText(TextUtils.isEmpty(reference.getContent()) ? reference.getTitle()
                : reference.getContent());
        return rowView;
    }

    static class ViewHolder {
        public ImageView avatar;
        public TextView name, time;
        public TextView content;

    }
}