package cn.lingox.android.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.lingox.android.Constant;
import cn.lingox.android.R;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.widget.Sidebar;

public class ContactAdapter extends ArrayAdapter<User> implements SectionIndexer {

    // Data Elements
    private Activity context;
    private LayoutInflater inflater;
    private ArrayList<User> contacts;
    private int rowResource;

    private SparseIntArray positionOfSection;
    private SparseIntArray sectionOfPosition;
//    private Sidebar sidebar;

    public ContactAdapter(Activity context, int resource, ArrayList<User> cList, Sidebar sidebar) {
        super(context, resource, cList);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.rowResource = resource;
        this.contacts = cList;
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        final ViewHolder holder;
        if (rowView == null) {
            rowView = inflater.inflate(rowResource, null);
            holder = new ViewHolder();
            holder.avatar = (ImageView) rowView.findViewById(R.id.avatar);
            holder.unreadMsgView = (TextView) rowView.findViewById(R.id.unread_msg_number);
            holder.nameTextView = (TextView) rowView.findViewById(R.id.name);
            holder.tvHeader = (TextView) rowView.findViewById(R.id.header);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        User user = contacts.get(position);
        String username = user.getUsername();
        String header = user.getHeader();
        if (!TextUtils.isEmpty(header)) {
            if (position == 0) {
                holder.tvHeader.setVisibility(View.VISIBLE);
                holder.tvHeader.setText(header);
            } else if (!header.equals(getItem(position - 1).getHeader())) {
                holder.tvHeader.setVisibility(View.VISIBLE);
                holder.tvHeader.setText(header);
            } else {
                holder.tvHeader.setVisibility(View.GONE);
            }
        } else {
            holder.tvHeader.setVisibility(View.GONE);
            Log.e("ContactAdapter", "Header was empty");
        }
        switch (username) {
            case Constant.NEW_FRIENDS_USERNAME:
                holder.nameTextView.setText("Find new friends!");
                holder.avatar.setImageResource(R.drawable.new_friends_icon);
                break;
            case Constant.GROUP_USERNAME:
                holder.nameTextView.setText(user.getNickname());
                holder.avatar.setImageResource(R.drawable.groups_icon);
                break;
            default:
                holder.nameTextView.setText(user.getNickname());
                UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar, user.getAvatar());

                if (holder.unreadMsgView != null)
                    holder.unreadMsgView.setVisibility(View.INVISIBLE);
                break;
        }
        return rowView;
    }

    public int getPositionForSection(int section) {
        return positionOfSection.get(section);
    }

    public int getSectionForPosition(int position) {
        return sectionOfPosition.get(position);
    }

    @Override
    public Object[] getSections() {
        positionOfSection = new SparseIntArray();
        sectionOfPosition = new SparseIntArray();
        int count = getCount();
        List<String> list = new ArrayList<>();
        list.add(getContext().getString(R.string.search_header));
        positionOfSection.put(0, 0);
        sectionOfPosition.put(0, 0);
        for (int i = 1; i < count; i++) {
            String letter = getItem(i).getHeader();
            int section = list.size() - 1;
            if (list.get(section) != null && !list.get(section).equals(letter)) {
                list.add(letter);
                section++;
                positionOfSection.put(section, i);
            }
            sectionOfPosition.put(i, section);
        }
        return list.toArray(new String[list.size()]);
    }

    static class ViewHolder {
        public ImageView avatar;
        public TextView unreadMsgView;
        public TextView nameTextView;
        public TextView tvHeader;
    }
}
