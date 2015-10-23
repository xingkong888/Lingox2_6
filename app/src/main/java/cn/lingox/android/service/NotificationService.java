package cn.lingox.android.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lingox.android.R;
import cn.lingox.android.activity.PathViewActivity;
import cn.lingox.android.activity.ReferenceActivity;
import cn.lingox.android.activity.UserInfoActivity;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.LingoNotification;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.task.GetUser;

public class NotificationService extends Service implements
        AMapLocationListener {
    public static final String NOTIFICATION = LingoXApplication.PACKAGE_NAME + ".activity";
    public static final String UPDATE = LingoXApplication.PACKAGE_NAME + ".UPDATE";
    public static final String NOTICE_CLICKED = LingoXApplication.PACKAGE_NAME + ".NOTICE_CLICKED";
    private static final String LOG_TAG = "NotificationService";
    public int type = 0;
    public int notiType = 0;
    private List<Notification> notificationList = new ArrayList<>();

    private LocationManagerProxy mLocationManagerProxy;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        mLocationManagerProxy = LocationManagerProxy.getInstance(getApplicationContext());
        //此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        //注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
        //在定位结束后，在合适的生命周期调用destroy()方法
        //其中如果间隔时间为-1，则定位只定一次
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, 60 * 1000, 15, this);
        mLocationManagerProxy.setGpsEnable(false);

        //获取通知信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        checkNotification();
                        Thread.sleep(60 * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0) {
            //获取位置信息
            Double geoLat = amapLocation.getLatitude();//纬度
            Double geoLng = amapLocation.getLongitude();//经度
            try {
                final double[] geoLocation = {geoLng, geoLat};
                final User user = CacheHelper.getInstance().getSelfInfo();
                user.setLoc(geoLocation);
                CacheHelper.getInstance().setSelfInfo(user);
                LingoXApplication.getInstance().setLocation(geoLat, geoLng);
                new Thread() {
                    public void run() {
                        Map<String, String> params = new HashMap<>();
                        params.put(StringConstant.userIdStr, CacheHelper
                                .getInstance().getSelfInfo().getId());
                        params.put(StringConstant.locStr, JsonHelper.getInstance().getLocationStr(geoLocation));
                        try {
                            ServerHelper.getInstance().updateUserInfo(params);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "onLocationChanged(): " + e.getMessage());
                        }
                    }
                }.start();
            } catch (Exception e) {
                Log.e(LOG_TAG, "LocationListener: Exception caught: " + e.getMessage());
            }
//            Toast.makeText(getApplicationContext(), geoLat + ">>>>" + geoLng, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        mLocationManagerProxy.destroy();
    }

    private void checkNotification() throws Exception {
        if (CacheHelper.getInstance().isLoggedIn()) {
            ArrayList<LingoNotification> LingoNotifications = ServerHelper.getInstance().getAllNewNotifications();
            CacheHelper.getInstance().addNotifications(LingoNotifications);
            showNotification(LingoNotifications);
            Intent broadcast = new Intent(NOTIFICATION);
            broadcast.putExtra(UPDATE, LingoNotifications.size() != 0);
            sendBroadcast(broadcast);
        }
    }

    private void showNotification(final ArrayList<LingoNotification> lingoNotifications) {
        notificationList.clear();
        if (lingoNotifications.size() != 0) {
            for (final LingoNotification lingoNotification : lingoNotifications) {
                final User notificationUser = CacheHelper.getInstance()
                        .getUserInfo(lingoNotification.getUser_src());
                if (notificationUser == null) {
                    new GetUser(lingoNotification.getUser_src(), new GetUser.Callback() {
                        @Override
                        public void onSuccess(User user) {
                            notificationList.add(
                                    generaNotification(user,
                                            makeNotifiText(user, lingoNotification), lingoNotification));
                        }

                        @Override
                        public void onFail() {

                        }
                    }).execute();
                } else
                    Log.d(LOG_TAG, notificationUser.toString());
                notificationList.add(generaNotification(notificationUser, makeNotifiText(notificationUser, lingoNotification), lingoNotification));
            }
        }
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        for (int i = 0; i < notificationList.size(); i++) {
            mNotificationManager.notify(i, notificationList.get(i));
        }
    }

    private Notification generaNotification(User user, String tickerText, LingoNotification notification) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("LingoX").setContentText(tickerText);
        switch (notiType) {
            case 1:
                Intent intent1 = new Intent(this, PathViewActivity.class);
                intent1.putExtra(PathViewActivity.PATH_TO_VIEW_ID, notification.getPath_id());
                PendingIntent pendIntent1 =
                        PendingIntent.getActivity(this, type, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pendIntent1);
                break;
            case 2:
                Intent intent2 = new Intent(this, PathViewActivity.class);
                intent2.putExtra(PathViewActivity.PATH_TO_VIEW_ID, notification.getPath_id());
                PendingIntent pendIntent2 =
                        PendingIntent.getActivity(this, type, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pendIntent2);
                break;
            case 3:
                Intent intent3 = new Intent(this, UserInfoActivity.class);
                intent3.putExtra(UserInfoActivity.INTENT_USER_ID, user.getId());
                PendingIntent pendIntent3 =
                        PendingIntent.getActivity(this, type, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pendIntent3);
                break;
            case 4:
                Intent intent4 = new Intent(this, PathViewActivity.class);
                intent4.putExtra(PathViewActivity.PATH_TO_VIEW_ID, notification.getPath_id());
                PendingIntent pendIntent4 =
                        PendingIntent.getActivity(this, type, intent4, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pendIntent4);
                break;
            case 5:
//            case 6:
                Intent intent5 = new Intent(this, ReferenceActivity.class);
                intent5.putExtra(ReferenceActivity.INTENT_TARGET_USER_ID, user.getId());
                intent5.putExtra(ReferenceActivity.INTENT_TARGET_USER_NAME, user.getNickname());
                PendingIntent pendIntent5 = PendingIntent.getActivity(
                        getApplicationContext(), type, intent5, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pendIntent5);
                break;
//            case 6://申请完成，给参加的活动添加评论
//                Intent intent6 = new Intent(this, PathReferenceActivity.class);
//                intent6.putExtra(PathReferenceActivity.PATH, notification.getPath_id());
//                PendingIntent pendIntent6 = PendingIntent.getActivity(
//                        getApplicationContext(), type, intent6, PendingIntent.FLAG_UPDATE_CURRENT);
//                mBuilder.setContentIntent(pendIntent6);
//                break;
        }
        type++;
        Notification noti = mBuilder.build();
        noti.defaults = Notification.DEFAULT_ALL;
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
//        Log.d(LOG_TAG, noti.toString());
        return noti;
    }

    private String makeNotifiText(User notificationUser, LingoNotification notification) {
        String notifiText = "";
        switch (notification.getType()) {
            case LingoNotification.TYPE_PATH_COMMENT:
                notifiText = notificationUser.getNickname() + " " + getString(R.string.comment_notification);
                notiType = 1;
                break;
            case LingoNotification.TYPE_PATH_JOINED:
                notifiText = notificationUser.getNickname() + " " + getString(R.string.apply_notification);
                notiType = 2;
                break;
            case LingoNotification.TYPE_USER_FOLLOWED:
                notifiText = notificationUser.getNickname() + " " + getString(R.string.followed_notification);
                notiType = 3;
                break;
            case LingoNotification.TYPE_PATH_CHANGE:
                notifiText = notificationUser.getNickname() + "'s activity update.";
                notiType = 4;
                break;
            case LingoNotification.TYPE_USER_COMMENT:
                notifiText = notificationUser.getNickname() + " " + getString(R.string.comment_notification);
                notiType = 5;
                break;
            case LingoNotification.TYPE_INDENT_FINISH:
                notifiText = notificationUser.getNickname() + " " + getString(R.string.indent_notification);
                notiType = 6;
                break;
        }
        return notifiText;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}