package cn.lingox.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.easemob.chat.EMChatManager;

import cn.lingox.android.activity.VoiceCallActivity;

public class VoiceCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(EMChatManager.getInstance().getIncomingVoiceCallBroadcastAction()))
            return;

        String from = intent.getStringExtra("from");
        context.startActivity(new Intent(context, VoiceCallActivity.class).
                putExtra("username", from).putExtra("isComingCall", true).
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

}
