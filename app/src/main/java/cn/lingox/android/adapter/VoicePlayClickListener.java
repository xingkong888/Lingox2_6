package cn.lingox.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.chat.EMChatDB;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.VoiceMessageBody;

import java.io.File;

import cn.lingox.android.R;

public class VoicePlayClickListener implements View.OnClickListener {

    public static boolean isPlaying = false;
    public static VoicePlayClickListener currentPlayListener = null;
    static EMMessage currentMessage = null;
    EMMessage message;
    VoiceMessageBody voiceBody;
    ImageView voiceIconView;
    MediaPlayer mediaPlayer = null;
    ImageView iv_read_status;
    Activity activity;
    private AnimationDrawable voiceAnimation = null;
    private String username;
    private ChatType chatType;
    private BaseAdapter adapter;


    /**
     * @param message
     * @param v
     * @param iv_read_status
     * @param activity
     */
    public VoicePlayClickListener(EMMessage message, ImageView v, ImageView iv_read_status, BaseAdapter adapter, Activity activity,
                                  String username) {
        this.message = message;
        voiceBody = (VoiceMessageBody) message.getBody();
        this.iv_read_status = iv_read_status;
        this.adapter = adapter;
        voiceIconView = v;
        this.activity = activity;
        this.username = username;
        this.chatType = message.getChatType();
    }

    public void stopPlayVoice() {
        voiceAnimation.stop();
        if (message.direct == EMMessage.Direct.RECEIVE) {
            voiceIconView.setImageResource(R.drawable.chatfrom_voice_playing);
        } else {
            voiceIconView.setImageResource(R.drawable.chatto_voice_playing);
        }
        // stop play voice
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = false;
    }

    public void playVoice(String filePath) {
        if (!(new File(filePath).exists())) {
            return;
        }
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

        mediaPlayer = new MediaPlayer();
        if (EMChatManager.getInstance().getChatOptions().getUseSpeaker()) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        } else {
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    stopPlayVoice(); // stop animation
                }

            });
            isPlaying = true;
            currentPlayListener = this;
            currentMessage = message;
            mediaPlayer.start();
            showAnimation();
            try {
                if (!message.isAcked && message.direct == EMMessage.Direct.RECEIVE) {
                    message.isAcked = true;
                    if (iv_read_status != null && iv_read_status.getVisibility() == View.VISIBLE) {
                        iv_read_status.setVisibility(View.INVISIBLE);
                        EMChatDB.getInstance().updateMessageAck(message.getMsgId(), true);
                    }
                    if (chatType != ChatType.GroupChat)
                        EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                }
            } catch (Exception e) {
                message.isAcked = false;
            }
        } catch (Exception e) {
        }
    }

    // show the voice playing animation
    private void showAnimation() {
        // play voice, and start animation
        if (message.direct == EMMessage.Direct.RECEIVE) {
            //noinspection ResourceType
            voiceIconView.setImageResource(R.anim.voice_from_icon);
        } else {
            //noinspection ResourceType
            voiceIconView.setImageResource(R.anim.voice_to_icon);
        }
        voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
        voiceAnimation.start();
    }

    @Override
    public void onClick(View v) {

        if (isPlaying) {
            currentPlayListener.stopPlayVoice();
            if (currentMessage != null && currentMessage.hashCode() == message.hashCode()) {
                currentMessage = null;
                return;
            }
        }

        if (message.direct == EMMessage.Direct.SEND) {
            // for sent msg, we will try to play the voice file directly
            playVoice(voiceBody.getLocalUrl());
        } else {

            if (message.status == EMMessage.Status.SUCCESS) {
                File file = new File(voiceBody.getLocalUrl());
                if (file.exists() && file.isFile())
                    playVoice(voiceBody.getLocalUrl());
                else
                    System.err.println("file not exist");

            } else if (message.status == EMMessage.Status.INPROGRESS) {
                // TODO English
                Toast.makeText(activity, "...", Toast.LENGTH_SHORT).show();


            } else if (message.status == EMMessage.Status.FAIL) {
                // TODO English
                Toast.makeText(activity, "...", Toast.LENGTH_SHORT).show();
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        EMChatManager.getInstance().asyncFetchMessage(message);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                        adapter.notifyDataSetChanged();
                    }

                }.execute();
            }
        }
    }

    interface OnVoiceStopListener {
        void onStop();

        void onStart();
    }
}