package cn.lingox.android.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.EMError;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.ConnectionListener;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.NetUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import cn.lingox.android.R;
import cn.lingox.android.adapter.ChatAllHistoryAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.ChatAndNotify;
import cn.lingox.android.entity.LingoNotification;
import cn.lingox.android.entity.Reference;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.service.NotificationService;
import cn.lingox.android.task.GetUser;

public class ChatFragment extends Fragment {
    private static final String LOG_TAG = "ChatFragment";
    //是否为第一次进去该页面
    private static boolean isFirst = true;
    private static ChatFragment chatFragment;
    private int isExist = 3;//0 不存在  1  已存在，未读 3 已存在且已读

    private int unreadMSG = 0;
    private int unreadNotify = 0;
    private ProgressBar loading;

    // UI Elements
    private InputMethodManager inputMethodManager;
    private ListView listView;
    private ChatAllHistoryAdapter adapter;
    private RelativeLayout errorItem;
    private TextView errorText;
    private ImageView anim;
    private AnimationDrawable animationDrawable;
    private ArrayList<Reference> referenceList = new ArrayList<>();
    //huanxin
    private NewMessageBroadcastReceiver msgReceiver;
    // Data Elements
    private boolean hidden;
    private boolean isConflictDialogShow;
    private boolean isConflict = false;
    private android.app.AlertDialog.Builder conflictBuilder;
    //是否为正常登录 true 跳过 false正常登录
    private boolean isSkip = LingoXApplication.getInstance().getSkip();
    // 用于存储数据
    private ArrayList<ChatAndNotify> datas;
    // Following、comment的广播接收器
    private BroadcastReceiver notifyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getBooleanExtra(NotificationService.UPDATE, false)) {
                new LoadNotifications().execute();
            }
        }
    };
    private BroadcastReceiver ackMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            abortBroadcast();
            String msgid = intent.getStringExtra("msgid");
            String from = intent.getStringExtra("from");
            EMConversation conversation = EMChatManager.getInstance().getConversation(from);
            if (conversation != null) {
                // 把message设为已读
                EMMessage msg = conversation.getMessage(msgid);
                if (msg != null) {
                    // 2014-11-5 修复在某些机器上，在聊天页面对方发送已读回执时不立即显示已读的bug
                    if (ChatActivity.activityInstance != null) {
                        if (msg.getChatType() == EMMessage.ChatType.Chat) {
                            if (from.equals(ChatActivity.activityInstance.getToChatUsername()))
                                return;
                        }
                    }
                    msg.isAcked = true;
                }
            }
        }
    };
    // English
    // is this required?
    private BroadcastReceiver cmdMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            abortBroadcast();
            //Log.d(LOG_TAG, "收到透传消息 // cmdMessageReceiver onReceive()");
            //获取cmd message对象
            String msgId = intent.getStringExtra("msgid");
            EMMessage message = intent.getParcelableExtra("message");
            //获取消息body
            CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
            String action = cmdMsgBody.action;//获取自定义action

            //获取扩展属性 此处省略
            //Log.d(LOG_TAG, String.format("透传消息：action:%s,message:%s", action, message.toString()));
            String st9 = "Receive the passthrough:action：";
            Toast.makeText(getActivity(), st9 + action, Toast.LENGTH_SHORT).show();
        }
    };
    private showNum show;

    public static ChatFragment getObj() {
        return chatFragment;
    }

    public void setIsFirst(boolean isFirst1) {
        isFirst = isFirst1;
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            show = (showNum) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement showNum");
        }
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatFragment = this;
        if (!isSkip) {
            getActivity().registerReceiver(notifyReceiver, new IntentFilter(NotificationService.NOTIFICATION));
            msgReceiver = new NewMessageBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
            intentFilter.setPriority(3);
            getActivity().registerReceiver(msgReceiver, intentFilter);

            IntentFilter ackMessageIntentFilter = new IntentFilter(EMChatManager
                    .getInstance().getAckMessageBroadcastAction());
            ackMessageIntentFilter.setPriority(3);
            getActivity().registerReceiver(ackMessageReceiver, ackMessageIntentFilter);

            IntentFilter cmdMessageIntentFilter = new IntentFilter(EMChatManager.getInstance().getCmdMessageBroadcastAction());
            cmdMessageIntentFilter.setPriority(3);
            getActivity().registerReceiver(cmdMessageReceiver, cmdMessageIntentFilter);

            EMChatManager.getInstance().addConnectionListener(
                    new MyConnectionListener());
            EMChat.getInstance().setAppInited();
            new ConnectToHuanXin(CacheHelper.getInstance().getSelfInfo().getUsername(), CacheHelper.getInstance().getPassword()).execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation_history, container, false);

        anim = (ImageView) view.findViewById(R.id.anim);
        animationDrawable = (AnimationDrawable) anim.getBackground();

        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        errorItem = (RelativeLayout) view.findViewById(R.id.rl_error_item);
        errorText = (TextView) errorItem.findViewById(R.id.tv_connect_errormsg);

        loading = (ProgressBar) view.findViewById(R.id.progress);
        if (!LingoXApplication.getInstance().getSkip()) {
            loading.setVisibility(View.VISIBLE);
        } else {
            startAnim();
        }

        listView = (ListView) view.findViewById(R.id.chat_list);

        datas = new ArrayList<>();
        //下载通知
        if (!isSkip) {
            new LoadNotifications().execute();
            adapter = new ChatAllHistoryAdapter(getActivity(), datas);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // ListView的点击事件
                    listView.setClickable(false);
                    ChatAndNotify can = (ChatAndNotify) adapter.getItem(position);
                    switch (can.getType()) {
                        case 0://聊天
                            EMConversation conversation = (EMConversation) can.getObj();
                            String username = conversation.getUserName();
                            if (conversation.getUnreadMsgCount() > 0 && unreadMSG > 0) {
                                unreadMSG -= conversation.getUnreadMsgCount();
                            }
                            if (username.equals(CacheHelper.getInstance().getSelfInfo().getUsername())) {
                                Toast.makeText(getActivity(), "You cannot chat with yourself!", Toast.LENGTH_LONG).show();
                                Log.e(LOG_TAG, "User has somehow clicked themselves in the Chat History");
                            } else {
                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                if (conversation.isGroup()) {
                                    intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                                    intent.putExtra("groupId", username);
                                } else {
                                    intent.putExtra("username", username);
                                }
                                startActivity(intent);
                            }
                            listView.setClickable(true);
                            break;
                        case 1://  通知
                            final LingoNotification notify = (LingoNotification) can.getObj();
                            if (!notify.getRead() && unreadNotify > 0) {
                                unreadNotify -= 1;
                            }
                            final User notificationUser = CacheHelper.getInstance().getUserInfo(notify.getUser_src());
                            if (notificationUser == null) {
                                new GetUser(notify.getUser_src(), new GetUser.Callback() {
                                    @Override
                                    public void onSuccess(User user) {
                                        myNotify(notify, user);
                                    }

                                    @Override
                                    public void onFail() {
                                    }
                                });
                            } else {
                                myNotify(notify, notificationUser);
                            }
                            listView.setClickable(true);
                            break;
                    }
                }
            });
        }

        registerForContextMenu(listView);

        //  there's a better way to do this most likely using manifest or something
        // This was probably only here as before there was an EditText that would have opened the softkeyboard
        listView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (getActivity().getWindow().getAttributes().softInputMode
                        != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                    if (getActivity().getCurrentFocus() != null)
                        inputMethodManager.hideSoftInputFromWindow(
                                getActivity().getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });
        if (!isFirst) {
            refresh();
        }
        if (getActivity().getIntent().getBooleanExtra("conflict", false)
                && !isConflictDialogShow) {
            showConflictDialog();
        }
        return view;
    }

    private void startAnim() {
        if (!animationDrawable.isRunning()) {
            anim.setVisibility(View.VISIBLE);
            animationDrawable.start();
        }
    }

    private void stopAnim() {
        if (animationDrawable.isRunning()) {
            anim.setVisibility(View.GONE);
            animationDrawable.stop();
        }
    }

    private void myNotify(LingoNotification notify, User user) {
        switch (notify.getType()) {
            case LingoNotification.TYPE_USER_FOLLOWED://用户关注
                Intent mIntent = new Intent(getActivity(), UserInfoActivity.class);
                mIntent.putExtra(UserInfoActivity.INTENT_USER_ID, user.getId());
                if (!notify.getRead()) {
                    new ReadNotification().execute(notify);
                }
                startActivity(mIntent);
                break;
            case LingoNotification.TYPE_PATH_JOINED://加入活动
            case LingoNotification.TYPE_PATH_COMMENT://活动被评论
            case LingoNotification.TYPE_PATH_CHANGE://活动改变
                Intent mIntent1 = new Intent(getActivity(), LocalViewActivity.class);
                mIntent1.putExtra(LocalViewActivity.PATH_TO_VIEW_ID, notify.getPath_id());
                if (!notify.getRead()) {
                    new ReadNotification().execute(notify);
                }
                startActivity(mIntent1);
                break;
            case LingoNotification.TYPE_USER_COMMENT://用户评论
                Intent mIntent2 = new Intent(getActivity(), ReferenceActivity.class);
                mIntent2.putExtra(UserInfoFragment.TARGET_USER_ID, CacheHelper.getInstance().getSelfInfo().getId());
                mIntent2.putExtra(UserInfoFragment.TARGET_USER_NAME, user.getNickname());
                if (!notify.getRead()) {
                    new ReadNotification().execute(notify);
                }
                new LoadUserReferences(mIntent2).execute(user.getId());
                break;
            case LingoNotification.TYPE_INDENT_FINISH://申请完成---暂时有问题
                Intent mIntent3 = new Intent(getActivity(), ReferenceActivity.class);
                mIntent3.putExtra(UserInfoFragment.TARGET_USER_ID,
                        notify.getUser_src());
                mIntent3.putExtra(UserInfoFragment.TARGET_USER_NAME,
                        CacheHelper.getInstance().getSelfInfo().getNickname());
                if (!notify.getRead()) {
                    new ReadNotification().execute(notify);
                }
                new LoadUserReferences(mIntent3).execute(notify.getUser_src());
                break;
            case LingoNotification.TYPE_TRAVEL_LIKED://liked旅行者发布的问题
                Intent mIntent10 = new Intent(getActivity(), TravelViewActivity.class);
                mIntent10.putExtra(TravelViewActivity.TRAVEL_ID,
                        notify.getDemand_id());
                if (!notify.getRead()) {
                    new ReadNotification().execute(notify);
                }
                startActivity(mIntent10);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.delete_message, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_message) {
            int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
            // 删除
            ChatAndNotify can = (ChatAndNotify) adapter.getItem(position);
            switch (can.getType()) {
                case 0:// 聊天
                    EMConversation tobeDeleteCons = (EMConversation) can.getObj();
                    if (tobeDeleteCons.getUnreadMsgCount() > 0 && unreadMSG > 0) {
                        unreadMSG -= tobeDeleteCons.getUnreadMsgCount();
                    }
                    EMChatManager.getInstance().deleteConversation(tobeDeleteCons.getUserName(), tobeDeleteCons.isGroup());
                    if (adapter.remove(position) <= 0) {
                        startAnim();
                    } else {
                        stopAnim();
                    }
                    //TODO 向activity传递数据
                    show.showMessageNum(unreadMSG + unreadNotify);
                    loading.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    return true;
                case 1://通知
                    LingoNotification notify = (LingoNotification) can.getObj();
                    new DeleteNotification(position).execute(notify);
                    return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(msgReceiver);
        } catch (Exception e) {
            Log.e(LOG_TAG, "failed to unregisterReceiver(msgReceiver): " + e.toString());
        }
        try {
            getActivity().unregisterReceiver(ackMessageReceiver);
        } catch (Exception e) {
            Log.e(LOG_TAG, "failed to unregisterReceiver(ackMessageReceiver): " + e.toString());
        }
        try {
            getActivity().unregisterReceiver(cmdMessageReceiver);
        } catch (Exception e) {
            Log.e(LOG_TAG, "failed to unregisterReceiver(cmdMessageReceiver): " + e.toString());
        }
        try {
            getActivity().unregisterReceiver(notifyReceiver);
        } catch (Exception e) {
            Log.e(LOG_TAG, "failed to unregisterReceiver(notificationReceiver): " + e.toString());
        }
        if (conflictBuilder != null) {
            conflictBuilder.create().dismiss();
            conflictBuilder = null;
        }
    }

    // 更新聊天数据
    public void refresh() {
        try {
            ChatAndNotify can;
            unreadMSG = 0;
            if (loadConversationsWithRecentChat().size() > 0) {
                ArrayList<EMConversation> lists = loadConversationsWithRecentChat();
                Collections.reverse(lists);
                int isExist = 0;//0 不存在  1  已存在
                EMConversation conversation;
                int i, j;
                for (EMConversation c : lists) {
                    for (i = 0, j = datas.size(); i < j; i++) {
                        if (datas.get(i).getType() == 0) {
                            conversation = (EMConversation) datas.get(i).getObj();
                            if ((conversation.getUserName().equals(c.getUserName()))) {
                                //相等
                                if (c.getUnreadMsgCount() > 0) {
                                    isExist = 1;
                                    datas.remove(i);
                                } else if (c.getUnreadMsgCount() == 0) {
                                    isExist = 3;
                                }
                                break;
                            } else {
                                isExist = 0;
                            }
                        }
                    }
                    //根据isExist值判断是否先移除数据
                    if (isExist == 0) {
                        i++;
                    }
                    if (isExist == 0 || isExist == 1) {
                        //TODO 记录未读的信息
                        unreadMSG += c.getUnreadMsgCount();
                        can = new ChatAndNotify();
                        can.setType(0);
                        can.setObj(c);
                        datas.add(0, can);
                    }
                }
            }

            if (NetUtils.hasNetwork(getActivity())) {
                errorText.setText("Cannot connect to the chat server");
            } else {
                errorText.setText("Network is unavailable");
            }

            if (EMChatManager.getInstance().isConnected()) {
                errorItem.setVisibility(View.GONE);
            } else {
                errorItem.setVisibility(View.VISIBLE);
            }
            if (!isFirst) {
                notifyChange();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    private void notifyChange() {
        if (datas.size() <= 0) {
            startAnim();
        } else {
            stopAnim();
        }
        //TODO 向activity传递数据
        show.showMessageNum(unreadMSG + unreadNotify);
        loading.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    private ArrayList<EMConversation> loadConversationsWithRecentChat() {
        Hashtable<String, EMConversation> conversations = EMChatManager.getInstance().getAllConversations();
        ArrayList<EMConversation> conversationList = new ArrayList<>();
        for (EMConversation conversation : conversations.values()) {
            if (conversation.getAllMessages().size() != 0) {
                conversationList.add(conversation);
            }
        }
        sortConversationByLastChatTime(conversationList);
        return conversationList;
    }

    private void sortConversationByLastChatTime(List<EMConversation> conversationList) {
        Collections.sort(conversationList, new Comparator<EMConversation>() {
            @Override
            public int compare(final EMConversation con1, final EMConversation con2) {
                EMMessage con2LastMessage = con2.getLastMessage();
                EMMessage con1LastMessage = con1.getLastMessage();
                if (con2LastMessage.getMsgTime() == con1LastMessage.getMsgTime()) {
                    return 0;
                } else if (con2LastMessage.getMsgTime() > con1LastMessage.getMsgTime()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }
    // BroadcastReceivers

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.hidden = hidden;
        if (!hidden) {
            if (!isFirst) {
                refresh();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hidden) {
            if (!isFirst) {
                refresh();
            }
        }
        if (!isConflict) {
            EMChatManager.getInstance().activityResumed();
        }
        MobclickAgent.onPageStart("chatFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("chatFragment");
    }

    private void showConflictDialog() {
        isConflictDialogShow = true;
        LingoXApplication.getInstance().logout(null);
        if (getActivity() != null && !getActivity().isFinishing()) {
            // clear up global variables
            try {
                if (conflictBuilder == null) {
                    conflictBuilder = new android.app.AlertDialog.Builder(getActivity());
                }
                conflictBuilder.setTitle("Account Conflict");
                conflictBuilder.setMessage("You have been logged in elsewhere");
                conflictBuilder.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                conflictBuilder = null;
                                getActivity().finish();
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                            }
                        });
                conflictBuilder.setCancelable(false);
                conflictBuilder.create().show();
                isConflict = true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "showConflictDialog(): Exception caught: " + e.getMessage());
            }
        }
    }

    //从通知中读取详细数据
    private void getDatas(LingoNotification n) {
        ChatAndNotify can = new ChatAndNotify();
        if (datas.size() == 0) {
            can.setType(1);
            can.setObj(n);
            datas.add(0, can);
        } else {
            int i, j;
            for (i = 0, j = datas.size(); i < j; i++) {
                if (datas.get(i).getType() == 1) {
                    LingoNotification notify = (LingoNotification) datas.get(i).getObj();
                    if ((notify.getType() == n.getType()) && (notify.getUser_src().equals(n.getUser_src()))) {
                        //同一个用户的同一类型的通知
                        isExist = 1;
                        datas.remove(i);
                        if (!notify.getId().equals(n.getId())) {//用户相同，id不同，表示不是同一个通知
                            try {
                                ServerHelper.getInstance().deleteNotification(notify.getId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    } else if (i == datas.size() - 1) {
                        //集合中不存在该通知
                        isExist = 0;
                        break;
                    }
                }
            }
            //根据isExist值判断是否添加数据
            if (isExist == 0) {
                i++;
            }
            can.setType(1);
            can.setObj(n);
            datas.add(0, can);
        }
    }

    public interface showNum {
        void showMessageNum(int unread);
    }

    // 标注notify为已读
    private class ReadNotification extends AsyncTask<LingoNotification, Void, Boolean> {
        private LingoNotification notification;

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
                Toast.makeText(getActivity(), getActivity().getString(R.string.failed_mark_read_notification), Toast.LENGTH_SHORT).show();
            } else {
                notification.setRead(true);
                notifyChange();
            }
        }
    }

    // 删除选中的notify
    private class DeleteNotification extends AsyncTask<LingoNotification, Void, Boolean> {
        int position = 0;
        private LingoNotification notification;

        public DeleteNotification(int pos) {
            position = pos;
        }

        @Override
        protected Boolean doInBackground(LingoNotification... params) {
            notification = params[0];
            try {
                ServerHelper.getInstance().deleteNotification(notification.getId());
                if (unreadNotify > 0 && !notification.getRead()) {
                    unreadNotify -= 1;
                }
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
                Toast.makeText(getActivity(), getString(R.string.fail_del_notify), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Success to delete Notification", Toast.LENGTH_SHORT).show();
                datas.remove(position);
                notifyChange();
            }
        }
    }

    // 新消息广播接收器
    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 主页面收到消息后，主要为了提示未读，实际消息内容需要到chat页面查看
            String from = intent.getStringExtra("from");
            // 消息id
            String msgId = intent.getStringExtra("msgid");
            EMMessage message = EMChatManager.getInstance().getMessage(msgId);
            // fix: logout crash， 如果正在接收大量消息
            // 因为此时已经logout，消息队列已经被清空， broadcast延时收到，所以会出现message为空的情况
            if (message == null) {
                return;
            }

            // 2014-10-22 修复在某些机器上，在聊天页面对方发消息过来时不立即显示内容的bug
            if (ChatActivity.activityInstance != null) {
                if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                    if (message.getTo().equals(ChatActivity.activityInstance.getToChatUsername()))
                        return;
                } else {
                    if (from.equals(ChatActivity.activityInstance.getToChatUsername()))
                        return;
                }
            }
            // 注销广播接收者，否则在ChatActivity中会收到这个广播
            abortBroadcast();
            LingoXApplication.getInstance().notifyNewMessage(message);
            if (!isFirst) {
                refresh();
            }
        }
    }

    private class MyConnectionListener implements ConnectionListener {
        @Override
        public void onConnected() {
            if (!isFirst) {
                refresh();
            }
        }

        @Override
        public void onDisConnected(String errorString) {
            if (errorString != null && errorString.contains("conflict")) {
                showConflictDialog();
            } else {
                if (!isFirst) {
                    refresh();
                }
            }
        }

        @Override
        public void onReConnected() {
            if (!isFirst) {
                refresh();
            }
        }

        @Override
        public void onReConnecting() {
        }

        @Override
        public void onConnecting(String progress) {
        }
    }

    // Following 通知
    private class LoadNotifications extends AsyncTask<Void, Void, ArrayList<LingoNotification>> {
        @Override
        protected ArrayList<LingoNotification> doInBackground(Void... params) {
            ArrayList<LingoNotification> nList = new ArrayList<>();
            try {
                nList.addAll(ServerHelper.getInstance().getAllNotifications(CacheHelper.getInstance().getSelfInfo().getId()));
            } catch (Exception e) {
                Log.e(LOG_TAG, "Failed to get user's notifications: " + e.toString());
                return null;
            }
            // If we don't have the user information of the user that created the notification,
            // get it from the server
            //翻转list中的数据
            Collections.reverse(nList);
            unreadNotify = 0;
            for (LingoNotification n : nList) {
                if (!n.getRead()) {
                    unreadNotify += 1;
                }
                getDatas(n);
                if (CacheHelper.getInstance().getUserInfo(n.getUser_src()) == null) {
                    try {
                        User notificationUser = ServerHelper.getInstance().getUserInfo(n.getUser_src());
                        CacheHelper.getInstance().addUserInfo(notificationUser);
                    } catch (Exception e) {
                        //  In some way mark this notification as having an issue?
                        Log.e(LOG_TAG, "LoadNotifications: exception caught running ServerHelper.getUserInfo() for User: " + n.getUser_src());
                    }
                }
            }
            return nList;
        }

        //  Make it retry upon fail (only for a set number of tries)
        @Override
        protected void onPostExecute(ArrayList<LingoNotification> lingoNotifications) {
            super.onPostExecute(lingoNotifications);
            if (lingoNotifications == null) {
                Toast.makeText(getActivity(), "Failed to get Notifications", Toast.LENGTH_SHORT).show();
                //  Make the rightSideMenu show something to indicate no notifications, or disable the menu altogether
            } else {
                CacheHelper.getInstance().setNotificationList(lingoNotifications);
                if (!isSkip)
                    if (isFirst) {
                        //  更新chat的集合的数据
                        refresh();
                        isFirst = false;
                    }
                // 通知适配器，数据发生改变
                notifyChange();
            }
        }
    }

    //创建环信用户
    private class ConnectToHuanXin extends AsyncTask<Void, String, Void> {
        private String username, password;

        public ConnectToHuanXin(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                EMChatManager.getInstance().createAccountOnServer(username, password);
            } catch (final EaseMobException e) {
                Log.e(LOG_TAG, "EaseMob createAccountOnServer() exception caught: " + e.toString());
                final int errorCode = e.getErrorCode();
                if (errorCode == EMError.NONETWORK_ERROR) {
                    publishProgress("Network is not available");
                } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                    Log.d(LOG_TAG, "EaseMob createAccountOnServer() user already exists.");
                } else {
                    publishProgress("Unable to login to the chat client");
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null)
                Toast.makeText(getActivity(), values[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new LoginToHuanXin(username, password).execute();
        }
    }

    //登录环信
    private class LoginToHuanXin extends AsyncTask<Void, String, Void> {
        private String username, password;

        public LoginToHuanXin(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Void doInBackground(Void... params) {
            EMChatManager.getInstance().login(username, password, new EMCallBack() {
                @Override
                public void onSuccess() {
                    try {
                        EMGroupManager.getInstance().getGroupsFromServer();
                        EMGroupManager.getInstance().loadAllGroups();
                        EMChatManager.getInstance().loadAllConversations();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }

                @Override
                public void onError(int i, String s) {
                    Log.e(LOG_TAG, "EaseMob login Error: " + i + "; " + s);
                }

                @Override
                public void onProgress(int i, String s) {
                }
            });
            return null;
        }
    }

    //获取用户评论
    private class LoadUserReferences extends AsyncTask<String, String, Boolean> {
        private Intent intent;

        public LoadUserReferences(Intent mIntent) {
            intent = mIntent;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            referenceList.clear();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = false;
            try {
                referenceList.addAll(ServerHelper.getInstance().getUsersReferences(
                        CacheHelper.getInstance().getSelfInfo().getId()));
                success = true;
                for (int i = 0, j = referenceList.size(); i < j; i++) {
                    try {
                        User user = ServerHelper.getInstance().getUserInfo(referenceList.get(i).getUserSrcId());
                        CacheHelper.getInstance().addUserInfo(user);
                    } catch (Exception e2) {
                        Log.e(LOG_TAG, "Inner Exception caught: " + e2.toString());
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception caught: " + e.toString());
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (isAdded()) {
                if (success) {
                    intent.putParcelableArrayListExtra(UserInfoFragment.REFERENCES, referenceList);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.fail_get_reference), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}