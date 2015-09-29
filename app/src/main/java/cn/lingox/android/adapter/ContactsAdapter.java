package cn.lingox.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.Constant;
import cn.lingox.android.R;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.UIHelper;

public class ContactsAdapter extends ArrayAdapter<User> {
    // Constants
    static final int ROW_SEARCH = 0;
    static final int ROW_CONTACT = 1;

    // Data Elements
    private Activity context;
    private LayoutInflater inflater;
    private ArrayList<User> datas;
    private int rowResource;

    public ContactsAdapter(Activity context, int resource, ArrayList<User> cList) {
        super(context, resource, cList);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.datas = cList;
        this.rowResource = resource;
    }

    @Override
    public User getItem(int position) {
        return position == 0 ? null : super.getItem(position - 1);
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? ROW_SEARCH : ROW_CONTACT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        final ViewHolder holder;
        final ViewHolderSearchBar holderSearchBar;
        int rowType = getItemViewType(position);

        if (rowType == ROW_SEARCH) {
            if (rowView == null) {
                rowView = inflater.inflate(R.layout.search_bar_with_padding, parent, false);
                holderSearchBar = new ViewHolderSearchBar();
                holderSearchBar.query = (EditText) rowView.findViewById(R.id.query);
                holderSearchBar.clearSearch = (ImageButton) rowView.findViewById(R.id.search_clear);
                holderSearchBar.query.addTextChangedListener(new TextWatcher() {
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        getFilter().filter(s);
                        if (s.length() > 0) {
                            holderSearchBar.clearSearch.setVisibility(View.VISIBLE);
                        } else {
                            holderSearchBar.clearSearch.setVisibility(View.INVISIBLE);
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    public void afterTextChanged(Editable s) {
                    }
                });
                holderSearchBar.clearSearch.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (context.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
                            if (context.getCurrentFocus() != null)
                                manager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(),
                                        InputMethodManager.HIDE_NOT_ALWAYS);
                        holderSearchBar.query.getText().clear();
                    }
                });
                rowView.setTag(holderSearchBar);
            }
        } else {    // rowType == ROW_CONTACT
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

            User user = getItem(position);
            String username = user.getUsername();
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
        }
        return rowView;
    }

    static class ViewHolderSearchBar {
        public EditText query;
        public ImageButton clearSearch;
    }

    static class ViewHolder {
        public ImageView avatar;
        public TextView unreadMsgView;
        public TextView nameTextView;
        public TextView tvHeader;
    }
}
