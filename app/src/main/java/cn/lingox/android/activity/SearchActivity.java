package cn.lingox.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.lingox.android.R;
import cn.lingox.android.activity.select_area.SelectCountry;
import cn.lingox.android.adapter.MyAdapter;
import cn.lingox.android.adapter.NearbyAdapter;
import cn.lingox.android.adapter.PathAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.PathTags;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.widget.SearchDialog;

public class SearchActivity extends FragmentActivity implements OnClickListener {

    private static final int SEARCH_TYPE_ADVANCED = 3;
    private static final int SELECTLOCATION = 126;
    Map<String, String> params = new HashMap<>();
    private TextView cancel, done;
    private PullToRefreshListView listView;
    private ImageView anim;
    private AnimationDrawable animationDrawable;
    //活动页面
    private TextView local, travel, disLocation;
    private LinearLayout discover;
    private ImageView del;
    //个人页面
    private TextView name, memLocation, guide, meal, stay, male, female, language;
    private LinearLayout member;
    private ImageView delLocation, delLanguage, delName;
    private int which = 0;//1表示搜索活动 2表示搜索个人
    private String country = "";
    private String province = "";
    private String city = "";
    private PathAdapter disAdapter;
    private NearbyAdapter memAdapter;
    private ProgressBar progressBar;

    private int searchLocalOrTravel = 0;//1 Local 2 Travel
    private boolean searchLocal = false, searchMeal = false, searchStay = false;

    //活动
    private ArrayList<Path> pathList = new ArrayList<>();
    private ArrayList<User> userList = new ArrayList<>();

    private int page = 1;

    private ListView listView1;
    private MyAdapter adapter;
    private ArrayList<PathTags> datas = new ArrayList<>();
    private int checkedNum = 0;

    private HashMap<Integer, Integer> activityTags = new HashMap<>();

    private float x1 = 0, x2 = 0, y1 = 0, y2 = 0;
    private VelocityTracker mVelocityTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if (getIntent().hasExtra(MainActivity.SEARCH)) {
            which = getIntent().getIntExtra(MainActivity.SEARCH, 0);
        }
        initView();
    }

    private void initView() {
        anim = (ImageView) findViewById(R.id.search_anim);
        animationDrawable = (AnimationDrawable) anim.getBackground();
        cancel = (TextView) findViewById(R.id.search_cancel);
        cancel.setOnClickListener(this);
        done = (TextView) findViewById(R.id.search_done);
        done.setOnClickListener(this);


        listView1 = (ListView) findViewById(R.id.search_tage);
        datas = new ArrayList<>();
        datas = LingoXApplication.getInstance().getDatas();
        adapter = new MyAdapter(this, datas, 1);
        listView1.setAdapter(adapter);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (datas.get(position).getType() == 0) {
                    if (checkedNum < 3) {
                        activityTags.put(position, 1);
                        checkedNum++;
                        datas.get(position).setType(1);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    checkedNum--;
                    activityTags.remove(position);
                    datas.get(position).setType(0);
                    adapter.notifyDataSetChanged();
                }
            }
        });


        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        listView = (PullToRefreshListView) findViewById(R.id.search_show_list);
        switch (which) {
            case 1:
                MobclickAgent.onEvent(this, "search_discover");
                disAdapter = new PathAdapter(this, pathList);
                listView.setAdapter(disAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(SearchActivity.this, PathViewActivity.class);
                        intent.putExtra(PathViewActivity.PATH_TO_VIEW, pathList.get(position - 1));
                        startActivityForResult(intent, PathFragment.EDIT_PATH);
                    }
                });
                break;
            case 2:
                MobclickAgent.onEvent(this, "search_members");
                memAdapter = new NearbyAdapter(this, userList);
                listView.setAdapter(memAdapter);
                break;
        }

        listView.setMode(PullToRefreshBase.Mode.DISABLED);
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                switch (which) {
                    case 1://活动
                        if (page <= LingoXApplication.getInstance().getUserPageCount()) {
                            page += 1;
                            if (!disLocation.getText().toString().isEmpty() || searchLocalOrTravel != 0 || activityTags.size() > 0) {
                                if (country.isEmpty()) {
                                    new GetPaths("", "", "", searchLocalOrTravel, page, activityTags).execute();
                                } else if (province.isEmpty()) {
                                    new GetPaths(country, "", "", searchLocalOrTravel, page, activityTags).execute();
                                } else if (city.isEmpty()) {
                                    new GetPaths(country, province, "", searchLocalOrTravel, page, activityTags).execute();
                                } else {
                                    new GetPaths(country, province, city, searchLocalOrTravel, page, activityTags).execute();
                                }
                            } else {
                                Toast.makeText(SearchActivity.this, "Please select a filter condition", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case 2:
                        if (page <= LingoXApplication.getInstance().getUserPageCount()) {
                            page += 1;
                            if (tvSearch()) {
                                new SearchUser(params, SEARCH_TYPE_ADVANCED).execute();
                            }
                        }
                        break;
                }
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    //空闲时加载
                    switch (which) {
                        case 1:
                            disAdapter.setIsFling(false);
                            disAdapter.notifyDataSetChanged();
                            break;
                        case 2:
                            memAdapter.setIsFling(false);
                            memAdapter.notifyDataSetChanged();
                            break;
                    }
                } else {
                    switch (which) {
                        case 1:
                            disAdapter.setIsFling(true);
                            break;
                        case 2:
                            memAdapter.setIsFling(true);
                            break;
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        switch (which) {
            case 1:
                //discover页面
                del = (ImageView) findViewById(R.id.search_del);
                del.setOnClickListener(this);
                local = (TextView) findViewById(R.id.search_local);
                local.setOnClickListener(this);
                local.setTag(0);
                travel = (TextView) findViewById(R.id.search_travel);
                travel.setOnClickListener(this);
                travel.setTag(0);
                disLocation = (TextView) findViewById(R.id.search_location);
                disLocation.setOnClickListener(this);
                discover = (LinearLayout) findViewById(R.id.search_discover);
                discover.setVisibility(View.VISIBLE);
                break;
            case 2:
                //个人页面
                delLocation = (ImageView) findViewById(R.id.search_member_del1);
                delLocation.setOnClickListener(this);
                delLanguage = (ImageView) findViewById(R.id.search_member_del2);
                delLanguage.setOnClickListener(this);
                delName = (ImageView) findViewById(R.id.search_member_del0);
                delName.setOnClickListener(this);
                name = (TextView) findViewById(R.id.search_member_name);
                name.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                memLocation = (TextView) findViewById(R.id.search_member_location);
                memLocation.setOnClickListener(this);
                guide = (TextView) findViewById(R.id.search_member_local);
                guide.setOnClickListener(this);
                guide.setTag(0);
                meal = (TextView) findViewById(R.id.search_member_meal);
                meal.setOnClickListener(this);
                meal.setTag(0);
                stay = (TextView) findViewById(R.id.search_member_stay);
                stay.setOnClickListener(this);
                stay.setTag(0);
                male = (TextView) findViewById(R.id.search_member_male);
                male.setOnClickListener(this);
                male.setTag(0);
                female = (TextView) findViewById(R.id.search_member_female);
                female.setOnClickListener(this);
                female.setTag(0);
                language = (TextView) findViewById(R.id.search_member_language);
                language.setOnClickListener(this);
                member = (LinearLayout) findViewById(R.id.search_member);
                member.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void startAnim() {
        if (!animationDrawable.isRunning()) {
            anim.setVisibility(View.VISIBLE);
            animationDrawable.start();
        }
    }

    private void stopAnim() {
        if (animationDrawable.isRunning()) {
            anim.setVisibility(View.GONE);
            animationDrawable.stop();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_cancel:
                finish();
                break;
            case R.id.search_done:
                listView.setVisibility(View.VISIBLE);
                listView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
                if (done.getText().toString().equals("DONE")) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {//获取键盘状态 true:显示 false：隐藏
                        imm.hideSoftInputFromWindow(done.getWindowToken(), 0); //强制隐藏键盘
                    }
                    switch (which) {
                        case 1:
                            if (!disLocation.getText().toString().isEmpty() || searchLocalOrTravel != 0 || activityTags.size() > 0) {
                                pathList.clear();
                                disAdapter.notifyDataSetChanged();
                                discover.setVisibility(View.INVISIBLE);
                                if (country.isEmpty()) {
                                    new GetPaths("", "", "", searchLocalOrTravel, page, activityTags).execute();
                                } else if (province.isEmpty()) {
                                    new GetPaths(country, "", "", searchLocalOrTravel, page, activityTags).execute();
                                } else if (city.isEmpty()) {
                                    new GetPaths(country, province, "", searchLocalOrTravel, page, activityTags).execute();
                                } else {
                                    new GetPaths(country, province, city, searchLocalOrTravel, page, activityTags).execute();
                                }
                            } else {
                                Toast.makeText(this, "Please select a filter condition", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 2:
                            if (tvSearch()) {
                                userList.clear();
                                memAdapter.notifyDataSetChanged();
                                member.setVisibility(View.INVISIBLE);
                                new SearchUser(params, SEARCH_TYPE_ADVANCED).execute();
                            }
                            break;
                    }
                } else if (done.getText().toString().equals("SEARCH")) {
                    //停止动画
                    stopAnim();
                    listView.setVisibility(View.INVISIBLE);
                    done.setText(getString(R.string.done));
                    switch (which) {
                        case 1:
                            discover.setVisibility(View.VISIBLE);
                            break;
                        case 2:
                            member.setVisibility(View.VISIBLE);
                            break;
                    }
                }
                break;
            //diacoverym
            case R.id.search_local:
                if (((int) local.getTag()) == 0) {
                    searchLocalOrTravel = 1;
                    local.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_border_blue_cyc));
                    local.setTag(1);
                } else {
                    local.setTag(0);
                    searchLocalOrTravel = 0;
                    local.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_cyc));
                }
                travel.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_cyc));
                travel.setTag(0);
                break;
            case R.id.search_travel:
                if (((int) travel.getTag()) == 0) {
                    searchLocalOrTravel = 2;
                    travel.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_border_blue_cyc));
                    travel.setTag(1);
                } else {
                    travel.setTag(0);
                    searchLocalOrTravel = 0;
                    travel.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_cyc));
                }
                local.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_cyc));
                local.setTag(0);
                break;
            case R.id.search_del:
                disLocation.setText("");
                country = "";
                province = "";
                city = "";
                break;
            case R.id.search_location:
                //个人页面
            case R.id.search_member_location:
                Intent intent = new Intent(this, SelectCountry.class);
                intent.putExtra(SelectCountry.SELECTLOCATION, SELECTLOCATION);
                startActivityForResult(intent, SELECTLOCATION);
                break;
            case R.id.search_member_local:
                if (((int) guide.getTag()) == 0) {
                    searchLocal = true;
                    guide.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_border_blue_cyc));
                    guide.setTag(1);
                } else {
                    guide.setTag(0);
                    searchLocal = false;
                    guide.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_cyc));
                }
                break;
            case R.id.search_member_meal:
                if (((int) meal.getTag()) == 0) {
                    meal.setTag(1);
                    searchMeal = true;
                    meal.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_border_blue_cyc));
                } else {
                    meal.setTag(0);
                    searchMeal = false;
                    meal.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_cyc));
                }
                break;
            case R.id.search_member_stay:
                if (((int) stay.getTag()) == 0) {
                    stay.setTag(1);
                    searchStay = true;
                    stay.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_border_blue_cyc));
                } else {
                    stay.setTag(0);
                    searchStay = false;
                    stay.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_cyc));
                }
                break;
            case R.id.search_member_male:
                if (((int) male.getTag()) == 0) {
                    male.setTag(1);
                    male.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_border_blue_cyc));
                } else {
                    male.setTag(0);
                    male.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_cyc));
                }
                female.setTag(0);
                female.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_cyc));
                break;
            case R.id.search_member_female:
                if (((int) female.getTag()) == 0) {
                    female.setTag(1);
                    female.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_border_blue_cyc));
                } else {
                    female.setTag(0);
                    female.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_cyc));
                }
                male.setTag(0);
                male.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_cyc));
                break;
            case R.id.search_member_language:
                SearchDialog.newInstance("speak", this, language, "speak").show(getSupportFragmentManager(), "speak");
                break;
            case R.id.search_member_del1:
                memLocation.setText("");
                country = "";
                province = "";
                city = "";
                break;
            case R.id.search_member_del2:
                language.setText("");
                break;
            case R.id.search_member_del0:
                name.setText("");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECTLOCATION:
                String str = data.getStringExtra(SelectCountry.SELECTED);
                switch (which) {
                    case 1:
                        disLocation.setText(str);
                        break;
                    case 2:
                        memLocation.setText(str);
                        break;
                }

                String[] s = str.split(", ");
                switch (s.length) {
                    case 0:
                        country = "";
                        province = "";
                        city = "";
                        break;
                    case 1:
                        country = s[0];
                        break;
                    case 2:
                        country = s[0];
                        province = s[1];
                        break;
                    case 3:
                        country = s[0];
                        province = s[1];
                        city = s[2];
                        break;
                }
                break;
        }
    }

    private boolean tvSearch() {
        if (memLocation.getText().toString().isEmpty() && language.getText().toString().isEmpty() && (int) guide.getTag() == 0 && (int) meal.getTag() == 0
                && (int) stay.getTag() == 0 && (int) male.getTag() == 0 && (int) female.getTag() == 0 && name.getText().toString().trim().isEmpty()) {
            Toast.makeText(this,
                    getString(R.string.choose_condition), Toast.LENGTH_LONG)
                    .show();
            return false;
        } else {
            params.clear();
            if (!country.isEmpty())
                params.put(StringConstant.countryStr, country);
            if (!province.isEmpty())
                params.put(StringConstant.provinceStr, province);
            if (!city.isEmpty())
                params.put(StringConstant.cityStr, city);
            if (!language.getText().toString().isEmpty())
                params.put(StringConstant.speakStr, language.getText().toString());
            if (!name.getText().toString().isEmpty()) {
                if (isEmail(name.getText().toString().trim())) {
                    params.put(StringConstant.emailStr, name.getText().toString().trim());
                } else {
                    params.put(StringConstant.nicknameStr, name.getText().toString().trim());
                }
            }
            if ((int) male.getTag() != 0)
                params.put(StringConstant.genderStr, "Male");
            if ((int) female.getTag() != 0)
                params.put(StringConstant.genderStr, "Female");
            if (searchLocal) {
                params.put(StringConstant.localStr, "true");
            }
            if (searchMeal) {
                params.put(StringConstant.mealStr, "true");
            }
            if (searchStay) {
                params.put(StringConstant.stayStr, "true");
            }
            return true;
        }
    }

    private boolean isEmail(String str) {
        String strPattern = "^//s*//w+(?://.{0,1}[//w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*//.[a-zA-Z]+//s*$";

        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    //事件分发，右滑关闭本页面
    //暂未开通
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        createVelocityTracker(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN://按下
                x1 = ev.getX();
                y1 = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE://滑动
                y2 = ev.getY();
                x2 = ev.getX();
                //只判断是否为右滑
//                if (getScrollVelocity()>1000){
//                    Log.d("星期",getScrollVelocity()+"");
//                }
            case MotionEvent.ACTION_UP:
                recycleVelocityTracker();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 创建VelocityTracker对象，并将触摸界面的滑动事件加入到VelocityTracker当中。
     *
     * @param event
     */
    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 回收VelocityTracker对象。
     */
    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    /**
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。
     */
    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
//        int velocity = (int) mVelocityTracker.getYVelocity();
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

    private class GetPaths extends AsyncTask<Void, String, Boolean> {
        private String country = "", province = "", city = "";
        private int loaclOrTravel = 0;
        private HashMap<Integer, Integer> map;
        private ArrayList<String> postJson;

        public GetPaths(String country, String province, String city, int i, int page, HashMap<Integer, Integer> map) {
            this.country = country;
            this.city = city;
            this.province = province;
            loaclOrTravel = i;
            postJson = new ArrayList<>();
            this.map = new HashMap<>();
            this.map.putAll(map);
            if (page == 1) {
                progressBar.setVisibility(View.VISIBLE);
            }
            done.setClickable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ArrayList<Path> tempPathList = new ArrayList<>();
                if (map.size() > 0) {
                    Set key = activityTags.keySet();
                    Object[] post = key.toArray();
                    for (int i = 0; i < post.length; i++) {
                        postJson.add(String.valueOf((int) post[i]));
                    }
                }
                tempPathList.addAll(ServerHelper.getInstance().getPathsByLocation(
                        country, province, city, loaclOrTravel, page, postJson));

                if (!LingoXApplication.getInstance().getSkip()) {
                    for (Path path : tempPathList) {
                        User tempUser = CacheHelper.getInstance().getUserInfo(path.getUserId());
                        if (tempUser == null)
                            CacheHelper.getInstance().addUserInfo(ServerHelper.getInstance().getUserInfo(
                                    CacheHelper.getInstance().getSelfInfo().getId(), path.getUserId()
                            ));
                    }
                }
                //TODO 将数据添加到集合中
                pathList.addAll(tempPathList);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Search", "getData() error:" + e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            done.setClickable(true);
            if (success) {
                done.setText("SEARCH");
                if (pathList.size() == 0) {
                    //开始动画
                    startAnim();
                }
            } else {
                discover.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.GONE);
            disAdapter.notifyDataSetChanged();
            listView.onRefreshComplete();
        }
    }

    private class SearchUser extends AsyncTask<Void, String, Boolean> {
        public Map<String, String> localParams;
        private int searchType;

        public SearchUser(Map<String, String> p, int searchType) {
            this.localParams = p;
            this.searchType = searchType;
            done.setClickable(false);
            if (page == 1) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (LingoXApplication.getInstance().getSkip()) {
                    userList.addAll(ServerHelper.getInstance().searchUser(
                            "555e9dd4c10e00e42c16ef52",
                            searchType, localParams, page));
                } else {
                    userList.addAll(ServerHelper.getInstance().searchUser(
                            CacheHelper.getInstance().getSelfInfo().getId(),
                            searchType, localParams, page));
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            done.setClickable(true);
            if (success) {
                done.setText("SEARCH");
                if (userList.size() == 0) {
                    //开始动画
                    startAnim();
                }
            } else {
                member.setVisibility(View.VISIBLE);
                Toast.makeText(SearchActivity.this, "", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
            memAdapter.notifyDataSetChanged();
            listView.onRefreshComplete();
        }
    }
}
