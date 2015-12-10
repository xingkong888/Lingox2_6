package cn.lingox.android.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.FileMessageBody;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.LocationMessageBody;
import com.easemob.chat.NormalFileMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VideoMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.DateUtils;
import com.easemob.util.EMLog;
import com.easemob.util.FileUtils;
import com.easemob.util.LatLng;
import com.easemob.util.TextFormater;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.lingox.android.Constant;
import cn.lingox.android.R;
import cn.lingox.android.activity.AlertDialog1;
import cn.lingox.android.activity.ChatActivity;
import cn.lingox.android.activity.ContextMenu;
import cn.lingox.android.activity.ShowBigImage;
import cn.lingox.android.activity.ShowNormalFileActivity;
import cn.lingox.android.activity.ShowVideoActivity;
import cn.lingox.android.activity.UserInfoActivity;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ImageHelper;
import cn.lingox.android.task.LoadImageTask;
import cn.lingox.android.task.LoadVideoImageTask;
import cn.lingox.android.utils.ImageCache;
import cn.lingox.android.utils.ImageUtils;
import cn.lingox.android.utils.SmileUtils;

/**
 * 消息适配器
 */
public class MessageAdapter extends BaseAdapter {

    public static final String IMAGE_DIR = "chat/image/";
    private final static String TAG = "msg";
    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
    private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
    private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
    private static final int MESSAGE_TYPE_SENT_FILE = 10;
    private static final int MESSAGE_TYPE_RECV_FILE = 11;
    private static final int MESSAGE_TYPE_SENT_VOICE_CALL = 12;
    private static final int MESSAGE_TYPE_RECV_VOICE_CALL = 13;
    private String username;
    private LayoutInflater inflater;
    private Activity activity;
    private Context context;

    // reference to conversation object in chatsdk
    private EMConversation conversation;

    private Map<String, Timer> timers = new Hashtable<>();

    public MessageAdapter(Context context, String username) {
        this.context = context;
        this.username = username;
        this.inflater = LayoutInflater.from(context);
        this.activity = (Activity) context;
        this.conversation = EMChatManager.getInstance().getConversation(username);
    }

    public int getCount() {
        return conversation.getMsgCount();
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    @Override
    public EMMessage getItem(int position) {
        return conversation.getMessage(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        EMMessage message = conversation.getMessage(position);

        if (message.getType() == EMMessage.Type.TXT) {
            if (!message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_TXT : MESSAGE_TYPE_SENT_TXT;
            }
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE_CALL : MESSAGE_TYPE_SENT_VOICE_CALL;
        }
        if (message.getType() == EMMessage.Type.IMAGE) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_IMAGE : MESSAGE_TYPE_SENT_IMAGE;
        }
        if (message.getType() == EMMessage.Type.LOCATION) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_LOCATION : MESSAGE_TYPE_SENT_LOCATION;
        }
        if (message.getType() == EMMessage.Type.VOICE) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE : MESSAGE_TYPE_SENT_VOICE;
        }
        if (message.getType() == EMMessage.Type.VIDEO) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO : MESSAGE_TYPE_SENT_VIDEO;
        }
        if (message.getType() == EMMessage.Type.FILE) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_FILE : MESSAGE_TYPE_SENT_FILE;
        }
        return -1;// invalid
    }

    public int getViewTypeCount() {
        return 14;
    }

    /**
     * 根据不同的消息类型，创建不同的布局
     *
     * @param message 消息
     * @return view
     */
    @SuppressLint("InflateParams")
    private View createViewByMessage(EMMessage message) {
        switch (message.getType()) {
            case LOCATION:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater
                        .inflate(R.layout.row_received_location, null) : inflater
                        .inflate(R.layout.row_sent_location, null);

            case IMAGE:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater
                        .inflate(R.layout.row_received_picture, null) : inflater
                        .inflate(R.layout.row_sent_picture, null);

            case VOICE:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater
                        .inflate(R.layout.row_received_voice, null) : inflater
                        .inflate(R.layout.row_sent_voice, null);

            case VIDEO:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater
                        .inflate(R.layout.row_received_video, null) : inflater
                        .inflate(R.layout.row_sent_video, null);

            case FILE:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater
                        .inflate(R.layout.row_received_file, null) : inflater
                        .inflate(R.layout.row_sent_file, null);

            default:
                if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    return message.direct == EMMessage.Direct.RECEIVE
                            ? inflater.inflate(R.layout.row_received_voice_call, null)
                            : inflater.inflate(R.layout.row_sent_voice_call, null);
                }
                return message.direct == EMMessage.Direct.RECEIVE
                        ? inflater.inflate(R.layout.row_received_message, null)
                        : inflater.inflate(R.layout.row_sent_message, null);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final EMMessage message = getItem(position);
        ChatType chatType = message.getChatType();    // Single or Group
        final User user = CacheHelper.getInstance().getUserInfoFromUsername(chatType == ChatType.Chat ? username : message.getFrom());
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createViewByMessage(message);

            if (message.getType() == EMMessage.Type.IMAGE) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.iv_sendPicture));
                    holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                    switch (message.direct) {
                        case RECEIVE:
                            ImageHelper.getInstance().loadAvatar(holder.head_iv, user.getAvatar());
                            break;
                        case SEND:
                            ImageHelper.getInstance().loadAvatar(holder.head_iv, CacheHelper.getInstance().getSelfInfo().getAvatar());
                            break;
                    }
                } catch (Exception e) {
                    Log.e("MessageAdapter", "Error populating ViewHolder");
                }
            } else if (message.getType() == EMMessage.Type.TXT) {
                try {
                    holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                    if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                        holder.iv = (ImageView) convertView.findViewById(R.id.iv_call_icon);
                    }
                    switch (message.direct) {
                        case RECEIVE:
                            ImageHelper.getInstance().loadAvatar(holder.head_iv, user.getAvatar());
                            holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                            break;
                        case SEND:
                            ImageHelper.getInstance().loadAvatar(holder.head_iv, CacheHelper.getInstance().getSelfInfo().getAvatar());
                            holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                            holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                            break;
                        default:
                            throw new Exception("Neither SEND nor RECEIVE");
                    }
                } catch (Exception e) {
                    Log.e("MessageAdapter", "Error populating ViewHolder");
                }
            } else if (message.getType() == EMMessage.Type.VOICE) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.iv_voice));
                    holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_length);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    switch (message.direct) {
                        case RECEIVE:
                            ImageHelper.getInstance().loadAvatar(holder.head_iv, user.getAvatar());
                            holder.iv_read_status = (ImageView) convertView.findViewById(R.id.iv_unread_voice);
                            holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                            break;
                        case SEND:
                            ImageHelper.getInstance().loadAvatar(holder.head_iv, CacheHelper.getInstance().getSelfInfo().getAvatar());
                            holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                            break;
                    }

                } catch (Exception e) {
                    Log.e("MessageAdapter", "Error populating ViewHolder");
                }
            } else if (message.getType() == EMMessage.Type.LOCATION) {
                try {
                    holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_location);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                    switch (message.direct) {
                        case RECEIVE:
                            ImageHelper.getInstance().loadAvatar(holder.head_iv, user.getAvatar());
                            break;
                        case SEND:
                            ImageHelper.getInstance().loadAvatar(holder.head_iv, CacheHelper.getInstance().getSelfInfo().getAvatar());
                            break;
                    }
                } catch (Exception e) {
                    Log.e("MessageAdapter", "Error populating ViewHolder");
                }
            } else if (message.getType() == EMMessage.Type.VIDEO) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.chatting_content_iv));
                    holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.size = (TextView) convertView.findViewById(R.id.chatting_size_iv);
                    holder.timeLength = (TextView) convertView.findViewById(R.id.chatting_length_iv);
                    holder.playBtn = (ImageView) convertView.findViewById(R.id.chatting_status_btn);
                    holder.container_status_btn = (LinearLayout) convertView.findViewById(R.id.container_status_btn);
                    holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                    switch (message.direct) {
                        case RECEIVE:
                            ImageHelper.getInstance().loadAvatar(holder.head_iv, user.getAvatar());
                            break;
                        case SEND:
                            ImageHelper.getInstance().loadAvatar(holder.head_iv, CacheHelper.getInstance().getSelfInfo().getAvatar());
                            break;
                    }
                } catch (Exception e) {
                    Log.e("MessageAdapter", "Error populating ViewHolder");
                }
            } else if (message.getType() == EMMessage.Type.FILE) {
                try {
                    holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv_file_name = (TextView) convertView.findViewById(R.id.tv_file_name);
                    holder.tv_file_size = (TextView) convertView.findViewById(R.id.tv_file_size);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_file_download_state = (TextView) convertView.findViewById(R.id.tv_file_state);
                    holder.ll_container = (LinearLayout) convertView.findViewById(R.id.ll_file_container);
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                    holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                    switch (message.direct) {
                        case RECEIVE:
                            ImageHelper.getInstance().loadAvatar(holder.head_iv, user.getAvatar());
                            break;
                        case SEND:
                            ImageHelper.getInstance().loadAvatar(holder.head_iv, CacheHelper.getInstance().getSelfInfo().getAvatar());
                            break;
                    }
                } catch (Exception e) {
                    Log.e("MessageAdapter", "Error populating ViewHolder");
                }
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (message.getType()) {
            case IMAGE:
                handleImageMessage(message, holder, position, convertView);
                break;
            case TXT:
                if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    handleVoiceCallMessage(message, holder, position);
                } else {
                    handleTextMessage(message, holder, position);
                }
                break;
            case LOCATION:
                handleLocationMessage(message, holder, position, convertView);
                break;
            case VOICE:
                handleVoiceMessage(message, holder, position, convertView);
                break;
            case VIDEO:
                handleVideoMessage(message, holder, position, convertView);
                break;
            case FILE:
                handleFileMessage(message, holder, position, convertView);
                break;
        }

        if (message.direct == EMMessage.Direct.SEND) {
            View statusView = convertView.findViewById(R.id.msg_status);
            statusView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(activity, AlertDialog1.class);
                    intent.putExtra("msg", activity.getString(R.string.confirm_resend));
                    intent.putExtra("title", activity.getString(R.string.resend));
                    intent.putExtra("cancel", true);
                    intent.putExtra("position", position);
                    if (message.getType() == EMMessage.Type.TXT) {
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_TEXT);
                    } else if (message.getType() == EMMessage.Type.VOICE) {
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_VOICE);
                    } else if (message.getType() == EMMessage.Type.IMAGE) {
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_PICTURE);
                    } else if (message.getType() == EMMessage.Type.LOCATION) {
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_LOCATION);
                    } else if (message.getType() == EMMessage.Type.FILE) {
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_FILE);
                    } else if (message.getType() == EMMessage.Type.VIDEO) {
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_VIDEO);
                    }
                }
            });
            holder.head_iv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String type = "chat";
                    Intent intent = new Intent(activity, UserInfoActivity.class);
                    intent.putExtra(UserInfoActivity.INTENT_USER_ID, CacheHelper.getInstance().getSelfInfo().getId());
                    intent.putExtra("chat", type);
                    activity.startActivity(intent);
                }
            });
        } else {
            holder.head_iv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String type = "chat";
                    Intent intent = new Intent(activity, UserInfoActivity.class);
                    intent.putExtra(UserInfoActivity.INTENT_USER_ID, user.getId());
                    intent.putExtra("chat", type);
                    activity.startActivity(intent);
                }
            });
        }

        TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);

        if (position == 0) {
            timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
            timestamp.setVisibility(View.VISIBLE);
        } else {
            if (DateUtils.isCloseEnough(message.getMsgTime(), conversation.getMessage(position - 1).getMsgTime())) {
                timestamp.setVisibility(View.GONE);
            } else {
                timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                timestamp.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }

    private void handleTextMessage(EMMessage message, ViewHolder holder, final int position) {
        TextMessageBody txtBody = (TextMessageBody) message.getBody();
        Spannable span = SmileUtils
                .getSmiledText(context, txtBody.getMessage());
        holder.tv.setText(span, BufferType.SPANNABLE);

        holder.tv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult((new Intent(activity, ContextMenu.class)).putExtra("position", position)
                        .putExtra("type", EMMessage.Type.TXT.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });

        if (message.direct == EMMessage.Direct.SEND) {
            switch (message.status) {
                case SUCCESS:
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.GONE);
                    break;
                case FAIL:
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.VISIBLE);
                    break;
                case INPROGRESS:
                    holder.pb.setVisibility(View.VISIBLE);
                    holder.staus_iv.setVisibility(View.GONE);
                    break;
                default:
                    sendMsgInBackground(message, holder);
                    break;
            }
        }
    }

    private void handleVoiceCallMessage(EMMessage message, ViewHolder holder, final int position) {
        TextMessageBody txtBody = (TextMessageBody) message.getBody();
        holder.tv.setText(txtBody.getMessage());
    }

    private void handleImageMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {
        holder.pb.setTag(position);
        holder.iv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult((new Intent(activity,
                                ContextMenu.class)).putExtra("position", position)
                                .putExtra("type", EMMessage.Type.IMAGE.ordinal()),
                        ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });

        if (message.direct == EMMessage.Direct.RECEIVE) {
            // "it is receive msg";
            if (message.status == EMMessage.Status.INPROGRESS) {
                // "!!!! back receive";
                holder.iv.setImageResource(R.drawable.default_image);
                showDownloadImageProgress(message, holder);
            } else {
                // "!!!! not back receive, show image directly");
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.iv.setImageResource(R.drawable.default_image);
                ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
                if (imgBody.getLocalUrl() != null) {
                    String remotePath = imgBody.getRemoteUrl();
                    String filePath = ImageUtils.getImagePath(remotePath);
                    String thumbRemoteUrl = imgBody.getThumbnailUrl();
                    String thumbnailPath = ImageUtils.getThumbnailImagePath(thumbRemoteUrl);
                    showImageView(thumbnailPath, holder.iv, filePath, imgBody.getRemoteUrl(), message);
                }
            }
            return;
        }

        // process send message
        // send pic, show the pic directly
        ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
        String filePath = imgBody.getLocalUrl();
        if (filePath != null && new File(filePath).exists()) {
            showImageView(ImageUtils.getThumbnailImagePath(filePath), holder.iv, filePath, null, message);
        } else {
            showImageView(ImageUtils.getThumbnailImagePath(filePath), holder.iv, filePath, IMAGE_DIR, message);
        }
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                holder.staus_iv.setVisibility(View.GONE);
                holder.pb.setVisibility(View.VISIBLE);
                holder.tv.setVisibility(View.VISIBLE);
                if (timers.containsKey(message.getMsgId())) {
                    return;
                }
                // set a timer
                final Timer timer = new Timer();
                timers.put(message.getMsgId(), timer);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                holder.pb.setVisibility(View.VISIBLE);
                                holder.tv.setVisibility(View.VISIBLE);
                                holder.tv.setText(message.progress + "%");
                                if (message.status == EMMessage.Status.SUCCESS) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    timer.cancel();
                                } else if (message.status == EMMessage.Status.FAIL) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    holder.staus_iv.setVisibility(View.VISIBLE);
                                    Toast.makeText(activity, activity.getString(R.string.send_fail) + activity
                                            .getString(R.string.connect_failuer_toast), Toast.LENGTH_LONG).show();
                                    timer.cancel();
                                }
                            }
                        });
                    }
                }, 0, 500);
                break;
            default:
                sendPictureMessage(message, holder);
        }
    }

    private void handleVideoMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {

        VideoMessageBody videoBody = (VideoMessageBody) message.getBody();
        String localThumb = videoBody.getLocalThumb();

        holder.iv.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult(new Intent(activity,
                                ContextMenu.class).putExtra("position", position)
                                .putExtra("type", EMMessage.Type.VIDEO.ordinal()),
                        ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });

        if (localThumb != null) {
            showVideoThumbView(localThumb, holder.iv, videoBody.getThumbnailUrl(), message);
        }
        if (videoBody.getLength() > 0) {
            String time = DateUtils.toTimeBySecond(videoBody.getLength());
            holder.timeLength.setText(time);
        }
        holder.playBtn.setImageResource(R.drawable.video_download_btn_nor);

        if (message.direct == EMMessage.Direct.RECEIVE) {
            if (videoBody.getVideoFileLength() > 0) {
                String size = TextFormater.getDataSize(videoBody.getVideoFileLength());
                holder.size.setText(size);
            }
        } else {
            if (videoBody.getLocalUrl() != null && new File(videoBody.getLocalUrl()).exists()) {
                String size = TextFormater.getDataSize(new File(videoBody.getLocalUrl()).length());
                holder.size.setText(size);
            }
        }

        if (message.direct == EMMessage.Direct.RECEIVE) {
            if (message.status == EMMessage.Status.INPROGRESS) {
                holder.iv.setImageResource(R.drawable.default_image);
                showDownloadImageProgress(message, holder);
            } else {
                holder.iv.setImageResource(R.drawable.default_image);
                if (localThumb != null) {
                    showVideoThumbView(localThumb, holder.iv, videoBody.getThumbnailUrl(), message);
                }
            }
            return;
        }
        holder.pb.setTag(position);
        // until here ,deal with send video msg
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                if (timers.containsKey(message.getMsgId())) {
                    return;
                }
                // set a timer
                final Timer timer = new Timer();
                timers.put(message.getMsgId(), timer);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.VISIBLE);
                                holder.tv.setVisibility(View.VISIBLE);
                                holder.tv.setText(message.progress + "%");
                                if (message.status == EMMessage.Status.SUCCESS) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    timer.cancel();
                                } else if (message.status == EMMessage.Status.FAIL) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    holder.staus_iv.setVisibility(View.VISIBLE);
                                    Toast.makeText(
                                            activity,
                                            activity.getString(R.string.send_fail)
                                                    + activity
                                                    .getString(R.string.connect_failuer_toast),
                                            Toast.LENGTH_LONG).show();
                                    timer.cancel();
                                }
                            }
                        });

                    }
                }, 0, 500);
                break;
            default:
                sendPictureMessage(message, holder);
        }
    }

    private void handleVoiceMessage(final EMMessage message,
                                    final ViewHolder holder, final int position, View convertView) {
        VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();

        holder.tv.setText(voiceBody.getLength() + "\"");
        holder.iv.setOnClickListener(new VoicePlayClickListener(message,
                holder.iv, holder.iv_read_status, this, activity));
        holder.iv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult((new Intent(activity,
                                ContextMenu.class)).putExtra("position", position)
                                .putExtra("type", EMMessage.Type.VOICE.ordinal()),
                        ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });

        if (message.direct == EMMessage.Direct.RECEIVE) {
            if (message.isAcked) {
                holder.iv_read_status.setVisibility(View.INVISIBLE);
            } else {
                holder.iv_read_status.setVisibility(View.VISIBLE);
            }
            if (message.status == EMMessage.Status.INPROGRESS) {
                holder.pb.setVisibility(View.VISIBLE);

                ((FileMessageBody) message.getBody())
                        .setDownloadCallback(new EMCallBack() {
                            @Override
                            public void onSuccess() {
                                activity.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        holder.pb.setVisibility(View.INVISIBLE);
                                        notifyDataSetChanged();
                                    }
                                });
                            }

                            @Override
                            public void onProgress(int progress, String status) {
                            }

                            @Override
                            public void onError(int code, String message) {
                                activity.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        holder.pb.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        });
            } else {
                holder.pb.setVisibility(View.INVISIBLE);
            }
            return;
        }

        // until here, deal with send voice msg
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                holder.pb.setVisibility(View.VISIBLE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            default:
                sendMsgInBackground(message, holder);
        }
    }

    private void handleFileMessage(final EMMessage message,
                                   final ViewHolder holder, int position, View convertView) {
        final NormalFileMessageBody fileMessageBody = (NormalFileMessageBody) message
                .getBody();
        final String filePath = fileMessageBody.getLocalUrl();
        holder.tv_file_name.setText(fileMessageBody.getFileName());
        holder.tv_file_size.setText(TextFormater.getDataSize(fileMessageBody
                .getFileSize()));
        holder.ll_container.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                File file = new File(filePath);
                if (file.exists()) {
                    FileUtils.openFile(file, (Activity) context);
                } else {
                    context.startActivity(new Intent(context,
                            ShowNormalFileActivity.class).putExtra("msgbody",
                            fileMessageBody));
                }
                if (message.direct == EMMessage.Direct.RECEIVE && !message.isAcked) {
                    try {
                        EMChatManager.getInstance().ackMessageRead(
                                message.getFrom(), message.getMsgId());
                        message.isAcked = true;
                    } catch (EaseMobException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        if (message.direct == EMMessage.Direct.RECEIVE) {
            File file = new File(filePath);
            if (file.exists()) {
                holder.tv_file_download_state.setText("Have downloaded");
            } else {
                holder.tv_file_download_state.setText("Did not download");
            }
            return;
        }

        // until here, deal with send voice msg
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.INVISIBLE);
                holder.tv.setVisibility(View.INVISIBLE);
                holder.staus_iv.setVisibility(View.INVISIBLE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.INVISIBLE);
                holder.tv.setVisibility(View.INVISIBLE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                if (timers.containsKey(message.getMsgId())) {
                    return;
                }
                // set a timer
                final Timer timer = new Timer();
                timers.put(message.getMsgId(), timer);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.VISIBLE);
                                holder.tv.setVisibility(View.VISIBLE);
                                holder.tv.setText(message.progress + "%");
                                if (message.status == EMMessage.Status.SUCCESS) {
                                    holder.pb.setVisibility(View.INVISIBLE);
                                    holder.tv.setVisibility(View.INVISIBLE);
                                    timer.cancel();
                                } else if (message.status == EMMessage.Status.FAIL) {
                                    holder.pb.setVisibility(View.INVISIBLE);
                                    holder.tv.setVisibility(View.INVISIBLE);
                                    holder.staus_iv.setVisibility(View.VISIBLE);
                                    Toast.makeText(
                                            activity,
                                            activity.getString(R.string.send_fail)
                                                    + activity
                                                    .getString(R.string.connect_failuer_toast),
                                            Toast.LENGTH_LONG).show();
                                    timer.cancel();
                                }
                            }
                        });

                    }
                }, 0, 500);
                break;
            default:
                sendMsgInBackground(message, holder);
        }
    }

    private void handleLocationMessage(final EMMessage message,
                                       final ViewHolder holder, final int position, View convertView) {
        TextView locationView = ((TextView) convertView
                .findViewById(R.id.tv_location));
        LocationMessageBody locBody = (LocationMessageBody) message.getBody();
        locationView.setText(locBody.getAddress());
        LatLng loc = new LatLng(locBody.getLatitude(), locBody.getLongitude());
        locationView.setOnClickListener(new MapClickListener(loc, locBody
                .getAddress()));
        locationView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult((new Intent(activity,
                                ContextMenu.class)).putExtra("position", position)
                                .putExtra("type", EMMessage.Type.LOCATION.ordinal()),
                        ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return false;
            }
        });

        if (message.direct == EMMessage.Direct.RECEIVE) {
            return;
        }
        // deal with send message
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                holder.pb.setVisibility(View.VISIBLE);
                break;
            default:
                sendMsgInBackground(message, holder);
        }
    }

    public void sendMsgInBackground(final EMMessage message,
                                    final ViewHolder holder) {
        holder.staus_iv.setVisibility(View.GONE);
        holder.pb.setVisibility(View.VISIBLE);

        EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

            @Override
            public void onSuccess() {
                updateSendedView(message, holder);
            }

            @Override
            public void onError(int code, String error) {
                updateSendedView(message, holder);
            }

            @Override
            public void onProgress(int progress, String status) {
            }
        });
    }

    private void showDownloadImageProgress(final EMMessage message, final ViewHolder holder) {
        final FileMessageBody msgbody = (FileMessageBody) message.getBody();
        holder.pb.setVisibility(View.VISIBLE);
        holder.tv.setVisibility(View.VISIBLE);
        msgbody.setDownloadCallback(new EMCallBack() {

            @Override
            public void onSuccess() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (message.getType() == EMMessage.Type.IMAGE) {
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                        }
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onProgress(final int progress, String status) {
                if (message.getType() == EMMessage.Type.IMAGE) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.tv.setText(progress + "%");
                        }
                    });
                }
            }
        });
    }

    private void sendPictureMessage(final EMMessage message, final ViewHolder holder) {
        try {
            // before send, update ui
            holder.staus_iv.setVisibility(View.GONE);
            holder.pb.setVisibility(View.VISIBLE);
            holder.tv.setVisibility(View.VISIBLE);
            holder.tv.setText("0%");
            EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

                @Override
                public void onSuccess() {
                    Log.d(TAG, "send image message successfully");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            // send success
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onError(int code, String error) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                            holder.staus_iv.setVisibility(View.VISIBLE);
                            Toast.makeText(
                                    activity,
                                    activity.getString(R.string.send_fail)
                                            + activity
                                            .getString(R.string.connect_failuer_toast),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onProgress(final int progress, String status) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            holder.tv.setText(progress + "%");
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSendedView(final EMMessage message, final ViewHolder holder) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // send success
                if (message.getType() == EMMessage.Type.VIDEO) {
                    holder.tv.setVisibility(View.GONE);
                }
//                if (message.status == EMMessage.Status.SUCCESS) {
//                  没有事情要处理，删除掉
//                } else
                if (message.status == EMMessage.Status.FAIL) {
                    Toast.makeText(
                            activity,
                            activity.getString(R.string.send_fail)
                                    + activity
                                    .getString(R.string.connect_failuer_toast),
                            Toast.LENGTH_LONG).show();
                }
                notifyDataSetChanged();
            }
        });
    }

    private boolean showImageView(final String thumbernailPath,
                                  final ImageView iv, final String localFullSizePath,
                                  String remoteDir, final EMMessage message) {
        final String remote = remoteDir;
        EMLog.d("###", "local = " + localFullSizePath + " remote: " + remote);
        // first check if the thumbnail image already loaded into cache
        Bitmap bitmap = ImageCache.getInstance().get(thumbernailPath);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);
            iv.setClickable(true);
            iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.err.println("image view on click");
                    Intent intent = new Intent(activity, ShowBigImage.class);
                    File file = new File(localFullSizePath);
                    if (file.exists()) {
                        Uri uri = Uri.fromFile(file);
                        intent.putExtra("uri", uri);
                        System.err.println("here need to check why download everyTime");
                    } else {
                        // The local full size pic does not exist yet.
                        // ShowBigImage needs to download it from the server
                        // first
                        ImageMessageBody body = (ImageMessageBody) message.getBody();
                        intent.putExtra("secret", body.getSecret());
                        intent.putExtra("remotepath", remote);
                    }
                    if (message != null
                            && message.direct == EMMessage.Direct.RECEIVE
                            && !message.isAcked
                            && message.getChatType() != ChatType.GroupChat) {
                        try {
                            EMChatManager.getInstance().ackMessageRead(
                                    message.getFrom(), message.getMsgId());
                            message.isAcked = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    activity.startActivity(intent);
                }
            });
            return true;
        } else {
            new LoadImageTask().execute(thumbernailPath, localFullSizePath,
                    remote, message.getChatType(), iv, activity, message);
            return true;
        }
    }

    private void showVideoThumbView(String localThumb, ImageView iv,
                                    String thumbnailUrl, final EMMessage message) {
        // first check if the thumbnail image already loaded into cache
        Bitmap bitmap = ImageCache.getInstance().get(localThumb);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);
            iv.setClickable(true);
            iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoMessageBody videoBody = (VideoMessageBody) message.getBody();
                    System.err.println("video view is on click");
                    Intent intent = new Intent(activity,
                            ShowVideoActivity.class);
                    intent.putExtra("localpath", videoBody.getLocalUrl());
                    intent.putExtra("secret", videoBody.getSecret());
                    intent.putExtra("remotepath", videoBody.getRemoteUrl());
//                    if (message != null  上边方法里就没问题，fack
                    if (message.direct == EMMessage.Direct.RECEIVE
                            && !message.isAcked
                            && message.getChatType() != ChatType.GroupChat) {
                        message.isAcked = true;
                        try {
                            EMChatManager.getInstance().ackMessageRead(
                                    message.getFrom(), message.getMsgId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    activity.startActivity(intent);
                }
            });
        } else {
            new LoadVideoImageTask().execute(localThumb, thumbnailUrl, iv, activity, message, this);
        }
    }

    static class ViewHolder {
        ImageView iv, staus_iv, head_iv, playBtn, iv_read_status;
        ProgressBar pb;
        LinearLayout container_status_btn, ll_container;
        TextView tv, tv_userId, timeLength, size, tv_file_name, tv_file_size, tv_file_download_state;
    }

    //获取自己的当前位置
    class MapClickListener implements OnClickListener {
        LatLng location;
        String address;

        public MapClickListener(LatLng loc, String address) {
            location = loc;
            this.address = address;
        }

        @Override
        public void onClick(View v) {
            //跳转到地图，获取当前位置
        }
    }
}