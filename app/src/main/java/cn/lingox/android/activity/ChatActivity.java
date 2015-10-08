package cn.lingox.android.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.GroupReomveListener;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.LocationMessageBody;
import com.easemob.chat.NormalFileMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VideoMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;
import com.easemob.util.PathUtil;
import com.easemob.util.VoiceRecorder;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cn.lingox.android.R;
import cn.lingox.android.adapter.ExpressionAdapter;
import cn.lingox.android.adapter.ExpressionPagerAdapter;
import cn.lingox.android.adapter.MessageAdapter;
import cn.lingox.android.adapter.VoicePlayClickListener;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.Indent;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.utils.CommonUtils;
import cn.lingox.android.utils.ImageUtils;
import cn.lingox.android.utils.SmileUtils;
import cn.lingox.android.widget.ExpandGridView;
import cn.lingox.android.widget.PasteEditText;

public class ChatActivity extends BaseActivity implements OnClickListener {
    public static final int REQUEST_CODE_CONTEXT_MENU = 3;
    public static final int REQUEST_CODE_TEXT = 5;
    public static final int REQUEST_CODE_VOICE = 6;
    public static final int REQUEST_CODE_PICTURE = 7;
    public static final int REQUEST_CODE_LOCATION = 8;
    public static final int REQUEST_CODE_NET_DISK = 9;
    public static final int REQUEST_CODE_FILE = 10;
    public static final int REQUEST_CODE_COPY_AND_PASTE = 11;
    public static final int REQUEST_CODE_PICK_VIDEO = 12;
    public static final int REQUEST_CODE_DOWNLOAD_VIDEO = 13;
    public static final int REQUEST_CODE_VIDEO = 14;
    public static final int REQUEST_CODE_DOWNLOAD_VOICE = 15;
    public static final int REQUEST_CODE_SELECT_USER_CARD = 16;
    public static final int REQUEST_CODE_SEND_USER_CARD = 17;
    public static final int REQUEST_CODE_CAMERA = 18;
    public static final int REQUEST_CODE_LOCAL = 19;
    public static final int REQUEST_CODE_CLICK_DESTORY_IMG = 20;
    public static final int REQUEST_CODE_GROUP_DETAIL = 21;
    public static final int REQUEST_CODE_SELECT_VIDEO = 23;
    public static final int REQUEST_CODE_SELECT_FILE = 24;
    public static final int REQUEST_CODE_ADD_TO_BLACKLIST = 25;
    public static final int RESULT_CODE_COPY = 1;
    public static final int RESULT_CODE_DELETE = 2;
    public static final int RESULT_CODE_FORWARD = 3;
    public static final int RESULT_CODE_OPEN = 4;
    public static final int RESULT_CODE_DWONLOAD = 5;
    public static final int RESULT_CODE_TO_CLOUD = 6;
    public static final int RESULT_CODE_EXIT_GROUP = 7;
    public static final int CHATTYPE_SINGLE = 1;
    public static final int CHATTYPE_GROUP = 2;
    public static final String COPY_IMAGE = "EASEMOBIMG";
    private static final String LOG_TAG = "ChatActivity";
    private static final int REQUEST_CODE_EMPTY_HISTORY = 2;
    private static final int REQUEST_CODE_MAP = 4;
    public static ChatActivity activityInstance = null;
    static int resendPos;
    private final int pagesize = 20;
    private View recordingContainer;
    private ImageView micImage;
    private TextView recordingHint;
    private ListView listView;
    private PasteEditText mEditTextContent;
    private View buttonSetModeKeyboard;
    private View buttonSetModeVoice;
    private View buttonSend;
    private View buttonPressToSpeak;
    private ViewPager expressionViewpager;
    private LinearLayout expressionContainer;
    private LinearLayout btnContainer;
    private View more;
    private ClipboardManager clipboard;
    private InputMethodManager manager;
    private List<String> reslist;
    private Drawable[] micImages;
    private int chatType;
    private EMConversation conversation;
    private NewMessageBroadcastReceiver receiver;
    private String toChatUsername;
    private VoiceRecorder voiceRecorder;
    private MessageAdapter adapter;
    private File cameraFile;
    private GroupListener groupListener;
    private ImageView iv_emoticons_normal;
    private ImageView iv_emoticons_checked;
    private RelativeLayout edittext_layout;
    private ProgressBar loadmorePB;
    private boolean isloading;
    private boolean haveMoreData = true;
    private Button btnMore;

    private String tarId = "";

    private RelativeLayout layoutShowOne;
    private LinearLayout declineAndAccept;
    private TextView state, timeAndNum, pathTitle, cancel, decline, accept, showWaiting;

    private String title;//由于编辑后后台无返回数据

    private Handler micImageHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            micImage.setImageDrawable(micImages[msg.what]);
        }
    };
    private BroadcastReceiver ackMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msgid = intent.getStringExtra("msgid");
            String from = intent.getStringExtra("from");
            EMConversation conversation = EMChatManager.getInstance()
                    .getConversation(from);
            if (conversation != null) {
                EMMessage msg = conversation.getMessage(msgid);
                if (msg != null) {
                    msg.isAcked = true;
                }
            }
            abortBroadcast();
            if (!tarId.isEmpty()) {
                new GetMessage().execute(tarId);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    };
    /**
     * 环信的BroadcastReceiver
     */
    private BroadcastReceiver deliveryAckMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msgid = intent.getStringExtra("msgid");
            String from = intent.getStringExtra("from");
            EMConversation conversation = EMChatManager.getInstance()
                    .getConversation(from);
            if (conversation != null) {
                // 获取message
                EMMessage msg = conversation.getMessage(msgid);
                if (msg != null) {
                    msg.isDelivered = true;
                }
            }
            abortBroadcast();
            if (!tarId.isEmpty()) {
                new GetMessage().execute(tarId);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    };
    private PowerManager.WakeLock wakeLock;

    private String describe = "";
    private ArrayList<Indent> indentDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatType = getIntent().getIntExtra("chatType", CHATTYPE_SINGLE);
        if (getIntent().hasExtra("describe")) {
            describe = getIntent().getStringExtra("describe");
        }
        initView();
        setUpView();
        if (!describe.isEmpty()) {
            sendText(describe);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat_activity, menu);
        switch (chatType) {
            case CHATTYPE_SINGLE:
                menu.removeItem(R.id.container_to_group);
                break;
            case CHATTYPE_GROUP:
                menu.removeItem(R.id.container_remove);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.container_remove:
                startActivityForResult(
                        new Intent(this, AlertDialog1.class)
                                .putExtra("titleIsCancel", true)
                                .putExtra("msg", "Delete this conversation?").putExtra("cancel", true),
                        REQUEST_CODE_EMPTY_HISTORY);
                return true;
            case R.id.container_to_group:
                Toast.makeText(this, "Coming soon...", Toast.LENGTH_SHORT).show();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //订单
        layoutShowOne = (RelativeLayout) findViewById(R.id.layout_show_one);
        declineAndAccept = (LinearLayout) findViewById(R.id.layout_decline_accept);
        state = (TextView) findViewById(R.id.show_one_state);
        timeAndNum = (TextView) findViewById(R.id.show_one_start_time);
        pathTitle = (TextView) findViewById(R.id.show_one_path_title);
        cancel = (TextView) findViewById(R.id.self_cancel);
        decline = (TextView) findViewById(R.id.decline);
        accept = (TextView) findViewById(R.id.accept);
        showWaiting = (TextView) findViewById(R.id.show_state_waiting);
        showWaiting.setOnClickListener(this);
        pathTitle.setOnClickListener(this);
        cancel.setOnClickListener(this);
        decline.setOnClickListener(this);
        accept.setOnClickListener(this);

        recordingContainer = findViewById(R.id.recording_container);
        micImage = (ImageView) findViewById(R.id.mic_image);
        recordingHint = (TextView) findViewById(R.id.recording_hint);
        listView = (ListView) findViewById(R.id.list);
        mEditTextContent = (PasteEditText) findViewById(R.id.et_sendmessage);
        buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
        edittext_layout = (RelativeLayout) findViewById(R.id.edittext_layout);
        buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
        buttonSend = findViewById(R.id.btn_send);
        buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
        expressionViewpager = (ViewPager) findViewById(R.id.vPager);
        expressionContainer = (LinearLayout) findViewById(R.id.ll_face_container);
        btnContainer = (LinearLayout) findViewById(R.id.ll_btn_container);
        iv_emoticons_normal = (ImageView) findViewById(R.id.iv_emoticons_normal);
        iv_emoticons_checked = (ImageView) findViewById(R.id.iv_emoticons_checked);
        loadmorePB = (ProgressBar) findViewById(R.id.pb_load_more);
        btnMore = (Button) findViewById(R.id.btn_more);
        iv_emoticons_normal.setVisibility(View.VISIBLE);
        iv_emoticons_checked.setVisibility(View.INVISIBLE);
        more = findViewById(R.id.more);
        edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);

        micImages = new Drawable[]{
                getResources().getDrawable(R.drawable.record_animate_01),
                getResources().getDrawable(R.drawable.record_animate_02),
                getResources().getDrawable(R.drawable.record_animate_03),
                getResources().getDrawable(R.drawable.record_animate_04),
                getResources().getDrawable(R.drawable.record_animate_05),
                getResources().getDrawable(R.drawable.record_animate_06),
                getResources().getDrawable(R.drawable.record_animate_07),
                getResources().getDrawable(R.drawable.record_animate_08),
                getResources().getDrawable(R.drawable.record_animate_09),
                getResources().getDrawable(R.drawable.record_animate_10),
                getResources().getDrawable(R.drawable.record_animate_11),
                getResources().getDrawable(R.drawable.record_animate_12),
                getResources().getDrawable(R.drawable.record_animate_13),
                getResources().getDrawable(R.drawable.record_animate_14),};
        reslist = getExpressionRes(35);
        List<View> views = new ArrayList<>();
        View gv1 = getGridChildView(1);
        View gv2 = getGridChildView(2);
        views.add(gv1);
        views.add(gv2);
        expressionViewpager.setAdapter(new ExpressionPagerAdapter(views));
        edittext_layout.requestFocus();
        voiceRecorder = new VoiceRecorder(micImageHandler);
        buttonPressToSpeak.setOnTouchListener(new PressToSpeakListen());
        mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edittext_layout
                            .setBackgroundResource(R.drawable.input_bar_bg_active);
                } else {
                    edittext_layout
                            .setBackgroundResource(R.drawable.input_bar_bg_normal);
                }
            }
        });
        mEditTextContent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                edittext_layout
                        .setBackgroundResource(R.drawable.input_bar_bg_active);
                more.setVisibility(View.GONE);
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.INVISIBLE);
                expressionContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.GONE);
            }
        });
        mEditTextContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!TextUtils.isEmpty(s)) {
                    btnMore.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                } else {
                    btnMore.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setUpView() {
        activityInstance = this;
        iv_emoticons_normal.setOnClickListener(this);
        iv_emoticons_checked.setOnClickListener(this);
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");
        if (chatType == CHATTYPE_SINGLE) {
            toChatUsername = getIntent().getStringExtra("username");
            User toChatUser = CacheHelper.getInstance().getUserInfoFromUsername(toChatUsername);
            getSupportActionBar().setTitle(toChatUser.getNickname());
            tarId = toChatUser.getId();
        } else {
            toChatUsername = getIntent().getStringExtra("groupId");
            EMGroup group = EMGroupManager.getInstance().getGroup(toChatUsername);
            getSupportActionBar().setTitle(group.getGroupName());
        }
        conversation = EMChatManager.getInstance().getConversation(
                toChatUsername);
        conversation.resetUnsetMsgCount();
        adapter = new MessageAdapter(this, toChatUsername, chatType);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new ListScrollListener());
        int count = listView.getCount();
        if (count > 0) {
            listView.setSelection(count - 1);
        }

        listView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                more.setVisibility(View.GONE);
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.INVISIBLE);
                expressionContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.GONE);
                return false;
            }
        });
        receiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EMChatManager
                .getInstance().getNewMessageBroadcastAction());
        intentFilter.setPriority(5);
        registerReceiver(receiver, intentFilter);

        IntentFilter ackMessageIntentFilter = new IntentFilter(EMChatManager
                .getInstance().getAckMessageBroadcastAction());
        ackMessageIntentFilter.setPriority(5);
        registerReceiver(ackMessageReceiver, ackMessageIntentFilter);

        IntentFilter deliveryAckMessageIntentFilter = new IntentFilter(
                EMChatManager.getInstance()
                        .getDeliveryAckMessageBroadcastAction());
        deliveryAckMessageIntentFilter.setPriority(5);
        registerReceiver(deliveryAckMessageReceiver,
                deliveryAckMessageIntentFilter);

        groupListener = new GroupListener();
        EMGroupManager.getInstance().addGroupChangeListener(groupListener);

        // show forward message if the message is not null
        String forward_msg_id = getIntent().getStringExtra("forward_msg_id");
        if (forward_msg_id != null) {
            forwardMessage(forward_msg_id);
        }
    }

    protected void forwardMessage(String forward_msg_id) {
        EMMessage forward_msg = EMChatManager.getInstance().getMessage(
                forward_msg_id);
        EMMessage.Type type = forward_msg.getType();
        switch (type) {
            case TXT:
                String content = ((TextMessageBody) forward_msg.getBody())
                        .getMessage();
                sendText(content);
                break;
            case IMAGE:
                String filePath = ((ImageMessageBody) forward_msg.getBody())
                        .getLocalUrl();
                if (filePath != null) {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        filePath = ImageUtils.getThumbnailImagePath(filePath);
                    }
                    sendPicture(filePath);
                }
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CODE_EXIT_GROUP) {
            setResult(RESULT_OK);
            finish();
            return;
        }
        if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
            switch (resultCode) {
                case RESULT_CODE_COPY:
                    EMMessage copyMsg = adapter.getItem(data.getIntExtra(StringConstant.msgPosition, -1));
                    if (copyMsg.getType() == EMMessage.Type.IMAGE) {
                        ImageMessageBody imageBody = (ImageMessageBody) copyMsg
                                .getBody();
                        clipboard.setText(COPY_IMAGE + imageBody.getLocalUrl());
                    } else {
                        clipboard.setText(((TextMessageBody) copyMsg.getBody())
                                .getMessage());
                    }
                    break;
                case RESULT_CODE_DELETE:
                    EMMessage deleteMsg = adapter.getItem(data.getIntExtra(StringConstant.msgPosition, -1));
                    conversation.removeMessage(deleteMsg.getMsgId());
                    adapter.refresh();
                    listView.setSelection(data.getIntExtra("position",
                            adapter.getCount()) - 1);
                    break;
            }
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_EMPTY_HISTORY) {
                EMChatManager.getInstance().clearConversation(toChatUsername);
                adapter.refresh();
            } else if (requestCode == REQUEST_CODE_CAMERA) {
                if (cameraFile != null && cameraFile.exists())
                    sendPicture(cameraFile.getAbsolutePath());
            } else if (requestCode == REQUEST_CODE_SELECT_VIDEO) {

                int duration = data.getIntExtra("dur", 0);
                String videoPath = data.getStringExtra("path");
                File file = new File(PathUtil.getInstance().getImagePath(),
                        "thvideo" + System.currentTimeMillis());
                Bitmap bitmap = null;
                FileOutputStream fos = null;
                try {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, 3);
                    if (bitmap == null) {
                        EMLog.d("chatactivity",
                                "problem load video thumbnail bitmap,use default icon");
                        bitmap = BitmapFactory.decodeResource(getResources(),
                                R.drawable.app_panel_video_icon);
                    }
                    fos = new FileOutputStream(file);
                    bitmap.compress(CompressFormat.JPEG, 100, fos);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                }
                sendVideo(videoPath, file.getAbsolutePath(), duration / 1000);
            } else if (requestCode == REQUEST_CODE_LOCAL) {
                if (data != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        sendPicByUri(selectedImage);
                    }
                }
            } else if (requestCode == REQUEST_CODE_SELECT_FILE) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        sendFile(uri);
                    }
                }
            } else if (requestCode == REQUEST_CODE_MAP) {
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                String locationAddress = data.getStringExtra("address");
                if (locationAddress != null && !locationAddress.equals("")) {
                    more(more);
                    sendLocationMsg(latitude, longitude, "", locationAddress);
                } else {
                    Toast.makeText(this, getString(R.string.location_error), Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_CODE_TEXT) {
                resendMessage();
            } else if (requestCode == REQUEST_CODE_VOICE) {
                resendMessage();
            } else if (requestCode == REQUEST_CODE_PICTURE) {
                resendMessage();
            } else if (requestCode == REQUEST_CODE_LOCATION) {
                resendMessage();
            } else if (requestCode == REQUEST_CODE_VIDEO
                    || requestCode == REQUEST_CODE_FILE) {
                resendMessage();
            } else if (requestCode == REQUEST_CODE_COPY_AND_PASTE) {
                // ճ��
                if (!TextUtils.isEmpty(clipboard.getText())) {
                    String pasteText = clipboard.getText().toString();
                    if (pasteText.startsWith(COPY_IMAGE)) {
                        sendPicture(pasteText.replace(COPY_IMAGE, ""));
                    }
                }
            } else if (requestCode == REQUEST_CODE_ADD_TO_BLACKLIST) {
                EMMessage deleteMsg = adapter.getItem(data
                        .getIntExtra("position", -1));
                addUserToBlacklist(deleteMsg.getFrom());
            } else if (conversation.getMsgCount() > 0) {
                adapter.refresh();
                setResult(RESULT_OK);
            } else if (requestCode == REQUEST_CODE_GROUP_DETAIL) {
                adapter.refresh();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                String s = mEditTextContent.getText().toString();
                sendText(s);
                break;
            case R.id.btn_take_picture:
                selectPicFromCamera();
                break;
            case R.id.btn_picture:
                selectPicFromLocal();
                break;
            case R.id.iv_emoticons_normal:
                more.setVisibility(View.VISIBLE);
                iv_emoticons_normal.setVisibility(View.INVISIBLE);
                iv_emoticons_checked.setVisibility(View.VISIBLE);
                btnContainer.setVisibility(View.GONE);
                expressionContainer.setVisibility(View.VISIBLE);
                hideKeyboard();
                break;
            case R.id.iv_emoticons_checked:
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.INVISIBLE);
                btnContainer.setVisibility(View.VISIBLE);
                expressionContainer.setVisibility(View.GONE);
                more.setVisibility(View.GONE);
                break;
            case R.id.btn_video:
                Intent intent = new Intent(ChatActivity.this, ImageGridActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
                break;
            case R.id.btn_file:
                selectFileFromLocal();
                break;
            case R.id.btn_voice_call:
                if (!EMChatManager.getInstance().isConnected())
                    Toast.makeText(this, "The chat client is not connected", Toast.LENGTH_SHORT).show();
                else
                    startActivity(new Intent(ChatActivity.this,
                            VoiceCallActivity.class).putExtra("username",
                            toChatUsername).putExtra("isComingCall", false));
                break;
            case R.id.self_cancel:
                final Indent indent = indentDatas.get(0);
                final EditText edit = new EditText(this);
                new AlertDialog.Builder(this)
                        .setMessage("Please fill out the reason")
                        .setView(edit)
                        .setPositiveButton("COMMIT", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("id", indent.getId());
                                map.put("state", "2");
                                map.put("reason", edit.getText().toString());
                                new EditIndent().execute(map);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create().show();
                break;
            case R.id.decline:
                Indent indent1 = indentDatas.get(0);
                HashMap<String, String> map = new HashMap<>();
                map.put("id", indent1.getId());
                map.put("state", "4");
                new EditIndent().execute(map);
                break;
            case R.id.accept:
                Indent indent2 = indentDatas.get(0);
                HashMap<String, String> map1 = new HashMap<>();
                map1.put("id", indent2.getId());
                map1.put("state", "3");
                new EditIndent().execute(map1);
                break;
            case R.id.show_state_waiting:
                Intent intent1 = new Intent(this, ShowMoreIndentActivity.class);
                intent1.putExtra("Indents", indentDatas);
                intent1.putExtra("username",
                        CacheHelper.getInstance().getUserInfoFromUsername(toChatUsername).getNickname());
                startActivity(intent1);
                break;
            case R.id.show_one_path_title:
                Intent intent2 = new Intent(this, PathViewActivity.class);
                intent2.putExtra(PathViewActivity.PATH_TO_VIEW_ID, indentDatas.get(0).getPathId());
                startActivity(intent2);
                break;
        }
    }

    public void selectPicFromCamera() {
        if (!CommonUtils.isExitsSdcard()) {
            Toast.makeText(getApplicationContext(), getString(R.string.sd_card_does_not_exist), Toast.LENGTH_SHORT).show();
            return;
        }

        cameraFile = new File(PathUtil.getInstance().getImagePath(),
                CacheHelper.getInstance().getSelfInfo().getUsername()
                        + System.currentTimeMillis() + ".jpg");
        cameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                        MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                REQUEST_CODE_CAMERA);
    }

    private void selectFileFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }

    public void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }

    private void sendText(String content) {
        if (content.length() > 0) {
            EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
            if (chatType == CHATTYPE_GROUP)
                message.setChatType(ChatType.GroupChat);
            TextMessageBody txtBody = new TextMessageBody(content);
            message.addBody(txtBody);
            message.setReceipt(toChatUsername);
            conversation.addMessage(message);
            adapter.refresh();
            listView.setSelection(listView.getCount() - 1);
            mEditTextContent.setText("");

            setResult(RESULT_OK);
        }
    }

    private void sendVoice(String filePath, String fileName, String length,
                           boolean isResend) {
        if (!(new File(filePath).exists())) {
            return;
        }
        try {
            final EMMessage message = EMMessage
                    .createSendMessage(EMMessage.Type.VOICE);
            if (chatType == CHATTYPE_GROUP)
                message.setChatType(ChatType.GroupChat);
            message.setReceipt(toChatUsername);
            int len = Integer.parseInt(length);
            VoiceMessageBody body = new VoiceMessageBody(new File(filePath),
                    len);
            message.addBody(body);

            conversation.addMessage(message);
            adapter.refresh();
            listView.setSelection(listView.getCount() - 1);
            setResult(RESULT_OK);
            // send file
            // sendVoiceSub(filePath, fileName, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPicture(final String filePath) {
        String to = toChatUsername;
        // create and add image message in view
        final EMMessage message = EMMessage
                .createSendMessage(EMMessage.Type.IMAGE);
        if (chatType == CHATTYPE_GROUP)
            message.setChatType(ChatType.GroupChat);
        message.setReceipt(to);
        ImageMessageBody body = new ImageMessageBody(new File(filePath));
        // body.setSendOriginalImage(true);
        message.addBody(body);
        conversation.addMessage(message);

        listView.setAdapter(adapter);
        adapter.refresh();
        listView.setSelection(listView.getCount() - 1);
        setResult(RESULT_OK);
        // more(more);
    }

    private void sendVideo(final String filePath, final String thumbPath,
                           final int length) {
        final File videoFile = new File(filePath);
        if (!videoFile.exists()) {
            return;
        }
        try {
            EMMessage message = EMMessage
                    .createSendMessage(EMMessage.Type.VIDEO);
            if (chatType == CHATTYPE_GROUP)
                message.setChatType(ChatType.GroupChat);
            String to = toChatUsername;
            message.setReceipt(to);
            VideoMessageBody body = new VideoMessageBody(videoFile, thumbPath,
                    length, videoFile.length());
            message.addBody(body);
            conversation.addMessage(message);
            listView.setAdapter(adapter);
            adapter.refresh();
            listView.setSelection(listView.getCount() - 1);
            setResult(RESULT_OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPicByUri(Uri selectedImage) {
        Cursor cursor = getContentResolver().query(selectedImage, null, null,
                null, null);
        String picError = getString(R.string.pic_error);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex("_data");
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            if (picturePath == null || picturePath.equals("null")) {
                Toast toast = Toast.makeText(this, picError, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendPicture(picturePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Toast toast = Toast.makeText(this, picError, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendPicture(file.getAbsolutePath());
        }
    }

    private void sendLocationMsg(double latitude, double longitude,
                                 String imagePath, String locationAddress) {
        EMMessage message = EMMessage
                .createSendMessage(EMMessage.Type.LOCATION);
        if (chatType == CHATTYPE_GROUP)
            message.setChatType(ChatType.GroupChat);
        LocationMessageBody locBody = new LocationMessageBody(locationAddress,
                latitude, longitude);
        message.addBody(locBody);
        message.setReceipt(toChatUsername);
        conversation.addMessage(message);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setSelection(listView.getCount() - 1);
        setResult(RESULT_OK);
    }

    private void sendFile(Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, projection, null,
                        null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        if (filePath == null) {
            Log.e(LOG_TAG, "sendFile(): filePath was null");
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(getApplicationContext(), "Error finding file", Toast.LENGTH_SHORT).show();
            return;
        }
        if (file.length() > 10 * 1024 * 1024) {
            Toast.makeText(getApplicationContext(), "File is too large (max size: 10MB)", Toast.LENGTH_SHORT).show();
            return;
        }

        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.FILE);
        if (chatType == CHATTYPE_GROUP)
            message.setChatType(ChatType.GroupChat);

        message.setReceipt(toChatUsername);
        NormalFileMessageBody body = new NormalFileMessageBody(new File(
                filePath));
        message.addBody(body);

        conversation.addMessage(message);
        listView.setAdapter(adapter);
        adapter.refresh();
        listView.setSelection(listView.getCount() - 1);
        setResult(RESULT_OK);
    }

    private void resendMessage() {
        EMMessage msg;
        msg = conversation.getMessage(resendPos);
        msg.status = EMMessage.Status.CREATE;

        adapter.refresh();
        listView.setSelection(resendPos);
    }

    public void setModeVoice(View view) {
        hideKeyboard();
        edittext_layout.setVisibility(View.GONE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeKeyboard.setVisibility(View.VISIBLE);
        buttonSend.setVisibility(View.GONE);
        btnMore.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.VISIBLE);
        iv_emoticons_normal.setVisibility(View.VISIBLE);
        iv_emoticons_checked.setVisibility(View.INVISIBLE);
        btnContainer.setVisibility(View.VISIBLE);
        expressionContainer.setVisibility(View.GONE);
    }

    public void setModeKeyboard(View view) {
        edittext_layout.setVisibility(View.VISIBLE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.VISIBLE);
        mEditTextContent.requestFocus();
        buttonPressToSpeak.setVisibility(View.GONE);
        if (TextUtils.isEmpty(mEditTextContent.getText())) {
            btnMore.setVisibility(View.VISIBLE);
            buttonSend.setVisibility(View.GONE);
        } else {
            btnMore.setVisibility(View.GONE);
            buttonSend.setVisibility(View.VISIBLE);
        }
    }

    public void more(View view) {
        if (more.getVisibility() == View.GONE) {
            hideKeyboard();
            more.setVisibility(View.VISIBLE);
            btnContainer.setVisibility(View.VISIBLE);
            expressionContainer.setVisibility(View.GONE);
        } else {
            if (expressionContainer.getVisibility() == View.VISIBLE) {
                expressionContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.VISIBLE);
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.INVISIBLE);
            } else {
                more.setVisibility(View.GONE);
            }
        }
    }

    public void editClick(View v) {
        listView.setSelection(listView.getCount() - 1);
        if (more.getVisibility() == View.VISIBLE) {
            more.setVisibility(View.GONE);
            iv_emoticons_normal.setVisibility(View.VISIBLE);
            iv_emoticons_checked.setVisibility(View.INVISIBLE);
        }
    }

    private View getGridChildView(int i) {
        View view = View.inflate(this, R.layout.expression_gridview, null);
        ExpandGridView gv = (ExpandGridView) view.findViewById(R.id.gridview);
        List<String> list = new ArrayList<>();
        if (i == 1) {
            List<String> list1 = reslist.subList(0, 20);
            list.addAll(list1);
        } else if (i == 2) {
            list.addAll(reslist.subList(20, reslist.size()));
        }
        list.add("delete_expression");
        final ExpressionAdapter expressionAdapter = new ExpressionAdapter(this,
                1, list);
        gv.setAdapter(expressionAdapter);
        gv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String filename = expressionAdapter.getItem(position);
                try {
                    if (buttonSetModeKeyboard.getVisibility() != View.VISIBLE) {
                        if (!filename.equals("delete_expression")) {
                            Class clz = Class
                                    .forName(LingoXApplication.PACKAGE_NAME + ".utils.SmileUtils");
                            Field field = clz.getField(filename);
                            mEditTextContent.append(SmileUtils.getSmiledText(
                                    ChatActivity.this, (String) field.get(null)));
                        } else {
                            if (!TextUtils.isEmpty(mEditTextContent.getText())) {

                                int selectionStart = mEditTextContent
                                        .getSelectionStart();
                                if (selectionStart > 0) {
                                    String body = mEditTextContent.getText()
                                            .toString();
                                    String tempStr = body.substring(0,
                                            selectionStart);
                                    int i = tempStr.lastIndexOf("[");
                                    if (i != -1) {
                                        CharSequence cs = tempStr.substring(i,
                                                selectionStart);
                                        if (SmileUtils.containsKey(cs
                                                .toString()))
                                            mEditTextContent.getEditableText()
                                                    .delete(i, selectionStart);
                                        else
                                            mEditTextContent.getEditableText()
                                                    .delete(selectionStart - 1,
                                                            selectionStart);
                                    } else {
                                        mEditTextContent.getEditableText()
                                                .delete(selectionStart - 1,
                                                        selectionStart);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }
        });
        return view;
    }

    public List<String> getExpressionRes(int getSum) {
        List<String> reslist = new ArrayList<>();
        for (int x = 1; x <= getSum; x++) {
            String filename = "ee_" + x;
            reslist.add(filename);
        }
        return reslist;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityInstance = null;
        EMGroupManager.getInstance().removeGroupChangeListener(groupListener);
        try {
            unregisterReceiver(receiver);
            receiver = null;
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
        try {
            unregisterReceiver(ackMessageReceiver);
            ackMessageReceiver = null;
            unregisterReceiver(deliveryAckMessageReceiver);
            deliveryAckMessageReceiver = null;
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart("ChatActivity");
        super.onResume();
        if (!tarId.isEmpty()) {
            new GetMessage().execute(tarId);
        }
        adapter.refresh();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd("ChatActivity");
        super.onPause();
        if (wakeLock.isHeld())
            wakeLock.release();
        if (VoicePlayClickListener.isPlaying
                && VoicePlayClickListener.currentPlayListener != null) {
            VoicePlayClickListener.currentPlayListener.stopPlayVoice();
        }
        try {
            if (voiceRecorder.isRecording()) {
                voiceRecorder.discardRecording();
                recordingContainer.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    private void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void addUserToBlacklist(String username) {
        try {
            EMContactManager.getInstance().addUserToBlackList(username, true);
            Toast.makeText(getApplicationContext(), getString(R.string.user_blocked), Toast.LENGTH_SHORT).show();
        } catch (EaseMobException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getString(R.string.error_block), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (more.getVisibility() == View.VISIBLE) {
            more.setVisibility(View.GONE);
            iv_emoticons_normal.setVisibility(View.VISIBLE);
            iv_emoticons_checked.setVisibility(View.INVISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String username = intent.getStringExtra("username");
        if (toChatUsername.equals(username))
            super.onNewIntent(intent);
        else {
            startActivity(intent);
            finish();
        }
    }

    public String getToChatUsername() {
        return toChatUsername;
    }

    private void setIndent(Indent indent) {
        LingoXApplication.getInstance().setIndent(
                cancel, declineAndAccept, state, timeAndNum, pathTitle, indent
        );
    }

    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String username = intent.getStringExtra("from");
            String msgid = intent.getStringExtra("msgid");
            EMMessage message = EMChatManager.getInstance().getMessage(msgid);
            if (message.getChatType() == ChatType.GroupChat) {
                username = message.getTo();
            }
            if (!username.equals(toChatUsername)) {
                return;
            }
            adapter.refresh();
            listView.setSelection(listView.getCount() - 1);
            abortBroadcast();
        }
    }

    /**
     * ��ס˵��listener
     */
    class PressToSpeakListen implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!CommonUtils.isExitsSdcard()) {
                        Toast.makeText(ChatActivity.this, "����������Ҫsdcard֧�֣�",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    try {
                        v.setPressed(true);
                        wakeLock.acquire();
                        if (VoicePlayClickListener.isPlaying)
                            VoicePlayClickListener.currentPlayListener
                                    .stopPlayVoice();
                        recordingContainer.setVisibility(View.VISIBLE);
                        recordingHint
                                .setText(getString(R.string.move_up_to_cancel));
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                        voiceRecorder.startRecording(null, toChatUsername,
                                getApplicationContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                        v.setPressed(false);
                        if (wakeLock.isHeld())
                            wakeLock.release();
                        if (voiceRecorder != null)
                            voiceRecorder.discardRecording();
                        recordingContainer.setVisibility(View.INVISIBLE);
                        Toast.makeText(ChatActivity.this, R.string.recoding_fail,
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        recordingHint
                                .setText(getString(R.string.release_to_cancel));
                        recordingHint
                                .setBackgroundResource(R.drawable.recording_text_hint_bg);
                    } else {
                        recordingHint
                                .setText(getString(R.string.move_up_to_cancel));
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    v.setPressed(false);
                    recordingContainer.setVisibility(View.INVISIBLE);
                    if (wakeLock.isHeld())
                        wakeLock.release();
                    if (event.getY() < 0) {
                        // discard the recorded audio.
                        voiceRecorder.discardRecording();
                    } else {
                        // stop recording and send voice file
                        try {
                            int length = voiceRecorder.stopRecoding();
                            if (length > 0) {
                                sendVoice(voiceRecorder.getVoiceFilePath(),
                                        voiceRecorder
                                                .getVoiceFileName(toChatUsername),
                                        Integer.toString(length), false);
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.the_recording_time_is_too_short), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ChatActivity.this, getString(R.string.send_fail), Toast.LENGTH_SHORT).show();
                        }

                    }
                    return true;
                default:
                    recordingContainer.setVisibility(View.INVISIBLE);
                    if (voiceRecorder != null)
                        voiceRecorder.discardRecording();
                    return false;
            }
        }
    }

    private class ListScrollListener implements OnScrollListener {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:
                    if (view.getFirstVisiblePosition() == 0 && !isloading
                            && haveMoreData) {
                        loadmorePB.setVisibility(View.VISIBLE);
                        List<EMMessage> messages;
                        try {
                            if (chatType == CHATTYPE_SINGLE)
                                messages = conversation.loadMoreMsgFromDB(adapter
                                        .getItem(0).getMsgId(), pagesize);
                            else
                                messages = conversation.loadMoreGroupMsgFromDB(
                                        adapter.getItem(0).getMsgId(), pagesize);
                        } catch (Exception e1) {
                            loadmorePB.setVisibility(View.GONE);
                            return;
                        }
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            Log.e(LOG_TAG, e.toString());
                        }
                        if (messages.size() != 0) {
                            adapter.notifyDataSetChanged();
                            listView.setSelection(messages.size() - 1);
                            if (messages.size() != pagesize)
                                haveMoreData = false;
                        } else {
                            haveMoreData = false;
                        }
                        loadmorePB.setVisibility(View.GONE);
                        isloading = false;
                    }
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

        }
    }

    class GroupListener extends GroupReomveListener {

        @Override
        public void onUserRemoved(final String groupId, String groupName) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (toChatUsername.equals(groupId)) {
                        Toast.makeText(ChatActivity.this, getString(R.string.you_are_group), Toast.LENGTH_LONG)
                                .show();
                        finish();
                    }
                }
            });
        }

        @Override
        public void onGroupDestroy(final String groupId, String groupName) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (toChatUsername.equals(groupId)) {
                        Toast.makeText(ChatActivity.this, getString(R.string.the_current_group), Toast.LENGTH_LONG)
                                .show();
                        finish();
                    }
                }
            });
        }
    }

    private class GetMessage extends AsyncTask<String, Void, Boolean> {
        HashMap<String, String> map = new HashMap<>();
        ArrayList<Indent> tempData = new ArrayList<>();
        private String userId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            indentDatas.clear();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            userId = params[0];
            //通过username获取每个用户的订单数据
            map.put("tarId", CacheHelper.getInstance().getSelfInfo().getId());
            map.put("userId", userId);
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
                e1.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            adapter.notifyDataSetChanged();
            if (success) {
                switch (indentDatas.size()) {
                    case 0:
                        break;
                    case 1:
                        layoutShowOne.setVisibility(View.VISIBLE);
                        title = indentDatas.get(0).getPathTitle();
                        setIndent(indentDatas.get(0));
                        break;
                    default:
                        showWaiting.setVisibility(View.VISIBLE);
                        showWaiting.setText(indentDatas.size() + " applications");
                        break;
                }
            } else {
                Toast.makeText(ChatActivity.this, "Edit failure", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class EditIndent extends AsyncTask<HashMap<String, String>, Void, Boolean> {
        private Indent indent;
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(ChatActivity.this);
            pd.setMessage("Submiting...");
            pd.show();
        }

        @Override
        protected Boolean doInBackground(HashMap<String, String>... params) {
            try {

                indent = ServerHelper.getInstance().editApplication(params[0]);
                indent.setPathTitle(title);
                return true;
            } catch (final Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.setMessage("Failure" + e.getMessage());
                    }
                });
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            pd.dismiss();
            if (aBoolean) {
                setIndent(indent);
            }
        }
    }
}