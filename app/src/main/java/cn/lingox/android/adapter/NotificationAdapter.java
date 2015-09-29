package cn.lingox.android.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.activity.MainActivity;
import cn.lingox.android.activity.PathViewActivity;
import cn.lingox.android.activity.UserInfoActivity;
import cn.lingox.android.entity.LingoNotification;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.GetUser;

public class NotificationAdapter extends BaseAdapter {
    private static final String LOG_TAG = "NotificationAdapter";

    private MainActivity activity;
    private LayoutInflater inflater;
    private ArrayList<LingoNotification> notificationList;

    public NotificationAdapter(MainActivity activity, ArrayList<LingoNotification> nList) {
        this.activity = activity;
        this.notificationList = nList;
        this.inflater = LayoutInflater.from(activity);
    }

    @Override
    public int getItemViewType(int position) {
        return notificationList.get(position).getType();
    }

    @Override
    public int getViewTypeCount() {
        return LingoNotification.NUMBER_OF_NOTIFICATION_TYPES;
    }

    @Override
    public int getCount() {
        return notificationList.size();
    }

    @Override
    public LingoNotification getItem(int position) {
        return notificationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        final ViewHolder holder;
        final int posi = position;

        final LingoNotification notification = notificationList.get(position);
        if (rowView == null) {
            switch (getItemViewType(position)) {
                case LingoNotification.TYPE_USER_FOLLOWED:
                    rowView = inflater.inflate(R.layout.row_notification_follow, parent, false);
                    break;

                case LingoNotification.TYPE_PATH_JOINED:
                    rowView = inflater.inflate(R.layout.row_notification_join, parent, false);
                    break;

                case LingoNotification.TYPE_PATH_COMMENT:
                    rowView = inflater.inflate(R.layout.row_notification_comment, parent, false);
                    break;

                default:
                    Log.e(LOG_TAG, "notification type incorrect, no layout for this notification type found. Type: " + getItemViewType(position));
                    break;
            }
            holder = new ViewHolder();
            holder.notificationLayout = (RelativeLayout) rowView.findViewById(R.id.notification_layout);
            holder.nickname = (TextView) rowView.findViewById(R.id.notification_nickname);
            holder.avatar = (ImageView) rowView.findViewById(R.id.user_avatar);
            holder.timestamp = (TextView) rowView.findViewById(R.id.notification_datetime);
            holder.deleteButton = (ImageView) rowView.findViewById(R.id.notification_delete_button);

            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        final User notificationUser = CacheHelper.getInstance().getUserInfo(notification.getUser_src());

        // TODO Possible add a progress bar for each notification that is setVisibility(View.GONE) in loadView()
        if (notificationUser == null)
            new GetUser(notification.getUser_src(), new GetUser.Callback() {
                @Override
                public void onSuccess(User user) {
                    loadView(posi, holder, notification, user);
                }

                @Override
                public void onFail() {
                    Log.e(LOG_TAG, "GetUser onFail()");
                }
            });
        else loadView(posi, holder, notification, notificationUser);
        if (notification.getRead()) {
            rowView.setBackgroundColor(Color.GRAY);
            Log.d(LOG_TAG, "GRAY" + position);
        } else rowView.setBackgroundColor(Color.LTGRAY);
        return rowView;
    }

    private void loadView(final int position, final ViewHolder holder, final LingoNotification notification, final User user) {
        // --- Use data we definitely have ---
        // We should have User info due to the MainActivity getNotifications AsyncTask
        UIHelper uiHelper = UIHelper.getInstance();
        uiHelper.textViewSetPossiblyNullString(holder.nickname, user.getNickname());
        uiHelper.textViewSetPossiblyNullString(holder.timestamp, JsonHelper.getInstance().parseSailsJSDate(notification.getCreatedAt()));
        uiHelper.imageViewSetPossiblyEmptyUrl(activity, holder.avatar, user.getAvatar());
        // --- Set UI Elements that may require loading from Cache or Server ---

        // --- Start loading data from server if required ---

        // TODO possible should be higher up in this method to ensure they run first, or maybe detach listeners initially
        // --- Set OnClickListeners ---
        final View.OnClickListener userClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(activity, UserInfoActivity.class);
                mIntent.putExtra(UserInfoActivity.INTENT_USER_ID, user.getId());
                if (!notification.getRead())
                    new ReadNotification(holder, position).execute(notification);
                activity.startActivity(mIntent);

            }
        };
        final View.OnClickListener pathClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(activity, PathViewActivity.class);
                mIntent.putExtra(PathViewActivity.PATH_TO_VIEW_ID, notification.getPath_id());
                if (!notification.getRead())
                    new ReadNotification(holder, position).execute(notification);
                activity.startActivity(mIntent);
            }
        };
        final View.OnClickListener deleteNotificationListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DeleteNotification(holder).execute(notification);
            }
        };

        // TODO Change the delete method from pressing a button to swiping the notification?
        holder.deleteButton.setOnClickListener(deleteNotificationListener);

        switch (notification.getType()) {
            case LingoNotification.TYPE_USER_FOLLOWED:
                holder.notificationLayout.setOnClickListener(userClickListener);
                break;
            case LingoNotification.TYPE_PATH_JOINED:
            case LingoNotification.TYPE_PATH_COMMENT:
                holder.notificationLayout.setOnClickListener(pathClickListener);
                break;
        }
    }

    static class ViewHolder {
        RelativeLayout notificationLayout;
        TextView nickname;
        ImageView avatar;
        TextView timestamp;
        ImageView deleteButton;
    }

    private class DeleteNotification extends AsyncTask<LingoNotification, Void, Boolean> {
        private ViewHolder holder;
        private LingoNotification notification;

        public DeleteNotification(ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            holder.deleteButton.setClickable(false);
            holder.notificationLayout.setClickable(false);
        }

        @Override
        protected Boolean doInBackground(LingoNotification... params) {
            notification = params[0];
            try {
                ServerHelper.getInstance().deleteNotification(notification.getId());
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "DeleteNotification().doInBackground: " + e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!success) {
                Toast.makeText(activity, "Failed to delete Notification", Toast.LENGTH_SHORT).show();
                holder.deleteButton.setClickable(true);
                holder.notificationLayout.setClickable(true);
            } else {
                notificationList.remove(notification);
                notifyDataSetChanged();
                //   activity.updateNotificationNumber();
            }
        }
    }

    private class ReadNotification extends AsyncTask<LingoNotification, Void, Boolean> {
        private ViewHolder holder;
        private int position;
        private LingoNotification notification;

        public ReadNotification(ViewHolder holder, int position) {
            this.holder = holder;
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(LingoNotification... params) {
            notification = params[0];
            try {
                ServerHelper.getInstance().readNotification(notification.getId());
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "readNotification().doInBackground: " + e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!success) {
                Toast.makeText(activity, activity.getString(R.string.failed_mark_read_notification), Toast.LENGTH_SHORT).show();
            } else {
                notificationList.get(position).setRead(true);
                holder.notificationLayout.setBackgroundColor(Color.GRAY);
            }
        }
    }
}
