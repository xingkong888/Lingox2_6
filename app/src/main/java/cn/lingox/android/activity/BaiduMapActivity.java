///**
// * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
// * <p/>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * http://www.apache.org/licenses/LICENSE-2.0
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package cn.lingox.android.activity;
//
//import android.app.ProgressDialog;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.AutoCompleteTextView;
//import android.widget.Button;
//import android.widget.Toast;
//
//import com.baidu.location.BDLocation;
//import com.baidu.location.LocationClient;
//import com.baidu.mapapi.SDKInitializer;
//import com.baidu.mapapi.map.BaiduMap;
//import com.baidu.mapapi.map.BaiduMapOptions;
//import com.baidu.mapapi.map.BitmapDescriptorFactory;
//import com.baidu.mapapi.map.MapStatus;
//import com.baidu.mapapi.map.MapStatusUpdate;
//import com.baidu.mapapi.map.MapStatusUpdateFactory;
//import com.baidu.mapapi.map.MapView;
//import com.baidu.mapapi.map.MarkerOptions;
//import com.baidu.mapapi.map.MyLocationConfiguration;
//import com.baidu.mapapi.map.OverlayOptions;
//import com.baidu.mapapi.model.LatLng;
//import com.baidu.mapapi.overlayutil.PoiOverlay;
//import com.baidu.mapapi.search.core.CityInfo;
//import com.baidu.mapapi.search.core.PoiInfo;
//import com.baidu.mapapi.search.core.SearchResult;
//import com.baidu.mapapi.search.geocode.GeoCodeResult;
//import com.baidu.mapapi.search.geocode.GeoCoder;
//import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
//import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
//import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
//import com.baidu.mapapi.search.poi.PoiCitySearchOption;
//import com.baidu.mapapi.search.poi.PoiDetailResult;
//import com.baidu.mapapi.search.poi.PoiResult;
//import com.baidu.mapapi.search.poi.PoiSearch;
//import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
//import com.baidu.mapapi.search.sug.SuggestionResult;
//import com.baidu.mapapi.search.sug.SuggestionSearch;
//import com.baidu.mapapi.search.sug.SuggestionSearchOption;
//import com.baidu.mapapi.utils.CoordinateConverter;
//import com.umeng.analytics.MobclickAgent;
//
//import cn.lingox.android.R;
//import cn.lingox.android.app.LingoXApplication;
//import cn.lingox.android.entity.Path;
//import cn.lingox.android.entity.User;
//import cn.lingox.android.helper.CacheHelper;
//
//public class BaiduMapActivity extends BaseActivity implements View.OnClickListener,
//        OnGetPoiSearchResultListener, OnGetSuggestionResultListener, OnGetGeoCoderResultListener {
//
//    //intent
//    public static final int REQUEST_PATH_LOCATION = 201;
//    public static final String PATH_TO_LOCATE = LingoXApplication.PACKAGE_NAME + ".PATH_LOCATION";
//    public static final String LOCATION = LingoXApplication.PACKAGE_NAME + ".LOCATION";
//    public static final String CITY = LingoXApplication.PACKAGE_NAME + ".CITY";
//    public static final String GPS_ADDRESS = LingoXApplication.PACKAGE_NAME + ".GPS_ADDRESS";
//    private final static String LOG_TAG = "BaiduMapActivity";
//    public static BaiduMapActivity instance = null;
//    private static BDLocation lastLocation = null;
//    private static MapView mMapView = null;
//    // 定位相关
//    private LocationClient mLocClient;
//    private PoiSearch mPoiSearch = null;
//    /*public MyLocationListenner myListener = new MyLocationListenner();*/
//    private BaiduSDKReceiver mBaiduReceiver;
//    private MyLocationConfiguration.LocationMode mCurrentMode;
//    private SuggestionSearch mSuggestionSearch;
//    private BaiduMap mBaiduMap;
//    private GeoCoder mSearch = null;
//    //ui
//    private ProgressDialog progressDialog;
//    private Button searchButton = null;
//    private Button conformButton = null;
//    private Boolean isFromPathView;
//    private Boolean isPosiSelected = false;
//    private Path pathLocate;
//
//    /*private String city = "北京";*/
//    private double[] gpsAddress = new double[]{0, 0};
//    /**
//     * 搜索关键字输入窗口
//     */
//    private AutoCompleteTextView keyWorldsView = null;
//    private ArrayAdapter<String> sugAdapter = null;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//       /* isFromPathView = getIntent().hasExtra(PATH_TO_LOCATE);
//        pathLocate = getIntent().getParcelableExtra(PATH_TO_LOCATE);*/
//        mSearch = GeoCoder.newInstance();
//        instance = this;
//        User user = CacheHelper.getInstance().getSelfInfo();
//        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
//        //注意该方法要再setContentView方法之前实现
//        SDKInitializer.initialize(getApplication());
//        setContentView(R.layout.activity_baidumap);
//
//
//        mMapView = (MapView) findViewById(R.id.bmapView);
//        searchButton = (Button) findViewById(R.id.btn_location_search);
//        searchButton.setOnClickListener(this);
//        conformButton = (Button) findViewById(R.id.btn_location_conform);
//        conformButton.setOnClickListener(this);
//        /*conformButton.setVisibility(isFromPathView ? View.INVISIBLE : View.VISIBLE);*/
//        keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
//        sugAdapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_dropdown_item_1line);
//        keyWorldsView.setAdapter(sugAdapter);
//        mPoiSearch = PoiSearch.newInstance();
//        mPoiSearch.setOnGetPoiSearchResultListener(this);
//        mSuggestionSearch = SuggestionSearch.newInstance();
//        mSuggestionSearch.setOnGetSuggestionResultListener(this);
//        keyWorldsView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() <= 0) {
//                    return;
//                }
//
//                /**
//                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
//                 */
//                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
//                        .keyword(s.toString()).city(CacheHelper.getInstance().getSelfInfo().getCity()));
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
///*
//                mSearch.geocode(new GeoCodeOption().city(city).address(keyWorldsView.getText().toString()));
//*/
//            }
//        });
//        double longitude = user.getLoc()[0];
//        double latitude = user.getLoc()[1];
//        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
//        mBaiduMap = mMapView.getMap();
//        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
//        mBaiduMap.setMapStatus(msu);
//        mMapView.setLongClickable(true);
//        String address = user.getLocString();
//        LatLng p = new LatLng(latitude, longitude);
//        mMapView = new MapView(this,
//                new BaiduMapOptions().mapStatus(new MapStatus.Builder()
//                        .target(p).build()));
//        showMap(latitude, longitude, address);
//
//        // 注册 SDK 广播监听者
//        IntentFilter iFilter = new IntentFilter();
//        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
//        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
//        mBaiduReceiver = new BaiduSDKReceiver();
//        registerReceiver(mBaiduReceiver, iFilter);
//
//
//    }
//
//    private void showMap(double latitude, double longtitude, String address) {
//        LatLng llA = new LatLng(latitude, longtitude);
//        CoordinateConverter converter = new CoordinateConverter();
//        converter.coord(llA);
//        converter.from(CoordinateConverter.CoordType.COMMON);
//        LatLng convertLatLng = converter.convert();
//        OverlayOptions ooA = new MarkerOptions().position(convertLatLng).icon(BitmapDescriptorFactory
//                .fromResource(R.drawable.icon_marka))
//                .zIndex(4).draggable(true);
//        mBaiduMap.addOverlay(ooA);
//        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(convertLatLng, 17.0f);
//        mBaiduMap.animateMapStatus(u);
//    }
//
//
//    @Override
//    protected void onPause() {
//        MobclickAgent.onPause(this);
//        mMapView.onPause();
// /*       if (mLocClient != null) {
//            mLocClient.stop();
//        }*/
//        super.onPause();
///*
//        lastLocation = null;
//*/
//    }
//
//    @Override
//    protected void onResume() {
//        MobclickAgent.onResume(this);
//        mMapView.onResume();
//        if (mLocClient != null) {
//            mLocClient.start();
//        }
//        super.onResume();
//    }
//
//    @Override
//    protected void onDestroy() {
///*        if (mLocClient != null)
//            mLocClient.stop();*/
//        mMapView.onDestroy();
//        unregisterReceiver(mBaiduReceiver);
//        mPoiSearch.destroy();
//        mSuggestionSearch.destroy();
//        super.onDestroy();
//    }
//
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_location_search:
//                if (!TextUtils.isEmpty(keyWorldsView.getText())) {
//                    String s = keyWorldsView.getText().toString();
//                    mPoiSearch.searchInCity((new PoiCitySearchOption())
//                            .city("北京")
//                            .keyword(s));
//                }
//                break;
//            case R.id.btn_location_conform:
//                String location = keyWorldsView.getText().toString();
//               /* new setPathGps().execute();*/
//                if (!TextUtils.isEmpty(location) && isPosiSelected) {
//                    Intent locationIntent = new Intent();
//                    locationIntent.putExtra(LOCATION, location);
//                    locationIntent.putExtra(GPS_ADDRESS, gpsAddress);
//                    setResult(RESULT_OK, locationIntent);
//                    finish();
//                } else if (TextUtils.isEmpty(location)) {
//                    Toast.makeText(BaiduMapActivity.this, getString(R.string.toast_enter_location), Toast.LENGTH_SHORT).show();
//
//                } else if (!isPosiSelected) {
//                    Toast.makeText(BaiduMapActivity.this, getString(R.string.toast_select_location), Toast.LENGTH_SHORT).show();
//
//                }
//
//                break;
//        }
//    }
//
//    @Override
//    public void onGetPoiResult(PoiResult result) {
//        if (result == null
//                || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
//            Toast.makeText(this, "未找到结果", Toast.LENGTH_LONG)
//                    .show();
//            return;
//        }
//        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//            mBaiduMap.clear();
//            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
//            mBaiduMap.setOnMarkerClickListener(overlay);
//            overlay.setData(result);
//            overlay.addToMap();
//            overlay.zoomToSpan();
//            return;
//        }
//        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
//
//            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
//            String strInfo = "在";
//            for (CityInfo cityInfo : result.getSuggestCityList()) {
//                strInfo += cityInfo.city;
//                strInfo += ",";
//            }
//            strInfo += "找到结果";
//            Toast.makeText(this, strInfo, Toast.LENGTH_LONG)
//                    .show();
//        }
//    }
//
//    @Override
//    public void onGetPoiDetailResult(PoiDetailResult result) {
//        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
//            Toast.makeText(this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
//                    .show();
//        } else {
//            Toast.makeText(this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT)
//                    .show();
//        }
//    }
//
//    @Override
//    public void onGetSuggestionResult(SuggestionResult res) {
//        if (res == null || res.getAllSuggestions() == null) {
//            return;
//        }
//        sugAdapter.clear();
//        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
//            if (info.key != null)
//                sugAdapter.add(info.key);
//        }
//        sugAdapter.notifyDataSetChanged();
//    }
//
//    @Override
//    public void onGetGeoCodeResult(GeoCodeResult result) {
//        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//            //没有检索到结果
//            Toast.makeText(this, "没有检索到结果", Toast.LENGTH_LONG).show();
//        } else {
//            final double[] geoLocation = {result.getLocation().latitude, result.getLocation().longitude};
//            gpsAddress = geoLocation;
//            Toast.makeText(this, result.getLocation().latitude + "longitude" + result.getLocation().longitude, Toast.LENGTH_LONG).show();
//            //获取地理编码结果
//        }
//    }
//
//    @Override
//    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
//
//    }
//
//    public void back(View v) {
//        finish();
//    }
//
//    /**
//     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
//     */
//    public class BaiduSDKReceiver extends BroadcastReceiver {
//        public void onReceive(Context context, Intent intent) {
//            String s = intent.getAction();
//            String st1 = getString(R.string.Network_error);
//            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
//
//                String st2 = getString(R.string.please_check);
//                Toast.makeText(instance, st2, Toast.LENGTH_SHORT).show();
//            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
//                Toast.makeText(instance, st1, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private class MyPoiOverlay extends PoiOverlay {
//
//        public MyPoiOverlay(BaiduMap baiduMap) {
//            super(baiduMap);
//        }
//
//        @Override
//        public boolean onPoiClick(int index) {
//            super.onPoiClick(index);
//            PoiInfo poi = getPoiResult().getAllPoi().get(index);
//            /*mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
//                    .poiUid(poi.uid));*/
//            gpsAddress[0] = poi.location.latitude;
//            gpsAddress[1] = poi.location.longitude;
//            isPosiSelected = true;
//            /*city = poi.city;*/
//            keyWorldsView.setText(poi.name);
//            return true;
//        }
//    }
//
//  /*  private class setPathGps extends AsyncTask<Void,Void,Void> {
//        String location = keyWorldsView.getText().toString();
//        @Override
//        protected Void doInBackground(Void... params) {
//            if(!isPosiSelected) {
//                mSearch.geocode(new GeoCodeOption().city(city).address(location));
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            if (!TextUtils.isEmpty(location)) {
//                Intent locationIntent = new Intent();
//                locationIntent.putExtra(LOCATION, location);
//                *//*locationIntent.putExtra(CITY, city);*//*
//                locationIntent.putExtra(GPS_ADDRESS,gpsAddress);
//                setResult(RESULT_OK, locationIntent);
//                finish();
//            } else if(TextUtils.isEmpty(location)) {
//                Toast.makeText(BaiduMapActivity.this, getString(R.string.toast_enter_location), Toast.LENGTH_SHORT).show();
//
//            }
//        }
//    }*/
//    /* public void sendLocation(View view) {
//        Intent intent = this.getIntent();
//        intent.putExtra("latitude", lastLocation.getLatitude());
//        intent.putExtra("longitude", lastLocation.getLongitude());
//        intent.putExtra("address", lastLocation.getAddrStr());
//        this.setResult(RESULT_OK, intent);
//        finish();
//        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
//    }*/
//
//   /* *//**
//     * 监听函数，有新位置的时候，格式化成字符串，输出到屏幕中
//     *//*
//    public class MyLocationListenner implements BDLocationListener {
//        @Override
//        public void onReceiveLocation(BDLocation location) {
//            if (location == null) {
//                return;
//            }
//            Log.d("map", "On location change received:" + location);
//            Log.d("map", "addr:" + location.getAddrStr());
//            searchButton.setEnabled(true);
//            if (progressDialog != null) {
//                progressDialog.dismiss();
//            }
//
//            if (lastLocation != null) {
//                if (lastLocation.getLatitude() == location.getLatitude() && lastLocation.getLongitude() == location.getLongitude()) {
//                    Log.d("map", "same location, skip refresh");
//                    // mMapView.refresh(); //need this refresh?
//                    return;
//                }
//            }
//            lastLocation = location;
//            mBaiduMap.clear();
//            LatLng llA = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
//            CoordinateConverter converter = new CoordinateConverter();
//            converter.coord(llA);
//            converter.from(CoordinateConverter.CoordType.COMMON);
//            LatLng convertLatLng = converter.convert();
//            OverlayOptions ooA = new MarkerOptions().position(convertLatLng).icon(BitmapDescriptorFactory
//                    .fromResource(R.drawable.icon_marka))
//                    .zIndex(4).draggable(true);
//            mBaiduMap.addOverlay(ooA);
//            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(convertLatLng, 17.0f);
//            mBaiduMap.animateMapStatus(u);
//        }
//
//        public void onReceivePoi(BDLocation poiLocation) {
//            if (poiLocation == null) {
//                return;
//            }
//        }
//    }
//
//    public class NotifyLister extends BDNotifyListener {
//        public void onNotify(BDLocation mlocation, float distance) {
//        }
//    }*/
///* private void showMapWithLocationClient() {
//        String str1 = getString(R.string.Making_sure_your_location);
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressDialog.setMessage(str1);
//
//        progressDialog.setOnCancelListener(new OnCancelListener() {
//
//            public void onCancel(DialogInterface arg0) {
//                if (progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//                Log.d("map", "cancel retrieve location");
//                finish();
//            }
//        });
//
//        progressDialog.show();
//
//        mLocClient = new LocationClient(this);
//        mLocClient.registerLocationListener(myListener);
//
//        LocationClientOption option = new LocationClientOption();
//        option.setOpenGps(true);// 打开gps
//        // option.setCoorType("bd09ll"); //设置坐标类型
//        // Johnson change to use gcj02 coordination. chinese national standard
//        // so need to conver to bd09 everytime when draw on baidu map
//        option.setCoorType("gcj02");
//        option.setScanSpan(30000);
//        //option.setAddrType("all");
//        mLocClient.setLocOption(option);
//    }
//*/
//}
