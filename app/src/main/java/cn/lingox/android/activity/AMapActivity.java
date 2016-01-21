package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;

import java.util.ArrayList;
import java.util.List;

import cn.lingox.android.R;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;

/**
 * 高德地图
 */
public class AMapActivity extends Activity implements GeocodeSearch.OnGeocodeSearchListener, Inputtips.InputtipsListener {
    //声明变量
    private MapView mapView;
    private AMap aMap;
    private EditText editText;
    private ProgressBar pb;
    private Button ok;

    private String address = "";

    private User user = CacheHelper.getInstance().getSelfInfo();

    private GeocodeSearch geocoderSearch;
    private double lat, lng;
    private Intent intent = new Intent();
    private LatLonPoint point = null;

    private LinearLayout layout;//搜索栏

    private ListView listView;
    private List<String> arrayList = new ArrayList<>();
    private ArrayList<Tip> tipList = new ArrayList<>();
    private ArrayAdapter adapter;

    private boolean focus = false;//标识edit是否获取焦点，false 失去焦点 true 得到焦点
    //键盘管理器
    private InputMethodManager imm;

    private boolean isChat = false;//标识是否为聊天页获取位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在onCreat方法中给aMap对象赋值
        setContentView(R.layout.activity_map);
        setMapView(savedInstanceState);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        initView();
        if (getIntent().hasExtra("LOCATION")) {
            isChat = true;
        }
        if (getIntent().hasExtra("Latitude")) {
            ok.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);
            lat = Double.valueOf(getIntent().getStringExtra("Latitude"));
            lng = Double.valueOf(getIntent().getStringExtra("Longitude"));
            makeMarker(getIntent().getStringExtra("address"));
        }
    }

    /**
     * 设置地图
     *
     * @param savedInstanceState ""
     */
    private void setMapView(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 必须要写
        aMap = mapView.getMap();
        aMap.getUiSettings().setZoomControlsEnabled(false);//使缩放按钮不可见
        if (getIntent().hasExtra("String")) {
            lat = Double.valueOf(getIntent().getStringArrayExtra("String")[0]);
            lng = Double.valueOf(getIntent().getStringArrayExtra("String")[1]);
        } else {
            lat = user.getLoc()[1];
            lng = user.getLoc()[0];
        }

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);

        getAddress(new LatLonPoint(lat, lng));
    }

    /**
     * 初始化控件
     */
    private void initView() {
        layout = (LinearLayout) findViewById(R.id.qwertyu);
        //ok
        ok = (Button) findViewById(R.id.map_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnResult();
            }
        });
        //用于搜索
        editText = (EditText) findViewById(R.id.map_search);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2 && focus) {
                    request(s.toString());
                } else {
                    arrayList.clear();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focus = hasFocus;
            }
        });
        pb = (ProgressBar) findViewById(R.id.map_pb);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request(editText.getText().toString());
                //关闭键盘
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList);
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //关闭键盘
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                Tip tip = tipList.get(position);
                if (pb.getVisibility() == View.GONE) {
                    pb.setVisibility(View.VISIBLE);
                }
                lat = tip.getPoint().getLatitude();
                lng = tip.getPoint().getLongitude();
                getAddress(tip.getPoint());
                focus = false;
                editText.setText(tip.getName());
                focus = true;
                listView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 发起请求
     *
     * @param str 关键字
     */
    private void request(String str) {
        if (listView.getVisibility() == View.GONE) {
            listView.setVisibility(View.VISIBLE);
        }
        if (str.trim().length() < 2) {
            arrayList.clear();
            listView.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        } else {
            // 发送输入提示请求
            Inputtips inputtips = new Inputtips(AMapActivity.this, AMapActivity.this);
            try {
                // 第一个参数表示提示关键字，第二个参数默认代表全国，也可以为城市区号
                inputtips.requestInputtips(str.trim(), "");
            } catch (Exception e) {
                Toast.makeText(AMapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 点击系统返回键，若搜索结果显示，则关闭显示；
     * 否则返回上一界面
     */
    @Override
    public void onBackPressed() {
        if (listView.getVisibility() == View.VISIBLE) {
            listView.setVisibility(View.GONE);
        } else {
            double[] doubles = {};
            intent.putExtra(LocalEditActivity.SELECTDETIALLAT, doubles);//坐标
            intent.putExtra(LocalEditActivity.SELECTDETIALADD, "");//地址
            setResult(LocalEditActivity.SELECTDETIAL, intent);
            finish();
        }
    }

    //根据坐标，获取地址描述
    private void getAddress(LatLonPoint latLonPoint) {
        //    latLonPoint参数表示一个Latlng，第二参数表示范围多少米，
        //      GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    //创建标记
    private void makeMarker(String address) {
        LatLng latLng = new LatLng(lat, lng);
        if (pb.getVisibility() == View.VISIBLE) {
            pb.setVisibility(View.GONE);
        }
        this.address = address;
        aMap.clear();
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        Marker marker = aMap.addMarker(new MarkerOptions().position(latLng).title(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(true));
        marker.showInfoWindow();// 设置默认显示一个infowinfow
        ok.setVisibility(View.VISIBLE);
    }

    /**
     * @param list 返回的结果集合
     * @param i    返回的状态码
     */
    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        if (i == 0) {
            tipList.clear();
            tipList.addAll(list);
            arrayList.clear();
            for (Tip tip : list) {
                arrayList.add(tip.getName());
            }
            if (arrayList.size() > 0) {
                listView.setVisibility(View.VISIBLE);
            } else {
                listView.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
        }
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
                makeMarker(addressName);
            } else {
                Toast.makeText(this, "No data", Toast.LENGTH_LONG).show();
            }
        } else if (rCode == 27 || rCode == 32) {
            Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "An unknown error", Toast.LENGTH_LONG).show();
        }
    }

    //地理编码是将中文地址(或地名描述)转换为地理坐标 的回调接口

    /**
     * @param result 返回结果
     * @param rCode  返回码
     */
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
    }

    //返回结果到上一级
    private void returnResult() {
        double[] doubles;
        if (point != null) {
            doubles = new double[2];
            doubles[0] = point.getLongitude();
            doubles[1] = point.getLatitude();
        } else {
            doubles = new double[2];
            doubles[0] = lng;
            doubles[1] = lat;
        }
        if (isChat) {
            intent.putExtra("latitude", doubles[1]);//坐标
            intent.putExtra("longitude", doubles[0]);//坐标
            intent.putExtra("address", address);//地址
            setResult(RESULT_OK, intent);
        } else {
            intent.putExtra(LocalEditActivity.SELECTDETIALLAT, doubles);//坐标
            intent.putExtra(LocalEditActivity.SELECTDETIALADD, address);//地址
            setResult(LocalEditActivity.SELECTDETIAL, intent);
        }
        finish();
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
}