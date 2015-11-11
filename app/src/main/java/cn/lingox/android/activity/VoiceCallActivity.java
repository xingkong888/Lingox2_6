package cn.lingox.android.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMCallStateChangeListener;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EMServiceNotReadyException;
import com.umeng.analytics.MobclickAgent;

import java.util.UUID;

import cn.lingox.android.Constant;
import cn.lingox.android.R;

public class VoiceCallActivity extends BaseActivity implements OnClickListener {
    private String msgid;
    private LinearLayout comingBtnContainer;
    private Button hangupBtn;
    private ImageView muteImage;
    private ImageView handsFreeImage;
    private boolean isMuteState;
    private boolean isHandsfreeState;
    private boolean isInComingCall;
    private TextView callStateTextView;
    private SoundPool soundPool;
    private int streamID;
    private boolean endCallTriggerByMe = false;
    private Handler handler = new Handler();
    private Ringtone ringtone;
    private int outgoing;
    private AudioManager audioManager;
    private Chronometer chronometer;
    private String callDruationText;
    private String username;
    private CallingState callingState = CallingState.CANCED;
    private boolean isAnswered;
    private LinearLayout voiceContronlLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        comingBtnContainer = (LinearLayout) findViewById(R.id.ll_coming_call);
        Button refuseBtn = (Button) findViewById(R.id.btn_refuse_call);
        Button answerBtn = (Button) findViewById(R.id.btn_answer_call);
        hangupBtn = (Button) findViewById(R.id.btn_hangup_call);
        muteImage = (ImageView) findViewById(R.id.iv_mute);
        handsFreeImage = (ImageView) findViewById(R.id.iv_handsfree);
        callStateTextView = (TextView) findViewById(R.id.tv_call_state);
        TextView nickTextView = (TextView) findViewById(R.id.tv_nick);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        voiceContronlLayout = (LinearLayout) findViewById(R.id.ll_voice_control);

        refuseBtn.setOnClickListener(this);
        answerBtn.setOnClickListener(this);
        hangupBtn.setOnClickListener(this);
        muteImage.setOnClickListener(this);
        handsFreeImage.setOnClickListener(this);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMicrophoneMute(false);

        addCallStateListener();
        msgid = UUID.randomUUID().toString();

        username = getIntent().getStringExtra("username");
        isInComingCall = getIntent().getBooleanExtra("isComingCall", false);

        nickTextView.setText(username);
        if (!isInComingCall) {// ����绰
            soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
            outgoing = soundPool.load(this, R.raw.outgoing, 1);

            comingBtnContainer.setVisibility(View.INVISIBLE);
            hangupBtn.setVisibility(View.VISIBLE);
            // TODO English
            callStateTextView.setText("...");
            handler.postDelayed(new Runnable() {
                public void run() {
                    streamID = playMakeCallSounds();
                }
            }, 300);
            try {
                EMChatManager.getInstance().makeVoiceCall(username);
            } catch (EMServiceNotReadyException e) {
                e.printStackTrace();
                // TODO English
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(VoiceCallActivity.this, "...", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            voiceContronlLayout.setVisibility(View.INVISIBLE);
            Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            audioManager.setMode(AudioManager.MODE_RINGTONE);
            audioManager.setSpeakerphoneOn(true);
            ringtone = RingtoneManager.getRingtone(this, ringUri);
            ringtone.play();
        }
    }

    void addCallStateListener() {
        EMChatManager.getInstance().addVoiceCallStateChangeListener(new EMCallStateChangeListener() {

            @Override
            public void onCallStateChanged(CallState callState, CallError error) {
                // Message msg = handler.obtainMessage();
                switch (callState) {

                    case CONNECTING:
                        // TODO English
                        VoiceCallActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                callStateTextView.setText("...");
                            }

                        });
                        break;
                    case CONNECTED:
                        // TODO English
                        VoiceCallActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                callStateTextView.setText("...");
                            }

                        });
                        break;

                    case ACCEPTED:
                        VoiceCallActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    if (soundPool != null)
                                        soundPool.stop(streamID);
                                } catch (Exception e) {
                                }
                                closeSpeakerOn();
                                chronometer.setVisibility(View.VISIBLE);
                                chronometer.setBase(SystemClock.elapsedRealtime());
                                chronometer.start();
                                // TODO English
                                callStateTextView.setText("...");
                                callingState = CallingState.NORMAL;
                            }

                        });
                        break;
                    case DISCONNNECTED:
                        final CallError fError = error;
                        VoiceCallActivity.this.runOnUiThread(new Runnable() {
                            private void postDelayedCloseMsg() {
                                handler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        saveCallRecord();
                                        Animation animation = new AlphaAnimation(1.0f, 0.0f);
                                        animation.setDuration(800);
                                        findViewById(R.id.root_layout).startAnimation(animation);
                                        finish();
                                    }

                                }, 200);
                            }

                            @Override
                            public void run() {
                                chronometer.stop();
                                callDruationText = chronometer.getText().toString();

                                if (fError == CallError.REJECTED) {
                                    callingState = CallingState.BEREFUESD;
                                    // TODO English
                                    callStateTextView.setText("...");
                                } else if (fError == CallError.ERROR_TRANSPORT) {
                                    // TODO English
                                    callStateTextView.setText("...");
                                } else if (fError == CallError.ERROR_INAVAILABLE) {
                                    callingState = CallingState.OFFLINE;
                                    // TODO English
                                    callStateTextView.setText("...");
                                } else if (fError == CallError.ERROR_BUSY) {
                                    callingState = CallingState.BUSY;
                                    // TODO English
                                    callStateTextView.setText("...");
                                } else if (fError == CallError.ERROR_NORESPONSE) {
                                    callingState = CallingState.NORESPONSE;
                                    // TODO English
                                    callStateTextView.setText("...");
                                } else {
                                    if (isAnswered) {
                                        callingState = CallingState.NORMAL;
                                        if (endCallTriggerByMe) {
                                            // TODO English
                                            callStateTextView.setText("...");
                                        } else {
                                            // TODO English
                                            callStateTextView.setText("...");
                                        }
                                    } else {
                                        if (isInComingCall) {
                                            callingState = CallingState.UNANSWERED;
                                            // TODO English
                                            callStateTextView.setText("...");
                                        } else {
                                            callingState = CallingState.CANCED;
                                            // TODO English
                                            callStateTextView.setText("...");
                                        }
                                    }
                                }
                                postDelayedCloseMsg();
                            }

                        });

                        break;

                    default:
                        break;
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_refuse_call:
                if (ringtone != null)
                    ringtone.stop();
                try {
                    EMChatManager.getInstance().rejectCall();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    saveCallRecord();
                    finish();
                }
                callingState = CallingState.REFUESD;
                break;

            case R.id.btn_answer_call:
                comingBtnContainer.setVisibility(View.INVISIBLE);
                hangupBtn.setVisibility(View.VISIBLE);
                voiceContronlLayout.setVisibility(View.VISIBLE);
                if (ringtone != null)
                    ringtone.stop();
                closeSpeakerOn();
                if (isInComingCall) {
                    try {
                        isAnswered = true;
                        EMChatManager.getInstance().answerCall();
                    } catch (Exception e) {
                        e.printStackTrace();
                        saveCallRecord();
                        finish();
                    }
                }
                break;

            case R.id.btn_hangup_call:
                if (soundPool != null)
                    soundPool.stop(streamID);
                endCallTriggerByMe = true;
                try {
                    EMChatManager.getInstance().endCall();
                } catch (Exception e) {
                    e.printStackTrace();
                    saveCallRecord();
                    finish();
                }
                break;

            case R.id.iv_mute:
                if (isMuteState) {
                    muteImage.setImageResource(R.drawable.icon_mute_normal);
                    audioManager.setMicrophoneMute(false);
                    isMuteState = false;
                } else {
                    muteImage.setImageResource(R.drawable.icon_mute_on);
                    audioManager.setMicrophoneMute(true);
                    isMuteState = true;
                }
                break;
            case R.id.iv_handsfree:
                if (isHandsfreeState) {
                    handsFreeImage.setImageResource(R.drawable.icon_speaker_normal);
                    closeSpeakerOn();
                    isHandsfreeState = false;
                } else {
                    handsFreeImage.setImageResource(R.drawable.icon_speaker_on);
                    openSpeakerOn();
                    isHandsfreeState = true;
                }
                break;
            default:
                break;
        }
    }

    private int playMakeCallSounds() {
        try {
            float audioMaxVolumn = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            float audioCurrentVolumn = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            float volumnRatio = audioCurrentVolumn / audioMaxVolumn;

            audioManager.setMode(AudioManager.MODE_RINGTONE);
            audioManager.setSpeakerphoneOn(false);

            int id = soundPool.play(outgoing,
                    volumnRatio, volumnRatio,
                    1, -1, 1);
            return id;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    protected void onDestroy() {
        if (soundPool != null) {
            soundPool.release();
        }
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        audioManager.setMode(AudioManager.MODE_NORMAL);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        EMChatManager.getInstance().endCall();
        callDruationText = chronometer.getText().toString();
        saveCallRecord();
        finish();
    }

    public void openSpeakerOn() {
        try {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            if (!audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(true);
            }
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeSpeakerOn() {

        try {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                if (audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(false);
                }
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveCallRecord() {
        EMMessage message;
        TextMessageBody txtBody;
        if (!isInComingCall) {
            message = EMMessage.createSendMessage(EMMessage.Type.TXT);
            message.setReceipt(username);
        } else {
            message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            message.setFrom(username);
        }

        switch (callingState) {
            case NORMAL:
                // TODO English
                txtBody = new TextMessageBody("... " + callDruationText);
                break;
            case REFUESD:
                // TODO English
                txtBody = new TextMessageBody("...");
                break;
            case BEREFUESD:
                // TODO English
                txtBody = new TextMessageBody("...");
                break;
            case OFFLINE:
                // TODO English
                txtBody = new TextMessageBody("...");
                break;
            case BUSY:
                // TODO English
                txtBody = new TextMessageBody("...");
                break;
            case NORESPONSE:
                // TODO English
                txtBody = new TextMessageBody("...");
                break;
            case UNANSWERED:
                // TODO English
                txtBody = new TextMessageBody("...");
                break;
            default:
                // TODO English
                txtBody = new TextMessageBody("...");
                break;
        }
        message.setAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, true);

        message.addBody(txtBody);
        message.setMsgId(msgid);

        EMChatManager.getInstance().saveMessage(message, false);
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    enum CallingState {
        CANCED, NORMAL, REFUESD, BEREFUESD, UNANSWERED, OFFLINE, NORESPONSE, BUSY
    }
}
