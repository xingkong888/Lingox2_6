package cn.lingox.android.utils;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;

import java.util.HashMap;
import java.util.Map;

import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.ServerHelper;

/**
 * 高德定位
 * <p/>
 * 注意：签名打包后才可正常使用
 */
public class GetLocationUtil implements AMapLocationListener {
    private static LocationManagerProxy mLocationManagerProxy;
    private static GetLocationUtil getLocationUtil = null;

    public static synchronized GetLocationUtil instance() {
        if (getLocationUtil == null) {
            getLocationUtil = new GetLocationUtil();
        }
        return getLocationUtil;
    }

    /**
     * 实例化定位管理器
     *
     * @param context  上下文
     * @param longTime 定位间隔，单位：ms
     */
    public void init(Context context, int longTime) {
        mLocationManagerProxy = LocationManagerProxy.getInstance(context);
        //此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        //注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
        //在定位结束后，在合适的生命周期调用destroy()方法
        //其中如果间隔时间为-1，则定位只定一次
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, longTime, 15, this);
        mLocationManagerProxy.setGpsEnable(false);
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
                //用户登录且用户位置发生改变
                if (user != null && (Math.abs(user.getLoc()[0] - geoLng) > 0.5) && (Math.abs(user.getLoc()[1] - geoLat) > 0.5)) {
                    user.setLoc(geoLocation);
                    CacheHelper.getInstance().setSelfInfo(user);
                    LingoXApplication.getInstance().setLocation(geoLat, geoLng);
                    new Thread() {
                        public void run() {
                            Map<String, String> params = new HashMap<>();
                            params.put(StringConstant.userIdStr, CacheHelper.getInstance().getSelfInfo().getId());
                            params.put(StringConstant.locStr, JsonHelper.getInstance().getLocationStr(geoLocation));
                            try {
                                ServerHelper.getInstance().updateUserInfo(params);
                            } catch (Exception e) {
                                Log.e("GetLocationUtil", "onLocationChanged(): " + e.getMessage());
                            }
                        }
                    }.start();
                } else {
                    LingoXApplication.getInstance().setLocation(geoLat, geoLng);
                }
            } catch (Exception e) {
                Log.e("GetLocationUtil", "LocationListener: Exception caught: " + e.getMessage());
            }
        }
    }

    //移除定位
    public void removeUpdates() {
        mLocationManagerProxy.removeUpdates(this);
    }

    //销毁实例
    public void onDestroy() {
        mLocationManagerProxy.destroy();
    }
}