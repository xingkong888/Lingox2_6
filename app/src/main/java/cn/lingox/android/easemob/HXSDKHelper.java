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

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.easemob.EMCallBack;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.OnMessageNotifyListener;
import com.easemob.chat.OnNotificationClickListener;

import java.util.Iterator;
import java.util.List;

import cn.lingox.android.helper.CacheHelper;

public abstract class HXSDKHelper {
    private static final String TAG = "HXSDKHelper";
    private static HXSDKHelper me = null;
    protected Context appContext = null;
    protected EMConnectionListener connectionListener = null;
    private boolean sdkInited = false;

    public HXSDKHelper() {
        me = this;
    }

    public static HXSDKHelper getInstance() {
        return me;
    }

    public synchronized boolean onInit(Context context) {
        if (sdkInited) {
            return true;
        }

        appContext = context;
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);

        if (processAppName == null || !processAppName.equalsIgnoreCase("cn.lingox.android")) {
            Log.e(TAG, "enter the service process!");

            return false;
        }

        EMChat.getInstance().init(context);
        EMChat.getInstance().setDebugMode(false);

        Log.d(TAG, "initialize EMChat SDK");

        initHXOptions();
        initListener();
        sdkInited = true;
        return true;
    }

    /**
     * please make sure you have to get EMChatOptions by following method and
     * set related options EMChatOptions options =
     * EMChatManager.getInstance().getChatOptions();
     */
    protected void initHXOptions() {
        Log.d(TAG, "init HuanXin Options");

        EMChatOptions options = EMChatManager.getInstance().getChatOptions();
        options.setAcceptInvitationAlways(false);
        options.setUseRoster(false);
        options.setNotifyBySoundAndVibrate(CacheHelper.getInstance().getSettingMsgNotification());
        options.setNoticeBySound(CacheHelper.getInstance().getSettingMsgSound());
        options.setNoticedByVibrate(CacheHelper.getInstance().getSettingMsgVibrate());
        options.setShowNotificationInBackgroud(true);
        options.setUseSpeaker(CacheHelper.getInstance().getSettingMsgSpeaker());
        options.setRequireAck(true);
        options.setRequireDeliveryAck(false);
        options.setOnNotificationClickListener(getNotificationClickListener());
        options.setNotifyText(getMessageNotifyListener());
    }

    /**
     * logout HuanXin SDK
     */
    public void logout(final EMCallBack callback) {
        EMChatManager.getInstance().logout(new EMCallBack() {

            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onProgress(int progress, String status) {
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }
        });
    }

    protected OnMessageNotifyListener getMessageNotifyListener() {
        return null;
    }

    protected OnNotificationClickListener getNotificationClickListener() {
        return null;
    }

    protected void initListener() {
        Log.d(TAG, "init listener");

        // create the global connection listener
        connectionListener = new EMConnectionListener() {
            @Override
            public void onDisconnected(int error) {
                if (error == EMError.CONNECTION_CONFLICT) {
                    onConnectionConflict();
                } else {
                    onConnectionDisconnected(error);
                }
            }

            @Override
            public void onConnected() {
                onConnectionConnected();
            }
        };
        EMChatManager.getInstance().addConnectionListener(connectionListener);
    }

    protected void onConnectionConflict() {
    }

    protected void onConnectionConnected() {
    }

    protected void onConnectionDisconnected(int error) {
    }

    /**
     * check the application process name if process name is not qualified, then
     * we think it is a service process and we will not init SDK
     *
     * @param pID
     * @return
     */
    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) appContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = appContext.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i
                    .next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm
                            .getApplicationInfo(info.processName,
                                    PackageManager.GET_META_DATA));
                    // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +"  Label: "+c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }
}
