package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import cn.lingox.android.R;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;


public class AMapActivity extends Activity implements AMap.OnMarkerClickListener,
        AMap.InfoWindowAdapter, GeocodeSearch.OnGeocodeSearchListener, AMap.OnInfoWindowClickListener {
    //声明变量
    private MapView mapView;
    private AMap aMap;

    private User user = CacheHelper.getInstance().getSelfInfo();
    private Marker marker;
    private LatLng latLng;

    private GeocodeSearch geocoderSearch;
    private LatLonPoint latLonPoint;
    private double lat, lng;
    private Intent intent = new Intent();

    private EditText editText;
    private Button btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在onCreat方法中给aMap对象赋值
        setContentView(R.layout.activity_map);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 必须要写
        aMap = mapView.getMap();

        lat = user.getLoc()[0];
        lng = user.getLoc()[1];
        latLng = new LatLng(lat, lng);
        latLonPoint = new LatLonPoint(lat, lng);

        aMap.setOnMarkerClickListener(this);
        aMap.setInfoWindowAdapter(this);
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        getAddress();
        initView();
    }


    private void initView() {
        editText = (EditText) findViewById(R.id.map_search);
        btn = (Button) findViewById(R.id.map_search_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // name表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode
                GeocodeQuery query = new GeocodeQuery(editText.getText().toString().trim(), "beijing");
                geocoderSearch.getFromLocationNameAsyn(query);
//                Log.d("星期","点击"+editText.getText());
            }
        });
    }

    //根据坐标，获取地址描述
    private void getAddress() {
        //latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    //创建标记
    private void makeMarker(LatLng latLng1, String title) {
        aMap.clear();
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng1));
        marker = aMap.addMarker(new MarkerOptions()
                .position(latLng1)
                .title(title)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .draggable(true));
        marker.showInfoWindow();// 设置默认显示一个infowinfow
    }

    //逆地理编码时将地理坐标转换为中文地址（地名描述） 的回调接口

    /**
     * @param result 返回的具体数据
     * @param rCode  返回码
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == 0) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                String addressName = result.getRegeocodeAddress().getFormatAddress()
                        + "附近";
//                Toast.makeText(this, addressName, Toast.LENGTH_LONG).show();
                makeMarker(latLng, addressName);
            } else {
                Toast.makeText(this, "No data", Toast.LENGTH_LONG).show();
            }
        } else if (rCode == 27) {
            Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_LONG).show();
        } else if (rCode == 32) {
//            Toast.makeText(this,R.string.network_unavailable,Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "An unknown error", Toast.LENGTH_LONG).show();
        }
    }

    //地理编码是将中文地址(或地名描述)转换为地理坐标 的回调接口

    /**
     * @param result
     * @param rCode
     */
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        if (rCode == 0) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = result.getGeocodeAddressList().get(0);
                LatLonPoint point = address.getLatLonPoint();
//                Toast.makeText(this,point.getLatitude()+">>>"+point.getLongitude(), Toast.LENGTH_LONG).show();
                makeMarker(new LatLng(point.getLatitude(), point.getLongitude()), address.getFormatAddress());
            } else {
                Toast.makeText(this, "No data", Toast.LENGTH_LONG).show();
            }
        } else if (rCode == 27) {
            Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_LONG).show();
        } else if (rCode == 32) {
//            Toast.makeText(this,R.string.network_unavailable,Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "An unknown error", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker1) {
        if (marker1.equals(marker)) {
            if (aMap != null) {
                Toast.makeText(this, "ABCD", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    /**
     * 监听自定义infowindow窗口的infowindow事件回调
     */
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * 监听自定义infowindow窗口的infocontents事件回调
     */
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(PathEditActivity.SELECTDETIAL, intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}