/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.lingox.android.easemob;

import android.content.Intent;
import android.content.IntentFilter;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chat.OnMessageNotifyListener;
import com.easemob.chat.OnNotificationClickListener;

import cn.lingox.android.activity.ChatActivity;
import cn.lingox.android.activity.MainActivity;
import cn.lingox.android.receiver.VoiceCallReceiver;
import cn.lingox.android.utils.CommonUtils;


public class DemoHXSDKHelper extends HXSDKHelper {

    @Override
    protected void initHXOptions() {
        super.initHXOptions();
    }

    @Override
    protected OnMessageNotifyListener getMessageNotifyListener() {
        return new OnMessageNotifyListener() {

            @Override
            public String onNewMessageNotify(EMMessage message) {
                String ticker = CommonUtils.getMessageDigest(message, appContext);
                if (message.getType() == Type.TXT)
                    ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
                return message.getFrom() + ": " + ticker;
            }

            @Override
            public String onLatestMessageNotify(EMMessage message, int fromUsersNum, int messageNum) {
                return null;
            }

            @Override
            public String onSetNotificationTitle(EMMessage message) {
                return null;
            }

            @Override
            public int onSetSmallIcon(EMMessage message) {
                return 0;
            }
        };
    }

    @Override
    protected OnNotificationClickListener getNotificationClickListener() {
        return new OnNotificationClickListener() {

            @Override
            public Intent onNotificationClick(EMMessage message) {
                Intent intent = new Intent(appContext, ChatActivity.class);
                ChatType chatType = message.getChatType();
                if (chatType == ChatType.Chat) {
                    intent.putExtra("username", message.getFrom());
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                } else {
                    intent.putExtra("groupId", message.getTo());
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                }
                return intent;
            }
        };
    }

    @Override
    protected void onConnectionConflict() {
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("conflict", true);
        appContext.startActivity(intent);
    }

    @Override
    protected void initListener() {
        super.initListener();
        IntentFilter callFilter = new IntentFilter(EMChatManager.getInstance().getIncomingVoiceCallBroadcastAction());
        appContext.registerReceiver(new VoiceCallReceiver(), callFilter);
    }

    @Override
    public void logout(final EMCallBack callback) {
        super.logout(new EMCallBack() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(int code, String message) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgress(int progress, String status) {
                // TODO Auto-generated method stub
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }

        });
    }
}
