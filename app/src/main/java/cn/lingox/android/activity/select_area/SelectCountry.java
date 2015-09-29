package cn.lingox.android.activity.select_area;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import cn.lingox.android.R;
import cn.lingox.android.adapter.ChooseAreaAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.location.Country1;
import cn.lingox.android.entity.location.Provinces;

/**
 * Created by Administrator on 2015/9/9.
 * 选择国家
 */
public class SelectCountry extends Activity {

    public static final String SELECTED = "Selected";
    public static final String SELECTLOCATION = "requestCode";

    private static SelectCountry selectCountry;
    private String country = "", province = "";
    private boolean isSelectedCountry = false, isSelectedProvince = false, isSelectedCity = false;
    private ListView listView1;
    private ListView listView2;
    private ListView listView3;
    private LinearLayout layout;
    private ImageView back;
    private ArrayList<Country1> datas = null;
    private ArrayList<String> countryDatas = new ArrayList<>();//存储国家数据，用于搜索
    private ArrayList<String> countryData = new ArrayList<>();//存储显示数据--国家
    private ArrayList<String> provincesData = new ArrayList<>();//存储显示数据--省份
    private ArrayList<String> cityData = new ArrayList<>();//存储显示数据--城市
    private ChooseAreaAdapter adapter1;
    private ChooseAreaAdapter adapter2;
    private ChooseAreaAdapter adapter3;
    private EditText search;
    private int requestCode = 0;
    private InputMethodManager imm;

    public static SelectCountry getObj() {
        return selectCountry;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_location);
        if (getIntent().hasExtra(SELECTLOCATION)) {
            requestCode = getIntent().getIntExtra(SELECTLOCATION, 0);
        }
        selectCountry = this;
        datas = new ArrayList<>();
        datas.addAll(LingoXApplication.getInstance().getCountryDatas());
        initView();
    }

    private void initView() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (datas != null) {
            for (int i = 0; i < datas.size(); i++) {
                countryData.add(datas.get(i).getCountry());
            }
        }
        Collections.sort(countryData);
        countryDatas.addAll(countryData);
        layout = (LinearLayout) findViewById(R.id.ppppppp);
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShow();
            }
        });
        search = (EditText) findViewById(R.id.search_edit);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });
        adapter1 = new ChooseAreaAdapter(this, countryData, isSelectedCountry);
        adapter2 = new ChooseAreaAdapter(this, provincesData, isSelectedProvince);
        adapter3 = new ChooseAreaAdapter(this, cityData, isSelectedCity);
        listView1 = (ListView) findViewById(R.id.choose_location_country);
        listView2 = (ListView) findViewById(R.id.choose_location_provinces);
        listView3 = (ListView) findViewById(R.id.choose_location_city);
        listView1.setAdapter(adapter1);
        listView2.setAdapter(adapter2);
        listView3.setAdapter(adapter3);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (Country1 c : datas) {
                    if (countryData.get(position).contentEquals(c.getCountry())) {
                        softIsShow(listView1);
                        if (c.getProvinces().isEmpty()) {
                            //无省份
                            close(c.getCountry());
                        } else {
                            provincesData.clear();
                            for (Provinces p : c.getProvinces()) {
                                provincesData.add(p.getProvinces());
                                Collections.sort(provincesData);
                            }
                            country = c.getCountry();
                            layout.setVisibility(View.GONE);
                            listView1.setVisibility(View.GONE);
                            listView2.setVisibility(View.VISIBLE);
                            adapter2.notifyDataSetChanged();
                        }
                        break;
                    }
                }
            }
        });
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (Country1 c : datas) {
                    if (country.contentEquals(c.getCountry())) {
                        for (Provinces p : c.getProvinces()) {
                            if (provincesData.get(position).contentEquals(p.getProvinces())) {
                                softIsShow(listView2);
                                if (p.getCity().isEmpty()) {
                                    //无数据
                                    close(p.getProvinces());
                                } else {
                                    province = p.getProvinces();
                                    cityData.clear();
                                    cityData.addAll(p.getCity());
                                    Collections.sort(cityData);
                                    listView2.setVisibility(View.GONE);
                                    listView3.setVisibility(View.VISIBLE);
                                    adapter3.notifyDataSetChanged();
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        });
        listView3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                close(cityData.get(position));
                softIsShow(listView3);
            }
        });
    }

    private void softIsShow(View view) {
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);//强制隐藏键盘
        }
    }

    private void isShow() {
        if (listView1.getVisibility() == View.VISIBLE) {
            close("");
            finish();
        } else if (listView2.getVisibility() == View.VISIBLE) {
            listView2.setVisibility(View.GONE);
            countryData.addAll(countryDatas);
            listView1.setVisibility(View.VISIBLE);
            search.setText("");
            layout.setVisibility(View.VISIBLE);
        } else if (listView3.getVisibility() == View.VISIBLE) {
            listView3.setVisibility(View.GONE);
            listView2.setVisibility(View.VISIBLE);
        }
    }

    private void close(String str) {
        Intent intent = new Intent();
        //TODO 实现添加数据
        if (str.isEmpty()) {
            intent.putExtra(SELECTED, str);
        } else if (provincesData.isEmpty()) {
            //直接添加国家
            intent.putExtra(SELECTED, str);
        } else if (cityData.isEmpty()) {
            //添加国家、省份
            intent.putExtra(SELECTED, country + ", " + str);
        } else {
            intent.putExtra(SELECTED, country + ", " + province + ", " + str);
        }
        setResult(requestCode, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        isShow();
    }

    private void search(String text) {
        if (adapter1.getIsEmpty()) {
            adapter1.setIsEmpty(true);
        }
        countryData.clear();
        for (String str : countryDatas) {
            if (str.toLowerCase(Locale.ENGLISH)
                    .contains(text.toLowerCase())) {
                countryData.add(str);
            }
        }
        adapter1.notifyDataSetChanged();
    }
}
