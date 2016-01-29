package cn.lingox.android.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.chat.EMContact;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cn.lingox.android.Constant;
import cn.lingox.android.R;
import cn.lingox.android.activity.MainActivity;
import cn.lingox.android.entity.ChatAndNotify;
import cn.lingox.android.entity.Indent;
import cn.lingox.android.entity.LingoNotification;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.TimeHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.GetUser;
import cn.lingox.android.utils.SmileUtils;

public class ChatAllHistoryAdapter extends BaseAdapter {
    private Activity context;
    private MainActivity activity;
    private ArrayList<ChatAndNotify> datas;
    private ArrayList<Indent> indentDatas;

    public ChatAllHistoryAdapter(Activity context, ArrayList<ChatAndNotify> cList) {
        this.context = context;
        this.datas = cList;
        activity = (MainActivity) context;
        indentDatas = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    public int remove(int position) {
        datas.remove(position);
        return datas.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.news_spec, parent, false);
            holder = new ViewHolder();
            holder.layout = (LinearLayout) convertView.findViewById(R.id.news_spec_layout);
            holder.avatar = (ImageView) convertView.findViewById(R.id.chat_user_avatar);
            holder.name = (TextView) convertView.findViewById(R.id.chat_user_name);
            holder.unreadLabel = (TextView) convertView.findViewById(R.id.chat_message_num);
//            holder.readMsg = (ImageView) convertView.findViewById(R.id.chat_read_msg);
            holder.message = (TextView) convertView.findViewById(R.id.chat_message);
            holder.time = (TextView) convertView.findViewById(R.id.chat_date);
            holder.state = (TextView) convertView.findViewById(R.id.msg_state);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.message.setTextColor(Color.rgb(171, 171, 171));
        // 根据类型，强转数据
        switch (datas.get(position).getType()) {
            case 0://EMEMConversation
                holder.state.setVisibility(View.GONE);
                chat(position, holder);
                break;
            case 1://通知
                notifition(position, holder);
                break;
        }
        return convertView;
    }

    //聊天
    private void chat(int position, final ViewHolder holder) {
        EMConversation conversation = (EMConversation) datas.get(position).getObj();
        final String username = conversation.getUserName();
        boolean isGroup = false;
        List<EMGroup> groups = EMGroupManager.getInstance().getAllGroups();
        EMContact contact = null;
        for (EMGroup group : groups) {
            if (group.getGroupId().equals(username)) {
                isGroup = true;
                contact = group;
                break;
            }
        }
        final User user = CacheHelper.getInstance().getUserInfoFromUsername(username);
        boolean loadUserdatasFromServer = (user == null);

        // --- Use datas we definitely have ---
        if (isGroup) {
            holder.avatar.setImageResource(R.drawable.group_icon);
            String str = contact.getNick() != null ? contact.getNick() : username;
            if (str.length() >= 20) {
                str = str.substring(0, 17) + "...";
            }
            holder.name.setText(str);
        } else {
            if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
                holder.name.setText("Notification");
            } else {
                if (user != null) {
                    new GetMessage(holder.state).execute(user.getId());
                }
                holder.name.setText(conversation.getUserName());
            }
        }
        if (conversation.getUnreadMsgCount() > 0) {
            holder.unreadLabel.setText(String.valueOf(conversation.getUnreadMsgCount()));
            holder.unreadLabel.setVisibility(View.VISIBLE);
        } else {
            holder.unreadLabel.setVisibility(View.INVISIBLE);
        }

        if (conversation.getMsgCount() != 0) {
            EMMessage lastMessage = conversation.getLastMessage();
            holder.message.setText(SmileUtils.getSmiledText(context, getMessageDigest(lastMessage)), TextView.BufferType.SPANNABLE);
            holder.time.setText(TimeHelper.getInstance().parseTimestampToTime(lastMessage.getMsgTime()));
        }

        // --- Set UI Elements that may require loading from Cache or Server ---
        // --- Start loading datas from server if required ---
        if (loadUserdatasFromServer) {
            new Thread() {
                public void run() {
                    try {
                        final User tempUser = ServerHelper.getInstance().getUserInfo(username);
                        if (tempUser != null) {
                            new GetMessage(holder.state).execute(tempUser.getId());
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar, tempUser.getAvatar(), "circular");
                                    holder.name.setText(tempUser.getNickname());
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e("ChatAllHistoryAdapter", e.getMessage());
                    }
                }
            }.start();
        } else { // --- Use datas we had in the cache ---
            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar, user.getAvatar(), "circular");
            holder.name.setText(user.getNickname());
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (datas.get(position).getObj() instanceof LingoNotification) {
            return ((LingoNotification) datas.get(position).getObj()).getType();
        } else {
            return 0;
        }
    }

    @Override
    public int getViewTypeCount() {
        return LingoNotification.NUMBER_OF_NOTIFICATION_TYPES;
    }

    //通知的
    private void notifition(final int position, final ViewHolder holder) {
        final LingoNotification notify = (LingoNotification) datas.get(position).getObj();
        final User notificationUser = CacheHelper.getInstance().getUserInfo(notify.getUser_src());
        if (notificationUser == null)
            new GetUser(notify.getUser_src(), new GetUser.Callback() {
                @Override
                public void onSuccess(User user) {
                    loadView(holder, notify, user);
                }

                @Override
                public void onFail() {
                }
            });
        else {
            loadView(holder, notify, notificationUser);
        }
    }

    private void loadView(final ViewHolder holder, final LingoNotification notification, final User user) {
        // --- Use data we definitely have ---
        // We should have User info due to the MainActivity getNotifications AsyncTask
        UIHelper uiHelper = UIHelper.getInstance();
        if (notification.getRead()) {
            holder.unreadLabel.setVisibility(View.GONE);
        } else {
            holder.unreadLabel.setVisibility(View.VISIBLE);
        }
        uiHelper.textViewSetPossiblyNullString(holder.name, user.getNickname());

        uiHelper.textViewSetPossiblyNullString(holder.time, JsonHelper.getInstance().parseSailsJSDate(notification.getCreatedAt(), 0));

        uiHelper.imageViewSetPossiblyEmptyUrl(activity, holder.avatar, user.getAvatar(), "circular");

        switch (notification.getType()) {
            case LingoNotification.TYPE_USER_FOLLOWED:
                //Followed
                uiHelper.textViewSetPossiblyNullString(holder.message, "Followed you.");
                break;
            case LingoNotification.TYPE_PATH_JOINED:
                uiHelper.textViewSetPossiblyNullString(holder.name, user.getNickname());
                uiHelper.textViewSetPossiblyNullString(holder.message, context.getString(R.string.chat_join));
                break;
            case LingoNotification.TYPE_PATH_COMMENT:
                uiHelper.textViewSetPossiblyNullString(holder.name, user.getNickname());
                holder.message.setSingleLine(false);
                String comment = context.getString(R.string.chat_comment);
                uiHelper.textViewSetPossiblyNullString(holder.message, comment);
                break;
            case LingoNotification.TYPE_PATH_CHANGE:
                uiHelper.textViewSetPossiblyNullString(holder.name, user.getNickname());
                uiHelper.textViewSetPossiblyNullString(holder.message, "Changed activity!");
                break;
            case LingoNotification.TYPE_USER_COMMENT:
                uiHelper.textViewSetPossiblyNullString(holder.name, user.getNickname());
                uiHelper.textViewSetPossiblyNullString(holder.message, context.getString(R.string.comment_notification));
                break;
            case LingoNotification.TYPE_INDENT_FINISH://填写逻辑
                uiHelper.textViewSetPossiblyNullString(holder.name, user.getNickname());
                uiHelper.textViewSetPossiblyNullString(holder.message, context.getString(R.string.indent_prompt));
                break;
            case LingoNotification.TYPE_TRAVEL_LIKED://liked旅行者发布的
                uiHelper.textViewSetPossiblyNullString(holder.name, user.getNickname());
                uiHelper.textViewSetPossiblyNullString(holder.message, context.getString(R.string.liked_travel));
                break;
        }
    }

    private String getMessageDigest(EMMessage message) {
        String digest;
        switch (message.getType()) {
            case LOCATION:
                if (message.direct == EMMessage.Direct.RECEIVE) {
                    digest = context.getString(R.string.location_recv);
                    digest = String.format(digest, message.getFrom());
                    return digest;
                } else {
                    digest = context.getResources().getString(R.string.location_prefix);
                }
                break;
            case IMAGE:
                ImageMessageBody imageBody = (ImageMessageBody) message.getBody();
                digest = context.getString(R.string.picture) + imageBody.getFileName();
                break;
            case VOICE:
                digest = context.getString(R.string.voice);
                break;
            case VIDEO:
                digest = context.getString(R.string.video);
                break;
            case TXT:
                if (!message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    TextMessageBody txtBody = (TextMessageBody) message.getBody();
                    digest = txtBody.getMessage();
                } else {
                    TextMessageBody txtBody = (TextMessageBody) message.getBody();
                    digest = context.getString(R.string.voice_call) + txtBody.getMessage();
                }
                break;
            case FILE:
                digest = context.getString(R.string.file);
                break;
            default:
                Log.e("ChatAllHistoryAdapter", "Error, unknown type");
                return "";
        }
        return digest;
    }

    static class ViewHolder {
        LinearLayout layout;
        TextView name, message, time, state;
        ImageView avatar;//, readMsg
        TextView unreadLabel;

    }

    //获取聊天信息的异步任务
    private class GetMessage extends AsyncTask<String, Void, Boolean> {
        HashMap<String, String> map = new HashMap<>();
        private String userId;
        private TextView view;
        private ArrayList<Indent> tempData;

        public GetMessage(TextView view) {
            this.view = view;
            tempData = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            userId = params[0];
            //通过username获取每个用户的订单数据
            map.put("tarId", CacheHelper.getInstance().getSelfInfo().getId());
            map.put("userId", userId);
            indentDatas.clear();
            tempData.clear();
            try {
                tempData.addAll(ServerHelper.getInstance().getApplication(map));
                for (Indent indent : tempData) {
                    if (indent.getUserId().contentEquals(CacheHelper.getInstance().getSelfInfo().getId())) {
                        indentDatas.add(indent);
                    } else {
                        if (indent.getState() != 2) {
                            indentDatas.add(indent);
                        }
                    }
                }
                Collections.reverse(indentDatas);
                return true;
            } catch (Exception e1) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                view.getPaint().setFlags(0);
                Indent indent;
                if (indentDatas.size() != 0) {
                    view.setVisibility(View.VISIBLE);
                    if (indentDatas.size() == 1) {
                        indent = indentDatas.get(0);
                        switch (indent.getState()) {
                            case 1:
                                if (indent.getUserId().contentEquals(CacheHelper.getInstance().getSelfInfo().getId())) {
                                    view.setText(context.getString(R.string.wait_confirm));//待处理
                                } else {
                                    view.setText(context.getString(R.string.received));//待处理
                                }
                                break;
                            case 2:
                                view.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                                view.setTextColor(Color.rgb(199, 199, 199));
                                view.setText(context.getString(R.string.application_cancelled));//申请者取消
                                break;
                            case 3:
                                view.setTextColor(Color.rgb(0, 131, 143));
                                view.setText(context.getString(R.string.application_accepted));//同意
                                break;
                            case 4:
                                view.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                                view.setTextColor(Color.rgb(199, 199, 199));
                                view.setText(context.getString(R.string.application_declined));//被拒绝
                                break;
                            case 5:
                                view.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                                view.setTextColor(Color.rgb(199, 199, 199));
                                view.setText(context.getString(R.string.time_out));//时间过
                        }
                    } else {
                        view.setText(new StringBuilder().append(tempData.size()).append(" applications"));//接收者
                    }
                }
            }
        }
    }
}
