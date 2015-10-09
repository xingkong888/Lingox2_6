package cn.lingox.android.app;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.chat.EMMessage;

import java.util.ArrayList;

import cn.lingox.android.activity.MainActivity;
import cn.lingox.android.easemob.DemoHXSDKHelper;
import cn.lingox.android.entity.Indent;
import cn.lingox.android.entity.PathTags;
import cn.lingox.android.entity.location.Country1;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ImageHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.TimeHelper;
import cn.lingox.android.service.NotificationService;
import cn.lingox.android.utils.CommonUtils;

public class LingoXApplication extends Application {

    public static final String PACKAGE_NAME = "cn.lingox.android";
    private static final int NOTIFICATION_ID = 11;
    private static final String LOG_TAG = "LingoXApplication";
    public static DemoHXSDKHelper hxSDKHelper = new DemoHXSDKHelper();
    // Constants
    private static LingoXApplication instance;
    private static PathTags pathTag = null;
    private static ArrayList<PathTags> datas = new ArrayList<>();
    protected NotificationManager notificationManager;
    private boolean isSkip = false;//标识应用是否为跳过注册进入true 跳过 false正常登录
    private int width = 0;//屏幕宽度
    private int pathPageCount = 0;//活动数据总页数
    private int pathsCount = 0;//每页中活动的条数

    private int userPageCount = 1;//用户数据总页数
    private int usersCount = 0;//每页中的用户条数

    private ArrayList<Country1> countryDatas = null;

    private PackageManager manager;
    private PackageInfo info = null;
    private String verNum = "";

    private String location = "";

    public static LingoXApplication getInstance() {
        return instance;
    }

    private void getAllTag() {
        for (int i = 0; i < JsonHelper.getInstance().getAllTags().size(); i++) {
            pathTag = new PathTags();
            pathTag.setTag(JsonHelper.getInstance().getAllTags().get(i));
            pathTag.setType(0);
            datas.add(pathTag);
        }
    }

    public String getAppVersion() {
        if (verNum.isEmpty()) {
            getVersion();
        }
        return verNum;
    }

    private void getVersion() {
        manager = this.getPackageManager();
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
            verNum = info.versionName.replace("Beta Ver. ", "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getLocation(String country, String province, String city) {
        readLocation(country, province, city);
        return location;
    }

    private void readLocation(String country, String province, String city) {
        if (!country.isEmpty()) {
            if (!province.isEmpty()) {
                if (!city.isEmpty()) {
                    location = country.trim() + ", " +
                            province.trim() + ", " + city.trim();
                } else {
                    location = country.trim() + ", " + province.trim();
                }
            } else {
                if (city.isEmpty()) {
                    location = country.trim();
                } else {
                    location = country.trim() + ",  " + city.trim();
                }
            }
        }
    }

    public ArrayList<Country1> getCountryDatas() {
        if (countryDatas == null) {
            countryDatas = new ArrayList<>();
            countryDatas.addAll(JsonHelper.getInstance().getCountries());
        } else {
            return countryDatas;
        }
        return countryDatas;
    }

    public ArrayList<PathTags> getDatas() {
        return datas;
    }

    public boolean getSkip() {
        return isSkip;
    }

    public void setSkip(boolean isSkip) {
        this.isSkip = isSkip;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setPathPageCount(int pathPageCount) {
        this.pathPageCount = pathPageCount;
    }

    public int getUserPageCount() {
        return userPageCount;
    }

    public void setUserPageCount(int userPageCount) {
        this.userPageCount = userPageCount;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
//        SDKInitializer.initialize(instance);
        CacheHelper.getInstance().setContext(getApplicationContext());
        ImageHelper.getInstance().setContext(getApplicationContext());
        JsonHelper.getInstance().setContext(getApplicationContext());
        TimeHelper.getInstance().setContext(getApplicationContext());
        hxSDKHelper.onInit(getApplicationContext());
        Intent servceIntent = new Intent(getApplicationContext(), NotificationService.class);
        startService(servceIntent);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        getAllTag();
    }

    public void notifyNewMessage(EMMessage message) {
        //如果是设置了不提醒只显示数目的群组(这个是app里保存这个数据的，demo里不做判断)
        //以及设置了setShowNotificationInbackgroup:false(设为false后，后台时sdk也发送广播)
        try {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(getApplicationInfo().icon)
                    .setWhen(System.currentTimeMillis()).setAutoCancel(true);

            String ticker = CommonUtils.getMessageDigest(message, this);
            String st = "[expression]";
            if (message.getType() == EMMessage.Type.TXT)
                ticker = ticker.replaceAll("\\[.{2,3}\\]", st);
            //设置状态栏提示
            mBuilder.setTicker(message.getFrom() + ": " + ticker);

            //必须设置pendingintent，否则在2.3的机器上会有bug
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, intent, PendingIntent.FLAG_ONE_SHOT);
            mBuilder.setContentIntent(pendingIntent);

            Notification notification = mBuilder.build();
            notificationManager.notify(NOTIFICATION_ID, notification);
            notificationManager.cancel(NOTIFICATION_ID);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    /**
     * @param cancel 拒绝
     * @param layout 拒绝和同意的布局
     * @param state  状态
     * @param indent
     */
    public void setIndent(
            TextView cancel, LinearLayout layout, TextView state,
            TextView timeAndNum, TextView pathTitle, Indent indent) {
        if (indent.getState() == 1) {
            if (indent.getTarId().contentEquals(CacheHelper.getInstance().getSelfInfo().getId())) {
                layout.setVisibility(View.VISIBLE);
            } else {
                cancel.setVisibility(View.VISIBLE);
            }
        } else {
            layout.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
        }
        state.getPaint().setFlags(0);//取消设置
        switch (indent.getState()) {
            case 1:
                if (indent.getUserId().contentEquals(CacheHelper.getInstance().getSelfInfo().getId())) {
                    state.setText("Waiting for confirm");//待处理
                } else {
                    state.setText("Received an application");//待处理
                }
                break;
            case 2:
                state.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                state.setTextColor(Color.rgb(199, 199, 199));
                state.setText("Application cancelled");//申请这个取消
                break;
            case 3:
                state.setTextColor(Color.rgb(0, 131, 143));
                state.setText("Application Accepted");//同意
                break;
            case 4:
                state.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                state.setTextColor(Color.rgb(199, 199, 199));
                state.setText("Application declined");//被拒绝
                break;
            case 5:
                state.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                state.setTextColor(Color.rgb(199, 199, 199));
                state.setText("Time out");//超过结束时间
                break;
        }
        if (indent.getFreeTime().isEmpty()) {
            timeAndNum.setText(TimeHelper.getInstance().parseTimestampToDate(indent.getStartTime())
                            + "—" + TimeHelper.getInstance().parseTimestampToDate(indent.getEndTime()) + ", " + indent.getParticipants()
                            + " people"
            );
        } else {
            timeAndNum.setText(indent.getFreeTime()
            );
        }
        pathTitle.setText(indent.getPathTitle());
    }

    public void logout(final EMCallBack emCallBack) {
        hxSDKHelper.logout(emCallBack);
    }

}

