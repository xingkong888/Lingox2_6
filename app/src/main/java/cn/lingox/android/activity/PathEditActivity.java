package cn.lingox.android.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import cn.lingox.android.R;
import cn.lingox.android.activity.imagechooser.PhotoDialog;
import cn.lingox.android.activity.select_area.SelectCountry;
import cn.lingox.android.adapter.MyAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.CachePath;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.PathTags;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.PathEditDialog;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.utils.FileUtil;

public class PathEditActivity extends FragmentActivity implements OnClickListener {
    //Incoming Intent Extras
    public static final String PATH_TO_EDIT = LingoXApplication.PACKAGE_NAME + ".PATH_TO_EDIT";
    //  Returning Intent Extras
    public static final String EDITED_PATH = LingoXApplication.PACKAGE_NAME + ".EDITED_PATH";
    public static final String DELETED_PATH = LingoXApplication.PACKAGE_NAME + ".DELETED_PATH";
    public static final String ADDED_PATH = LingoXApplication.PACKAGE_NAME + ".ADDED_PATH";
    public static final int SELECTDETIAL = 124;
    public static final String SELECTDETIALADD = "detial";
    public static final String SELECTDETIALLAT = "location";
    private static final String LOG_TAG = "PathEditActivity";
    private static final int SELECTLOCATION = 123;
    private int page = 0;//当前页面
    private RelativeLayout page0, page1, page2, page3, page4, page5, page6, page7;
    private Button next;
    private ImageView background, img, back, close;
    private TextView pageNum, oneTitle, twoTitle, threeTitle, fourTitle, fiveTitle;
    private Boolean addingNewPath = true;
    private Boolean imageSelected = false;
    private Path path;

    private String oldTitle = "";

    //第一页面
    private Button local, traveler;
    private LinearLayout layout;
    private TextView text1, text2, text3;
    //第二页面
    private Button countryBtn, detailAddress;
    //第三页面
    private EditText title, description;
    //第四页面
    private ListView listView;
    private MyAdapter adapter;
    private ArrayList<PathTags> datas;
    private int checkedNum = 0;
    private ArrayList<String> postJson;
    private HashMap<Integer, Integer> activityTags;
    //第五页面
    private ImageView addPathImage;
    private Uri imageUri = null;
    //第六页面
    private long start = 0, end = 0, now = System.currentTimeMillis() / 1000L;
    private Calendar calendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day);
            startTimePickerDialog();
        }
    };
    private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day);
            endTimePickerDialog();
        }
    };
    TimePickerDialog.OnTimeSetListener endTimePicked = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int h, int m) {
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            end = calendar.getTimeInMillis() / 1000L;
            if (end - now >= 0 && (start == 0 ? true : end >= start)) {
                UIHelper.getInstance().textViewSetPossiblyNullString(
                        endTime, JsonHelper.getInstance().parseTimestamp((int) end));
                path.setEndDateTime((int) end);
            } else {
                Toast.makeText(PathEditActivity.this, getString(R.string.end_start), Toast.LENGTH_SHORT).show();
                endDatePickerDialog();
            }
        }
    };
    private Button startTime, endTime;
    TimePickerDialog.OnTimeSetListener startTimePicked = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int h, int m) {
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            start = calendar.getTimeInMillis() / 1000L;
            if (end == 0 ? true : end >= start) {
                UIHelper.getInstance().textViewSetPossiblyNullString(
                        startTime, JsonHelper.getInstance().parseTimestamp((int) start));

                path.setDateTime((int) start);
            } else {
                Toast.makeText(PathEditActivity.this, getString(R.string.start_end), Toast.LENGTH_SHORT).show();
                startDatePickerDialog();
            }
        }
    };
    //第七页面
    private EditText availableTime;
    //第八页面
    private EditText groupSize, budget;
    private Path newPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_path_edit);
        initView();
        setData();
    }

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

        close = (ImageView) findViewById(R.id.path_edit_close);
        close.setOnClickListener(this);

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
        page6 = (RelativeLayout) findViewById(R.id.edit_page_6);//暂无
        page7 = (RelativeLayout) findViewById(R.id.edit_page_7);//暂无
        //一
        local = (Button) findViewById(R.id.path_edit_local);
        traveler = (Button) findViewById(R.id.path_edit_traveler);

        local.setOnClickListener(this);
        traveler.setOnClickListener(this);

        text1 = (TextView) findViewById(R.id.path_edit_1);
        text2 = (TextView) findViewById(R.id.path_edit_2);
        text3 = (TextView) findViewById(R.id.path_edit_3);

        layout = (LinearLayout) findViewById(R.id.zxcv);
        layout.setVisibility(View.INVISIBLE);
        //二
        countryBtn = (Button) findViewById(R.id.path_edit_country);
        detailAddress = (Button) findViewById(R.id.path_detail_address);
        countryBtn.setOnClickListener(this);
        detailAddress.setOnClickListener(this);
        //三
        title = (EditText) findViewById(R.id.path_edit_title);
        description = (EditText) findViewById(R.id.path_edit_description);

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
        listView = (ListView) findViewById(R.id.path_edit_listview);
        datas = new ArrayList<>();
        datas = LingoXApplication.getInstance().getDatas();
        adapter = new MyAdapter(this, datas, 0);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        //五
        addPathImage = (ImageView) findViewById(R.id.path_edit_choose_photo);
        addPathImage.measure(0, 0);
        ViewGroup.LayoutParams lp = addPathImage.getLayoutParams();
        lp.height = (int) (LingoXApplication.getInstance().getWidth() * 0.6);
        lp.width = (int) (LingoXApplication.getInstance().getWidth() * 0.6);
        addPathImage.setLayoutParams(lp);
        addPathImage.setOnClickListener(this);
        //六
        startTime = (Button) findViewById(R.id.path_edit_start_time);
        endTime = (Button) findViewById(R.id.path_edit_end_time);
        availableTime = (EditText) findViewById(R.id.availavle_time);
        startTime.setOnClickListener(this);
        endTime.setOnClickListener(this);
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
        //八
        groupSize = (EditText) findViewById(R.id.path_edit_group_size);
        groupSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    path.setCapacity(Integer.valueOf(s.toString()));
                }
            }
        });
        budget = (EditText) findViewById(R.id.path_edit_budget);
        budget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                path.setCost(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setData() {
        Intent intent = getIntent();
        if (intent.hasExtra(PATH_TO_EDIT)) {
            addingNewPath = false;
            path = intent.getParcelableExtra(PATH_TO_EDIT);
            imageSelected = !TextUtils.isEmpty(path.getImage());

            oldTitle = path.getTitle();
            start = path.getDateTime();
            end = path.getEndDateTime();
            //二
        } else {
            addingNewPath = true;
            path = new Path();
            path.setUserId(CacheHelper.getInstance().getSelfInfo().getId());

            if (CachePath.getInstance().getLocalOrTraveler() != 3) {
                path.setType(CachePath.getInstance().getLocalOrTraveler());
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
                if (!CachePath.getInstance().getTitle().isEmpty()) {
                    path.setTitle(CachePath.getInstance().getTitle());
                    UIHelper.getInstance().textViewSetPossiblyNullString(title, path.getTitle());
                }
                if (!CachePath.getInstance().getDescription().isEmpty()) {
                    path.setText(CachePath.getInstance().getDescription());
                    UIHelper.getInstance().textViewSetPossiblyNullString(description, path.getText());
                }
                if (CachePath.getInstance().getPhoto()) {
                    path.setImage(CachePath.getInstance().getImage());
                    UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, addPathImage, path.getImage());
                }
                if (CachePath.getInstance().getStartTime() != 0) {
                    path.setDateTime(CachePath.getInstance().getStartTime());
                    UIHelper.getInstance().textViewSetPossiblyNullString(startTime,
                            JsonHelper.getInstance().parseTimestamp(path.getDateTime()));
                }
                if (CachePath.getInstance().getEndTime() != 0) {
                    path.setEndDateTime(CachePath.getInstance().getEndTime());
                    UIHelper.getInstance().textViewSetPossiblyNullString(endTime,
                            JsonHelper.getInstance().parseTimestamp(path.getEndDateTime()));
                }
                if (CachePath.getInstance().getAvabilableTime().isEmpty()) {
                    path.setAvailableTime(CachePath.getInstance().getAvabilableTime());
                    UIHelper.getInstance().textViewSetPossiblyNullString(availableTime,
                            path.getAvailableTime());
                }

                if (CachePath.getInstance().getTags().size() > 0) {
                    for (int i = 0; i < CachePath.getInstance().getTags().size(); i++) {
                        activityTags.put(Integer.valueOf(CachePath.getInstance().getTags().get(i)), 1);
                        if (checkedNum < 3) {
                            checkedNum++;
                            datas.get(Integer.valueOf(CachePath.getInstance().getTags().get(i))).setType(1);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                if (!CachePath.getInstance().getAddress().isEmpty()) {
                    if (path.getType() == 2) {
                        path.setDetailAddress("");
                    }
                }
            } else {
                CachePath.getInstance().setNothing();
            }
        }
        if (!addingNewPath) {
            //一
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
            //二
            //三 在另一个地方实现
            if (path.getTags().size() > 0) {
                for (int i = 0; i < path.getTags().size(); i++) {
                    activityTags.put(Integer.valueOf(path.getTags().get(i)), 1);
                    if (checkedNum < 3) {
                        checkedNum++;
                        datas.get(Integer.valueOf(path.getTags().get(i))).setType(1);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            //四
            UIHelper.getInstance().textViewSetPossiblyNullString(title, path.getTitle());
            UIHelper.getInstance().textViewSetPossiblyNullString(description, path.getText());
            //五
            if (FileUtil.getImg(path.getImage()) != null) {
                addPathImage.setImageBitmap(FileUtil.getImg(path.getImage()));
            } else {
                UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, addPathImage, path.getImage());
            }
            //六
            UIHelper.getInstance().textViewSetPossiblyNullString(startTime,
                    JsonHelper.getInstance().parseTimestamp(path.getDateTime()));
            UIHelper.getInstance().textViewSetPossiblyNullString(endTime,
                    JsonHelper.getInstance().parseTimestamp(path.getEndDateTime()));
            UIHelper.getInstance().textViewSetPossiblyNullString(availableTime, path.getAvailableTime());
//            //七
//            address.setText(path.getDetailAddress());
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
                layout.setVisibility(View.VISIBLE);
                background.setBackgroundResource(R.drawable.active_background_01_320dp520dp);
                local.setBackgroundResource(R.drawable.button_border_orange);
                traveler.setBackgroundResource(R.drawable.button_border_blue);

                text1.setText(getString(R.string.path_edit_0_traveler_1));
                text2.setText(getString(R.string.path_edit_0_traveler_2));
                text3.setText(getString(R.string.path_edit_0_traveler_3));
                break;
            case R.id.path_edit_traveler:
                MobclickAgent.onEvent(this, "add_discover_traveler");
                path.setType(2);//旅行者
                background.setBackgroundResource(R.drawable.active_background_02_320dp520dp);
                layout.setVisibility(View.VISIBLE);
                traveler.setBackgroundResource(R.drawable.button_border_orange);
                local.setBackgroundResource(R.drawable.button_border_blue);

                text1.setText(getString(R.string.path_edit_0_local_1));
                text2.setText(getString(R.string.path_edit_0_local_2));
                text3.setText(getString(R.string.path_edit_0_local_3));
                break;
            case R.id.path_edit_country:
//                Intent intent = new Intent(this, AMapActivity.class);
//                startActivity(intent);
                Intent intent = new Intent(this, SelectCountry.class);
                intent.putExtra(SelectCountry.SELECTLOCATION, SELECTLOCATION);
                startActivityForResult(intent, SELECTLOCATION);
                break;
            case R.id.path_detail_address:
                Intent intent1 = new Intent(this, AMapActivity.class);
                startActivityForResult(intent1, SELECTDETIAL);
                break;
            case R.id.path_edit_choose_photo:
                Intent intent4 = new Intent(this, PhotoDialog.class);
                intent4.putExtra(PhotoDialog.REQUESTED_IMAGE, PhotoDialog.REQUEST_CARD_IMAGE);
                startActivityForResult(intent4, PhotoDialog.REQUEST_CARD_IMAGE);
                break;
            case R.id.path_edit_start_time:
                startDatePickerDialog();
                break;
            case R.id.path_edit_end_time:
                endDatePickerDialog();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECTLOCATION:
                String str = data.getStringExtra(SelectCountry.SELECTED);
                if (!str.isEmpty()) {
                    path.setLocation(str);
                    CachePath.getInstance().setLocation(str);
                    countryBtn.setText(str);
                }
                break;
            case SELECTDETIAL:
//                if (data.hasExtra(SELECTDETIALADD)) {
                double[] doubles = data.getDoubleArrayExtra(SELECTDETIALLAT);
                String add = data.getStringExtra(SELECTDETIALADD);
//                    Toast.makeText(this,doubles[0]+">>>"+doubles[1]+">>>"+add,Toast.LENGTH_LONG).show();
                    if (!add.isEmpty()) {
                        path.setDetailAddress(add);
                        detailAddress.setText(add);
                    }
                    if (doubles.length > 0) {
                        path.setLongitude(String.valueOf(doubles[0]));//经度
                        path.setLatitude(String.valueOf(doubles[1]));//纬度
                    }
//                }
                break;
            case PhotoDialog.REQUEST_CARD_IMAGE:
                if (resultCode != RESULT_OK)
                    Log.d(LOG_TAG, "onActivityResult -> PHOTO_RESULT -> not RESULT_OK");
                else {
                    if (data.hasExtra(PhotoDialog.SELECTED_SINGLE_IMAGE)) {
                        imageUri = data.getParcelableExtra(PhotoDialog.SELECTED_SINGLE_IMAGE);
                        addPathImage.setImageBitmap(FileUtil.getImg(imageUri.getPath()));
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
                CachePath.getInstance().setPhoto(true);
                break;
        }
    }

    /**
     * 0��ʾnext
     * 1��ʾback
     * page0-->local or traveler
     * page1-->选择地点
     * page5-->选择时间
     * page2-->标题
     * page3-->标签
     * page4-->选择图片
     *
     * @param nextOrBack
     */
    private void nextClick(int nextOrBack) {
        if (nextOrBack == 0) {
            page++;
        } else if (nextOrBack == 1) {
            page--;
        }
        if (page > 0) {
            switch (page) {
                case 1://country city address
                    switch (path.getType()) {
                        case 0:
                            page--;
                            break;
                        case 1://本地人
                            pageNum.setText("1/5");
                            oneTitle.setText("Where is this experience?");
                            twoTitle.setText("What local experience do you want to share with travelers?");
                            threeTitle.setText("Please choose the type(s) of your local experience");
                            fourTitle.setText("Is there a cover photo?");
                            fiveTitle.setText("When are you usually available for this local experience? ");

                            detailAddress.setVisibility(View.VISIBLE);

                            //page0  -->page2-->page1-->page5-->page3-->page4
                            background.setBackgroundResource(R.drawable.active_map_01_320dp520dp);
                            page2.setVisibility(View.VISIBLE);
                            page0.setVisibility(View.INVISIBLE);
                            page5.setVisibility(View.INVISIBLE);
                            page1.setVisibility(View.INVISIBLE);
                            break;
                        case 2://旅行者
                            pageNum.setText("1/5");
                            oneTitle.setText("Where are you going?");
                            twoTitle.setText("What do you want to experience with locals?");
                            threeTitle.setText("Please choose the type(s) of the experience");
                            fourTitle.setText("Is there a cover photo?");
                            fiveTitle.setText("Set a time frame for your travel");

                            detailAddress.setVisibility(View.INVISIBLE);

                            //page0  -->page1-->page5-->page2-->page3-->page4
                            background.setBackgroundResource(R.drawable.active_map_01_320dp520dp);
                            page1.setVisibility(View.VISIBLE);
                            page0.setVisibility(View.INVISIBLE);
                            page5.setVisibility(View.INVISIBLE);
                            page2.setVisibility(View.INVISIBLE);
                            break;
                    }
                    break;
                case 2://time

                    switch (path.getType()) {
                        case 1:
                            if (path.getTitle().isEmpty() || path.getText().isEmpty()) {
                                page--;
                            } else {

                                pageNum.setText("2/5");
                                //page0-->  page2  -->page1-->page5-->page3-->page4
                                background.setBackgroundResource(R.drawable.active_map_02_320dp520dp);
                                page1.setVisibility(View.VISIBLE);
                                page2.setVisibility(View.INVISIBLE);
                                page5.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case 2:
                            if (path.getChosenCountry().isEmpty()) {
                                page--;
                            } else {
                                pageNum.setText("2/5");
                                availableTime.setVisibility(View.GONE);
                                startTime.setVisibility(View.VISIBLE);
                                endTime.setVisibility(View.VISIBLE);
                                path.setAvailableTime("");
                                //page0-->page1-->  page5  -->page2-->page3-->page4
                                background.setBackgroundResource(R.drawable.active_map_05_320dp520dp);
                                page5.setVisibility(View.VISIBLE);
                                page1.setVisibility(View.INVISIBLE);
                                page2.setVisibility(View.INVISIBLE);
                            }
                            break;
                    }
                    break;
                case 3://title text
                    switch (path.getType()) {
                        case 1:
                            if (path.getChosenCountry().isEmpty()) {
                                page--;
                            } else {
                                pageNum.setText("3/5");
                                availableTime.setVisibility(View.VISIBLE);
                                startTime.setVisibility(View.GONE);
                                endTime.setVisibility(View.GONE);
                                path.setEndDateTime(0);
                                path.setDateTime(0);
                                //page0-->page2-->  page1  -->page5-->page3-->page4
                                background.setBackgroundResource(R.drawable.active_map_05_320dp520dp);
                                page5.setVisibility(View.VISIBLE);
                                page1.setVisibility(View.INVISIBLE);
                                page2.setVisibility(View.INVISIBLE);
                                page3.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case 2:
                            if ((path.getDateTime() == 0 || path.getEndDateTime() == 0)) {
                                page--;
                            } else {
                                pageNum.setText("3/5");
                                //page0-->page1-->page5-->  page2  -->page3-->page4
                                background.setBackgroundResource(R.drawable.active_map_02_320dp520dp);
                                page2.setVisibility(View.VISIBLE);
                                page3.setVisibility(View.INVISIBLE);
                                page5.setVisibility(View.INVISIBLE);
                            }
                            break;
                    }
                    break;
                case 4://tag
                    switch (path.getType()) {
                        case 1:
                            if (path.getAvailableTime().isEmpty()) {
                                page--;
                            } else {
                                pageNum.setText("4/5");
                                background.setBackgroundResource(R.drawable.active_map_03_320dp520dp);
                                page3.setVisibility(View.VISIBLE);
                                page2.setVisibility(View.INVISIBLE);
                                page4.setVisibility(View.INVISIBLE);
                                page5.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case 2:
                            if (path.getTitle().isEmpty() || path.getText().isEmpty()) {
                                page--;
                            } else {
                                pageNum.setText("4/5");
                                background.setBackgroundResource(R.drawable.active_map_03_320dp520dp);
                                page3.setVisibility(View.VISIBLE);
                                page2.setVisibility(View.INVISIBLE);
                                page4.setVisibility(View.INVISIBLE);
                                page5.setVisibility(View.INVISIBLE);
                            }
                            break;
                    }
                    //page0-->page1-->page5-->page2-->  page3  -->page4
                    break;
                case 5://photo
                    saveTags();
                    pageNum.setText("5/5");
                    //page0-->page1-->page5-->page2-->page3-->  page4
                    background.setBackgroundResource(R.drawable.active_map_04_320dp520dp);
                    page4.setVisibility(View.VISIBLE);
                    page3.setVisibility(View.INVISIBLE);
                    break;
                case 6:
                    if (addingNewPath) {
                        new AddPath().execute();
                    } else {
                        new EditPath().execute();
                    }
                    break;
            }
            if (path.getType() != 0) {
                img.setVisibility(View.INVISIBLE);
                back.setVisibility(View.VISIBLE);
                pageNum.setVisibility(View.VISIBLE);
            }

            if (page >= 5) {
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
                page6.setVisibility(View.INVISIBLE);
                page7.setVisibility(View.INVISIBLE);
            } else if (page < 0) {
                finish();
            }
        }
    }

    private void saveTags() {
        if (activityTags.size() > 0) //标签被选择
        {
            Set key = activityTags.keySet();
            Object[] post = key.toArray();
            postJson = new ArrayList<>();
            for (int i = 0; i < post.length; i++) {
                postJson.add(String.valueOf((int) post[i]));
            }
            CachePath.getInstance().setTags(postJson);
//            Log.d("星期",postJson.toString());
            path.setTags(postJson);
        }
    }

    public void startDatePickerDialog() {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setCallback(startDateListener);
        newFragment.setValues(calendar);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void endDatePickerDialog() {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setCallback(endDateListener);
        newFragment.setValues(calendar);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void startTimePickerDialog() {
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setCallback(startTimePicked);
        newFragment.setValues(calendar);
        newFragment.show(getFragmentManager(), getResources().getString(R.string.time_picker));
    }

    public void endTimePickerDialog() {
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setCallback(endTimePicked);
        newFragment.setValues(calendar);
        newFragment.show(getFragmentManager(), getResources().getString(R.string.time_picker));
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

    public static class DatePickerFragment extends DialogFragment {
        private DatePickerDialog.OnDateSetListener onDateSet;
        private int year;
        private int month;
        private int day;

        public void setCallback(DatePickerDialog.OnDateSetListener ods) {
            onDateSet = ods;
        }

        public void setValues(Calendar c) {
            this.year = c.get(Calendar.YEAR);
            this.month = c.get(Calendar.MONTH);
            this.day = c.get(Calendar.DAY_OF_MONTH);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new DatePickerDialog(getActivity(), onDateSet, year, month,
                    day);
        }
    }

    public static class TimePickerFragment extends DialogFragment {
        private TimePickerDialog.OnTimeSetListener onTimeSet;
        private int hour;
        private int minute;

        public void setCallback(TimePickerDialog.OnTimeSetListener ots) {
            onTimeSet = ots;
        }

        public void setValues(Calendar c) {
            this.hour = c.get(Calendar.HOUR_OF_DAY);
            this.minute = c.get(Calendar.MINUTE);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //  true states that we do want 24 hour format
            return new TimePickerDialog(getActivity(), onTimeSet, hour, minute,
                    true);
        }
    }

    private class AddPath extends AsyncTask<Void, String, Boolean> {
        private ProgressDialog pd = new ProgressDialog(PathEditActivity.this);

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
                //TODO 环信群————创建
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
                newPath = ServerHelper.getInstance().createPath(path);
                success = true;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
                publishProgress(null, "Error creating Activity");
            }
            try {
                if (imageUri != null) {
                    newPath.setImage(ServerHelper.getInstance().uploadPathImage(newPath.getId(),
                            FileUtil.getImg(imageUri.getPath())));
                }
//                Log.d(LOG_TAG, "AddPath: Uploaded image to Path: " + newPath.toString());
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
                Toast.makeText(PathEditActivity.this, values[1], Toast.LENGTH_SHORT).show();
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
        private ProgressDialog pd = new ProgressDialog(PathEditActivity.this);

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
                //TODO 环信群————创建

                if (TextUtils.isEmpty(path.getHxGroupId())) {
                    EMGroup emGroup = EMGroupManager.getInstance().
                            createPublicGroup(path.getTitle(), path.getText(), null, false);
                    if (emGroup != null) {
                        String groupId = emGroup.getGroupId();
                        path.setHxGroupId(groupId);
                    }
                } else if (oldTitle.contentEquals(path.getTitle())) {
                    //groupId 需要改变名称的群组的id
                    //changedGroupName 改变后的群组名称
                    EMGroupManager.getInstance().changeGroupName(path.getHxGroupId(), path.getTitle());//需异步处理
                }
                newPath = ServerHelper.getInstance().editPath(path.getId(), path);
                if (imageUri != null)
                    newPath.setImage(ServerHelper.getInstance().uploadPathImage(newPath.getId(),
                            FileUtil.getImg(imageUri.getPath())));
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
                publishProgress(null, "Error modifying Activity");
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null)
                pd.setMessage(values[0]);
            if (values[1] != null)
                Toast.makeText(PathEditActivity.this, values[1], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            next.setClickable(true);
            if (success) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(PathViewActivity.EDITED_PATH, newPath);
                setResult(RESULT_OK, returnIntent);
                finish();
            } else {
                page--;
            }
        }
    }
}