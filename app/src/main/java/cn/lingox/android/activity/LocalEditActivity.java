package cn.lingox.android.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import cn.lingox.android.R;
import cn.lingox.android.activity.imagechooser.PhotoDialog;
import cn.lingox.android.activity.select_area.SelectCountry;
import cn.lingox.android.adapter.PathTagsAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.CachePath;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.PathTags;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.PathEditDialog;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.utils.CompressImageUtil;
import cn.lingox.android.utils.FileUtil;

public class LocalEditActivity extends FragmentActivity implements OnClickListener {
    //Incoming Intent Extras
    public static final String PATH_TO_EDIT = LingoXApplication.PACKAGE_NAME + ".PATH_TO_EDIT";
    //  Returning Intent Extras
    public static final String EDITED_PATH = LingoXApplication.PACKAGE_NAME + ".EDITED_PATH";
    public static final String DELETED_PATH = LingoXApplication.PACKAGE_NAME + ".DELETED_PATH";
    public static final String ADDED_PATH = LingoXApplication.PACKAGE_NAME + ".ADDED_PATH";
    public static final int SELECTDETIAL = 124;
    public static final String SELECTDETIALADD = "detial";
    public static final String SELECTDETIALLAT = "location";
    private static final String LOG_TAG = "LocalEditActivity";
    private static final int SELECTLOCATION = 123;
    private int page = 0;//当前页面
    private RelativeLayout page0, page1, page2, page3, page4, page5, page6;
    private Button next;
    private ImageView background, img, back;
    private TextView pageNum, oneTitle, twoTitle, threeTitle, fourTitle, fiveTitle;
    private Boolean addingNewPath = true;//标识是否为新建true 新建；false编辑原有的
    private Boolean imageSelected = false;
    private Path path;

    private String oldTitle = "";

    //第一页面
    private Button local, traveler;
    private LinearLayout layout;
    private TextView text1, text2, text3;
    //第二页面
    private Button countryBtn, detailAddress;
    private boolean isSelected = false;//标识经纬度是否选择 false:未选择 true：已选择
    //第三页面
    private EditText title, description;
    //第四页面
    private PathTagsAdapter adapter;
    private ArrayList<PathTags> datas;
    private int checkedNum = 0;
    private HashMap<Integer, Integer> activityTags;
    //第五页面
    private ImageView addPathImage;
    private Uri imageUri = null;
    //第六页面
    //第七页面
    private EditText availableTime;

    private Path newPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_local_edit);
        initView();
        setData();
    }

    /**
     * 实例化控件
     */
    private void initView() {
        activityTags = new HashMap<>();
        oneTitle = (TextView) findViewById(R.id.path_edit_you_are);
        twoTitle = (TextView) findViewById(R.id.path_edit_todo);
        threeTitle = (TextView) findViewById(R.id.path_edit_choose);
        fourTitle = (TextView) findViewById(R.id.path_edit_photo);
        fiveTitle = (TextView) findViewById(R.id.path_edit_time);
        next = (Button) findViewById(R.id.path_edit_next);
        next.setOnClickListener(this);

        back = (ImageView) findViewById(R.id.path_edit_back);
        back.setOnClickListener(this);
        //关闭
        findViewById(R.id.path_edit_close).setOnClickListener(this);

        background = (ImageView) findViewById(R.id.path_edit_background);
        background.setOnClickListener(this);

        img = (ImageView) findViewById(R.id.path_edit_img);
        img.setOnClickListener(this);

        pageNum = (TextView) findViewById(R.id.path_edit_num);

        page0 = (RelativeLayout) findViewById(R.id.edit_page_0);//选择local or traveler
        page1 = (RelativeLayout) findViewById(R.id.edit_page_1);// 选择国家
        page2 = (RelativeLayout) findViewById(R.id.edit_page_2);//标题、简介
        page3 = (RelativeLayout) findViewById(R.id.edit_page_3);//标签
        page4 = (RelativeLayout) findViewById(R.id.edit_page_4);//图片
        page5 = (RelativeLayout) findViewById(R.id.edit_page_5);//选择时间
        page6 = (RelativeLayout) findViewById(R.id.edit_page_6);//花费问题
        //一
        local = (Button) findViewById(R.id.path_edit_local);
        traveler = (Button) findViewById(R.id.path_edit_traveler);
        local.setOnClickListener(this);
        traveler.setOnClickListener(this);
        text1 = (TextView) findViewById(R.id.path_edit_1);
        text2 = (TextView) findViewById(R.id.path_edit_2);
        text3 = (TextView) findViewById(R.id.path_edit_3);
        layout = (LinearLayout) findViewById(R.id.zxcv);
        //二
        countryBtn = (Button) findViewById(R.id.path_edit_country);
        detailAddress = (Button) findViewById(R.id.path_detail_address);
        countryBtn.setOnClickListener(this);
        detailAddress.setOnClickListener(this);
        //三
        title = (EditText) findViewById(R.id.path_edit_title);
        description = (EditText) findViewById(R.id.path_edit_description);
        description.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {//获取焦点
                    description.setHint("");
                } else {
                    if (description.getText().length() <= 0) {
                        description.setHint(getString(R.string.description));
                    }
                }
            }
        });
        title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {//获取焦点
                    title.setHint("");
                } else {
                    if (title.getText().length() <= 0) {
                        title.setHint(getString(R.string.title));
                    }
                }
            }
        });

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                path.setTitle(s.toString());
            }
        });
        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                path.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        //四
        ListView listView = (ListView) findViewById(R.id.path_edit_listview);
        datas = new ArrayList<>();
        datas.addAll(LingoXApplication.getInstance().getDatas());
        adapter = new PathTagsAdapter(this, datas, 0);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (datas.get(position).getType() == 0) {
                    if (checkedNum < 3) {
                        activityTags.put(position, 1);
                        checkedNum++;
                        datas.get(position).setType(1);
                    }
                } else {
                    checkedNum--;
                    activityTags.remove(position);
                    datas.get(position).setType(0);
                }
                adapter.notifyDataSetChanged();
            }
        });
        //五
        availableTime = (EditText) findViewById(R.id.availavle_time);
        availableTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                path.setAvailableTime(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        //图片
        addPathImage = (ImageView) findViewById(R.id.path_edit_choose_photo);
        addPathImage.measure(0, 0);
        ViewGroup.LayoutParams lp = addPathImage.getLayoutParams();
        lp.height = (int) (LingoXApplication.getInstance().getWidth() * 0.6);
        lp.width = (int) (LingoXApplication.getInstance().getWidth() * 0.6);
        addPathImage.setLayoutParams(lp);
        addPathImage.setOnClickListener(this);
    }

    /**
     * 设置数据
     */
    private void setData() {
        Intent intent = getIntent();
        if (intent.hasExtra(PATH_TO_EDIT)) {
            addingNewPath = false;
            path = intent.getParcelableExtra(PATH_TO_EDIT);
            imageSelected = !TextUtils.isEmpty(path.getImage());

            oldTitle = path.getTitle();
        } else {
            addingNewPath = true;
            path = new Path();
            path.setType(1);//本地人
            setLocalOrTraveler();
            path.setUserId(CacheHelper.getInstance().getSelfInfo().getId());

            if (CachePath.getInstance().getLocalOrTraveler() != 3) {
                path.setType(CachePath.getInstance().getLocalOrTraveler());
                setLocalOrTraveler();
                if (!CachePath.getInstance().getTitle().isEmpty()) {
                    path.setTitle(CachePath.getInstance().getTitle());
                    UIHelper.getInstance().textViewSetPossiblyNullString(title, path.getTitle());
                }
                if (!CachePath.getInstance().getLocation().isEmpty()) {
                    path.setLocation(CachePath.getInstance().getLocation());
                    UIHelper.getInstance().textViewSetPossiblyNullString(countryBtn, path.getLocation());
                }
                if (!CachePath.getInstance().getDescription().isEmpty()) {
                    path.setText(CachePath.getInstance().getDescription());
                    UIHelper.getInstance().textViewSetPossiblyNullString(description, path.getText());
                }
                if (CachePath.getInstance().getPhoto()) {
                    path.setImage(CachePath.getInstance().getImage());
                    UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, addPathImage, path.getImage(), "circular");
                }
                if (CachePath.getInstance().getAvabilableTime().isEmpty()) {
                    path.setAvailableTime(CachePath.getInstance().getAvabilableTime());
                    UIHelper.getInstance().textViewSetPossiblyNullString(availableTime,
                            path.getAvailableTime());
                }
                getTags(CachePath.getInstance().getTags());

                if (!CachePath.getInstance().getAddress().isEmpty() && path.getType() == 2) {
                    path.setDetailAddress("");
                }
            } else {
                //不保存新建活动的信息
                CachePath.getInstance().setNothing();
            }
        }
        if (!addingNewPath) {
            //一
            setLocalOrTraveler();
            //二
            //三
            getTags(path.getTags());
            //四
            UIHelper.getInstance().textViewSetPossiblyNullString(title, path.getTitle());
            UIHelper.getInstance().textViewSetPossiblyNullString(countryBtn, path.getLocation());
            if ("China".equals(path.getChosenCountry())) {
                detailAddress.setVisibility(View.VISIBLE);
                detailAddress.setText(path.getDetailAddress());
                isSelected = true;
            }
            UIHelper.getInstance().textViewSetPossiblyNullString(description, path.getText());
            //五
            if (FileUtil.getImg(path.getImage(), this) != null) {
                addPathImage.setImageBitmap(FileUtil.getImg(path.getImage(), this));
            } else {
                UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, addPathImage, path.getImage(), "original");
            }
            //六
            UIHelper.getInstance().textViewSetPossiblyNullString(availableTime, path.getAvailableTime());
        }
    }

    /**
     * 设置标签数据源
     *
     * @param list 从本地或活动获取标签数据集合
     */
    private void getTags(ArrayList<String> list) {
        int temp;
        for (int i = 0, j = list.size(); i < j; i++) {
            temp = Integer.valueOf(list.get(i));
            activityTags.put(temp, 1);
            if (checkedNum < 3) {
                checkedNum++;
                datas.get(temp).setType(1);
            }
        }
        adapter.notifyDataSetChanged();
    }

    //设置本地人和旅行者相应显示内容
    private void setLocalOrTraveler() {
        layout.setVisibility(View.VISIBLE);
        if (path.getType() == 1) {
            local.setBackgroundResource(R.drawable.button_border_orange);
            traveler.setBackgroundResource(R.drawable.button_border_blue);
            text1.setText(getString(R.string.path_edit_0_local_1));
            text2.setText(getString(R.string.path_edit_0_local_2));
            text3.setText(getString(R.string.path_edit_0_local_3));
        } else {
            background.setBackgroundResource(R.drawable.active_background_02_320dp520dp);
            traveler.setBackgroundResource(R.drawable.button_border_orange);
            local.setBackgroundResource(R.drawable.button_border_blue);
            text1.setText(getString(R.string.path_edit_0_traveler_1));
            text2.setText(getString(R.string.path_edit_0_traveler_2));
            text3.setText(getString(R.string.path_edit_0_traveler_3));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.path_edit_next://0 下一页 1返回
                nextClick(0);
                break;
            case R.id.path_edit_close:
                if (page == 3) {
                    saveTags();
                }
                PathEditDialog.getInstance().showDialog(this, path).show();
                break;
            case R.id.path_edit_back:
                nextClick(1);
                break;
            case R.id.path_edit_local:
                MobclickAgent.onEvent(this, "add_discover_local");
                path.setType(1);//本地人
                setLocalOrTraveler();
                break;
            case R.id.path_edit_traveler:
                MobclickAgent.onEvent(this, "add_discover_traveler");
                path.setType(2);//旅行者
                background.setBackgroundResource(R.drawable.active_background_02_320dp520dp);
                setLocalOrTraveler();
                break;
            case R.id.path_edit_country:
                Intent intent = new Intent(this, SelectCountry.class);
                intent.putExtra(SelectCountry.SELECTLOCATION, SELECTLOCATION);
                startActivityForResult(intent, SELECTLOCATION);
                break;
            case R.id.path_detail_address:
                Intent intent1 = new Intent(this, AMapActivity.class);
                if (!path.getLatitude().isEmpty() && !path.getLongitude().isEmpty()) {
                    String[] doubles = {path.getLatitude(), path.getLongitude()};
                    intent1.putExtra("String", doubles);
                }
                startActivityForResult(intent1, SELECTDETIAL);
                break;
            case R.id.path_edit_choose_photo:
                Intent intent4 = new Intent(this, PhotoDialog.class);
                intent4.putExtra(PhotoDialog.REQUESTED_IMAGE, PhotoDialog.REQUEST_CARD_IMAGE);
                startActivityForResult(intent4, PhotoDialog.REQUEST_CARD_IMAGE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECTLOCATION://获取国家、省份、城市
                String str = data.getStringExtra(SelectCountry.SELECTED);
                if (!str.isEmpty()) {
                    path.setLocation(str);
                    CachePath.getInstance().setLocation(str);
                    countryBtn.setText(str);
                    if ("China".equals(path.getChosenCountry())) {
                        //如果选择的是中国，显示地图按钮
                        detailAddress.setVisibility(View.VISIBLE);
                    } else {
                        detailAddress.setVisibility(View.GONE);
                        path.setDetailAddress("");
                        detailAddress.setText("");
                        detailAddress.setHint("Detail Address");
                    }
                }
                break;
            case SELECTDETIAL://获取经纬度-----如果选择的是中国
                double[] doubles = data.getDoubleArrayExtra(SELECTDETIALLAT);
                String add = data.getStringExtra(SELECTDETIALADD);
                if (!add.isEmpty()) {
                    path.setDetailAddress(add);
                    detailAddress.setText(add);
                }
                if (doubles.length > 0) {
                    path.setLongitude(String.valueOf(doubles[0]));//经度
                    path.setLatitude(String.valueOf(doubles[1]));//纬度
                    isSelected = false;
                }
                break;
            case PhotoDialog.REQUEST_CARD_IMAGE:
                if (resultCode != RESULT_OK) {
                    Log.d(LOG_TAG, "onActivityResult -> PHOTO_RESULT -> not RESULT_OK");
                } else {
                    if (data.hasExtra(PhotoDialog.SELECTED_SINGLE_IMAGE)) {
                        imageUri = data.getParcelableExtra(PhotoDialog.SELECTED_SINGLE_IMAGE);
                        addPathImage.setImageBitmap(FileUtil.getImg(imageUri.getPath(), this));
                        path.setImage("");
                        imageSelected = true;
                    } else if (data.hasExtra(PathCardImgDialog.PRESET_URI)) {
                        imageUri = null;
                        String presetImageUrl = data.getStringExtra(PathCardImgDialog.PRESET_URI);
                        Picasso.with(this).load(presetImageUrl).into(addPathImage);
                        path.setImage(presetImageUrl);
                        imageSelected = true;
                    }
                }
                //将新建的活动缓存
                CachePath.getInstance().setPhoto(true);
                break;
        }
    }

    /**
     * 0 on behalf of the next
     * 1 on behalf of the back
     * page0-->local or traveler
     * page1-->选择地点
     * page5-->选择时间
     * page2-->标题
     * page3-->标签
     * page4-->选择图片
     * page6-->花费问题
     * <p/>
     * page0-->page2-->page1-->page5-->page3-->page6-->page4
     * <p/>
     * 注意：现在只提供local发布活动的流程
     *
     * @param nextOrBack on behalf of the page number
     */
    private void nextClick(int nextOrBack) {
        if (nextOrBack == 0) {
            page++;
        } else if (nextOrBack == 1) {
            page--;
        }
        if (page > 0) {
            switch (page) {
                case 1://显示本地人  page0
                    //page0  -->page2-->page1-->page5-->page3-->page6-->page4

                    pageNum.setText("1/6");
                    oneTitle.setText(getString(R.string.local_one));
                    twoTitle.setText(getString(R.string.local_two));
                    threeTitle.setText(getString(R.string.local_three));
                    fourTitle.setText(getString(R.string.local_four));
                    fiveTitle.setText(getString(R.string.local_five));

                    background.setBackgroundResource(R.drawable.active_map_01_320dp520dp);
                    page2.setVisibility(View.VISIBLE);
                    page0.setVisibility(View.INVISIBLE);
                    page5.setVisibility(View.INVISIBLE);
                    page1.setVisibility(View.INVISIBLE);
                    page6.setVisibility(View.INVISIBLE);
                    break;
                case 2://title text  page2
                    //page0-->page2  -->page1-->page5-->page3-->page6-->page4

                    if (path.getTitle().isEmpty()) {
                        showToast("Please give a title to your local experience.");
                        page--;
                    } else if (path.getText().isEmpty()) {
                        showToast("Please tell travelers more details about your local experience.");
                        page--;
                    } else if (path.getText().length() < 100) {
                        //控制录入的字符不少于100个
                        showToast("Please enter a description no less than 100 letters");
                        page--;
                    } else {
                        pageNum.setText("2/6");
                        background.setBackgroundResource(R.drawable.active_map_02_320dp520dp);
                        page1.setVisibility(View.VISIBLE);
                        page2.setVisibility(View.INVISIBLE);
                        page5.setVisibility(View.INVISIBLE);
                        page6.setVisibility(View.INVISIBLE);

                    }
                    break;
                case 3://country city address  page1
                    //page0-->page2-->page1  -->page5-->page3-->page6-->page4

                    if (path.getChosenCountry().isEmpty()) {
                        showToast("Please choose a location for this local experience.");
                        page--;
                    } else if ("China".equals(path.getChosenCountry().trim()) && !isSelected) {
                        showToast("Please select a location in the map.");
                        page--;
                    } else {
                        pageNum.setText("3/6");
                        availableTime.setVisibility(View.VISIBLE);
                        path.setEndDateTime(0);
                        path.setDateTime(0);
                        background.setBackgroundResource(R.drawable.active_map_05_320dp520dp);
                        page5.setVisibility(View.VISIBLE);
                        page1.setVisibility(View.INVISIBLE);
                        page2.setVisibility(View.INVISIBLE);
                        page3.setVisibility(View.INVISIBLE);
                        page6.setVisibility(View.INVISIBLE);

                    }
                    break;
                case 4://tag  page3
                    //page0-->page2-->page1-->page5  -->page3-->page6-->page4

                    if (path.getAvailableTime().isEmpty()) {
                        showToast("Please choose your available time for this local experience.");
                        page--;
                    } else {
                        pageNum.setText("4/6");
                        background.setBackgroundResource(R.drawable.active_map_03_320dp520dp);
                        page3.setVisibility(View.VISIBLE);
                        page2.setVisibility(View.INVISIBLE);
                        page4.setVisibility(View.INVISIBLE);
                        page5.setVisibility(View.INVISIBLE);
                        page6.setVisibility(View.INVISIBLE);

                    }
                    break;
                case 5://花费问题 page6
                    //page0-->page2-->page1-->page5-->page3  -->page6-->page4
                    if (!saveTags()) {
                        showToast("Please choose the type(s) of your local experience.");
                        page--;
                    } else {
                        pageNum.setText("5/6");
                        background.setBackgroundResource(R.drawable.active_map_06_320dp520dp);
                        page6.setVisibility(View.VISIBLE);
                        page4.setVisibility(View.INVISIBLE);
                        page3.setVisibility(View.INVISIBLE);
                    }
                    break;
                case 6://photo  page4
                    //page0-->page1-->page5-->page2-->page3-->page6  -->page4

                    if (false) {//暂时未定判断条件
                        showToast("Please choose the type(s) of your local experience.");
                        page--;
                    } else {
                        pageNum.setText("6/6");
                        background.setBackgroundResource(R.drawable.active_map_04_320dp520dp);
                        page4.setVisibility(View.VISIBLE);
                        page6.setVisibility(View.INVISIBLE);
                    }
                    break;
                case 7:
                    if (imageSelected) {
                        if (addingNewPath) {
                            new AddPath().execute();
                        } else {
                            new EditPath().execute();
                        }
                    } else {
                        showToast("Please choose a picture of your local experience. ");
                        page--;
                    }
                    break;
            }
            if (path.getType() != 0) {
                img.setVisibility(View.INVISIBLE);
                back.setVisibility(View.VISIBLE);
                pageNum.setVisibility(View.VISIBLE);
            }
            if (page >= 6) {
                next.setText(getString(R.string.create));
            } else {
                next.setText(getString(R.string.path_edit_next));
            }
        } else {
            if (page == 0) {
                img.setVisibility(View.VISIBLE);
                back.setVisibility(View.INVISIBLE);
                pageNum.setVisibility(View.INVISIBLE);
                background.setBackgroundResource(R.drawable.active_background_01_320dp520dp);
                page0.setVisibility(View.VISIBLE);
                page1.setVisibility(View.INVISIBLE);
                page2.setVisibility(View.INVISIBLE);
                page3.setVisibility(View.INVISIBLE);
                page4.setVisibility(View.INVISIBLE);
                page5.setVisibility(View.INVISIBLE);
            } else if (page < 0) {
                finish();
            }
        }
    }

    /**
     * 保存活动标签
     *
     * @return true:选择至少一个标签，false:未选择标签
     */
    private boolean saveTags() {
        if (activityTags.size() > 0) {//标签被选择
            Set key = activityTags.keySet();
            Object[] post = key.toArray();
            ArrayList<String> postJson = new ArrayList<>();
            for (Object aPost : post) {
                postJson.add(String.valueOf((int) aPost));
            }
            CachePath.getInstance().setTags(postJson);
            path.setTags(postJson);
        }
        return path.getTags().size() > 0;
    }

    //提示用户
    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            nextClick(1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        HashMap<String, String> map = new HashMap<>();
        map.put("next", "page");
        MobclickAgent.onEventValue(this, "add_discover_next", map, page);
    }

    private class AddPath extends AsyncTask<Void, String, Boolean> {
        private ProgressDialog pd = new ProgressDialog(LocalEditActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Creating Activity...");
            pd.setCancelable(false);
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            next.setClickable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            /*Create Huan Xin Group Chat*/
            try {
                //环信群————创建
                EMGroup emGroup = EMGroupManager.getInstance().createPublicGroup
                        (path.getTitle(), path.getText(), null, false);
                if (emGroup != null) {
                    String groupId = emGroup.getGroupId();
                    path.setHxGroupId(groupId);
                }
            } catch (final EaseMobException e) {
                e.printStackTrace();
            }
            Boolean success = false;
            try {
                Collections.sort(path.getTags());
                newPath = ServerHelper.getInstance().path("create", path);
                success = true;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
                publishProgress(null, "Error creating Activity");
            }
            try {
                if (imageUri != null) {
                    ArrayList<String> list = new ArrayList<>();
                    list.addAll(ServerHelper.getInstance().uploadPathImage(newPath.getId(),
                            CompressImageUtil.compressImage(FileUtil.getImg(imageUri.getPath(), LocalEditActivity.this))));
                    if (list.size() > 0) {
                        newPath.setImage(list.get(0));
                        newPath.setImage21(list.get(2));
                        newPath.setImage11(list.get(1));
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
                publishProgress(null, "Error uploading image to Activity");
            }
            return success;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null)
                pd.setMessage(values[0]);
            if (values[1] != null)
                Toast.makeText(LocalEditActivity.this, values[1], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            next.setClickable(true);
            if (success) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(ADDED_PATH, newPath);
                setResult(RESULT_OK, returnIntent);
                finish();
            } else {
                page--;
            }
        }
    }

    private class EditPath extends AsyncTask<Void, String, Boolean> {
        private ProgressDialog pd = new ProgressDialog(LocalEditActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Modifying Activity...");
            pd.setCancelable(false);
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            next.setClickable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (TextUtils.isEmpty(path.getHxGroupId())) {
                    EMGroup emGroup = EMGroupManager.getInstance().createPublicGroup
                            (path.getTitle(), path.getText(), null, false);
                    if (emGroup != null) {
                        String groupId = emGroup.getGroupId();
                        path.setHxGroupId(groupId);
                    }
                } else if (!oldTitle.contentEquals(path.getTitle())) {
                    //groupId 需要改变名称的群组的id
                    //changedGroupName 改变后的群组名称
                    //需异步处理
                    EMGroupManager.getInstance().changeGroupName(path.getHxGroupId(), path.getTitle());
                }
            } catch (EaseMobException e) {
                e.printStackTrace();
            }
            try {
                newPath = ServerHelper.getInstance().path("edit", path);
                if (imageUri != null) {
                    ArrayList<String> list = new ArrayList<>();
                    list.addAll(ServerHelper.getInstance().uploadPathImage(newPath.getId(),
                            CompressImageUtil.compressImage(FileUtil.getImg(imageUri.getPath(), LocalEditActivity.this))));
                    if (list.size() > 0) {
                        newPath.setImage(list.get(0));
                        newPath.setImage21(list.get(2));
                        newPath.setImage11(list.get(1));
                    }
                }
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
                publishProgress(null, "Error modifying Activity " + e.toString());
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null)
                pd.setMessage(values[0]);
            if (values[1] != null)
                Toast.makeText(LocalEditActivity.this, values[1], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            next.setClickable(true);
            if (success) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(LocalViewActivity.EDITED_PATH, newPath);
                setResult(RESULT_OK, returnIntent);
                finish();
            } else {
                page--;
            }
        }
    }
}