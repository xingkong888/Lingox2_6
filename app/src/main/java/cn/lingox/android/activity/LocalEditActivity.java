package cn.lingox.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.CachePath;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.PathTags;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.PathEditDialog;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.utils.CompressImageUtil;
import cn.lingox.android.utils.DpToPx;
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
    /**
     * 标签之间的间距 px
     */
    private static final int itemMargins = 25;
    /**
     * 标签的行间距 px
     */
    private static final int lineMargins = 25;
    private int page = 1;//当前页面
    private Boolean addingNewPath = true;//标识是否为新建true 新建；false编辑原有的
    private Boolean imageSelected = false;
    private Path path;
    private String oldTitle = "";
    private Button next;
    private boolean isSelected = false;//标识经纬度是否选择 false:未选择 true：已选择

    //第一页面-----标题、地点、时间
    private LinearLayout pageOne;//第一页
    private EditText title;//标题
    private TextView address;//地址
    private EditText time;//时间
    //第二页面
    private LinearLayout pageTwo;//第二页
    private ViewGroup tags;//标签
    private EditText describe;//活动描述
    private TextView prompt;//描述内容的提示
    //第三页面
    private LinearLayout pageThree;//第三页
    private RelativeLayout giftLayout;//礼物
    private ImageView giftIv;//选择框
    private RelativeLayout shareLayout;//分享经历
    private ImageView shareIv;//选择框
    private RelativeLayout beingLayout;//将来作为本地人
    private ImageView beingIv;//选择框
    private RelativeLayout moneyLayout;//人均花费
    private ImageView moneyIv;//选择框
    private EditText cost;//钱数
    private RelativeLayout otherLayout;//其他
    private ImageView otherIv;//选择框
    private EditText others;//自己填写

    private String costStr = "gift?:%s@@@share?:%1$s@@@being?:%2$s@@@AA?:%3$s@@@others?:%4$s";//用于标记选择了那个
    private String giftStr = "false";//礼物。false表示未选择
    private String shareStr = "false";
    private String beingStr = "false";
    private String AAStr = "false";
    private String othersStr = "false";

    //标签相关
    private ArrayList<PathTags> datas;
    private int checkedNum = 0;
    private HashMap<Integer, Integer> activityTags;
    //第四页面
    private LinearLayout pageFour;//第四页
    private ImageView photo;//图片
    private Uri imageUri = null;

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
        next = (Button) findViewById(R.id.path_edit_next);
        next.setOnClickListener(this);
        //返回
        findViewById(R.id.path_edit_back).setOnClickListener(this);
        //关闭
        findViewById(R.id.path_edit_close).setOnClickListener(this);
        /***********************第一页***************************************/
        pageOne = (LinearLayout) findViewById(R.id.local_edit_page_one);//第一页
        title = (EditText) findViewById(R.id.local_edit_title);//标题
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                path.setTitle(s.toString());
                //todo 判断当前页数据是否填写完成，完成则按钮变成绿色
                changePageOne();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        address = (TextView) findViewById(R.id.local_edit_location);//地址
        address.setOnClickListener(this);
        time = (EditText) findViewById(R.id.local_edit_time);//时间
        time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                path.setAvailableTime(s.toString());
                //todo 判断当前页数据是否填写完成，完成则按钮变成绿色
                changePageOne();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        /**********************************第二页**************************/
        pageTwo = (LinearLayout) findViewById(R.id.local_edit_page_two);//第二页
        tags = (ViewGroup) findViewById(R.id.add_tags);//标签
        datas = new ArrayList<>();
        datas.addAll(LingoXApplication.getInstance().getDatas());
        addTagView(datas, tags, this);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (datas.get(position).getType() == 0) {
//                    if (checkedNum < 3) {
//                        activityTags.put(position, 1);
//                        checkedNum++;
//                        datas.get(position).setType(1);
//                    }
//                } else {
//                    checkedNum--;
//                    activityTags.remove(position);
//                    datas.get(position).setType(0);
//                }
//                adapter.notifyDataSetChanged();
//            }
//        });

        describe = (EditText) findViewById(R.id.local_edit_describe);//活动描述
        prompt = (TextView) findViewById(R.id.prompt);//当describe获取焦点时，隐藏
        describe.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {//获取焦点
                    prompt.setVisibility(View.GONE);
                }
            }
        });

        describe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                path.setText(s.toString());
                changePageTwo();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        /***********************第三页****************************************/
        pageThree = (LinearLayout) findViewById(R.id.local_edit_page_three);//第三页
        giftLayout = (RelativeLayout) findViewById(R.id.local_edit_cost_gift_layout);//礼物
        giftLayout.setOnClickListener(this);
        giftIv = (ImageView) findViewById(R.id.local_edit_cost_gift);//选择框
        giftIv.setTag(0);//0表示未被选择 1表示被选中
        giftIv.setImageAlpha(0);

        shareLayout = (RelativeLayout) findViewById(R.id.local_edit_cost_share_layout);//分享经历
        shareLayout.setOnClickListener(this);
        shareIv = (ImageView) findViewById(R.id.local_edit_cost_share);//选择框
        shareIv.setTag(0);//0表示未被选择 1表示被选中
        shareIv.setImageAlpha(0);

        beingLayout = (RelativeLayout) findViewById(R.id.local_edit_cost_being_layout);//将来作为本地人
        beingLayout.setOnClickListener(this);
        beingIv = (ImageView) findViewById(R.id.local_edit_cost_being);//选择框
        beingIv.setTag(0);//0表示未被选择 1表示被选中
        beingIv.setImageAlpha(0);

        moneyLayout = (RelativeLayout) findViewById(R.id.local_edit_cost_money_layout);//人均花费
        moneyLayout.setOnClickListener(this);
        moneyIv = (ImageView) findViewById(R.id.local_edit_cost_money);//选择框
        moneyIv.setTag(0);//0表示未被选择 1表示被选中
        moneyIv.setImageAlpha(0);
        cost = (EditText) findViewById(R.id.cost);//钱数
        cost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    AAStr = s.toString();
                } else {
                    AAStr = "false";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        otherLayout = (RelativeLayout) findViewById(R.id.local_edit_cost_others_layout);//其他
        otherLayout.setOnClickListener(this);
        otherIv = (ImageView) findViewById(R.id.local_edit_cost_others);//选择框
        otherIv.setTag(0);//0表示未被选择 1表示被选中
        otherIv.setImageAlpha(0);
        others = (EditText) findViewById(R.id.other);//自己填写
        others.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    othersStr = s.toString();
                } else {
                    othersStr = "false";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /**********************第四页*****************************************/
        pageFour = (LinearLayout) findViewById(R.id.local_edit_page_four);//第四页
        photo = (ImageView) findViewById(R.id.local_edit_photo);//图片
        photo.setOnClickListener(this);
        photo.measure(0, 0);
        ViewGroup.LayoutParams lp = photo.getLayoutParams();
        lp.height = (int) (LingoXApplication.getInstance().getWidth() * 0.6);
        lp.width = (int) (LingoXApplication.getInstance().getWidth() * 0.6);
        photo.setLayoutParams(lp);
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
            path.setUserId(CacheHelper.getInstance().getSelfInfo().getId());

            if (CachePath.getInstance().getLocalOrTraveler() != 3) {
                path.setType(CachePath.getInstance().getLocalOrTraveler());
                if (!CachePath.getInstance().getTitle().isEmpty()) {
                    path.setTitle(CachePath.getInstance().getTitle());
                    UIHelper.getInstance().textViewSetPossiblyNullString(title, path.getTitle());
                }
                if (!CachePath.getInstance().getLocation().isEmpty()) {
                    path.setLocation(CachePath.getInstance().getLocation());
                    UIHelper.getInstance().textViewSetPossiblyNullString(address, path.getLocation());
                }
                if (!CachePath.getInstance().getDescription().isEmpty()) {
                    path.setText(CachePath.getInstance().getDescription());
                    UIHelper.getInstance().textViewSetPossiblyNullString(describe, path.getText());
                }
                if (CachePath.getInstance().getPhoto()) {
                    path.setImage(CachePath.getInstance().getImage());
                    UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, photo, path.getImage(), "circular");
                }
                if (CachePath.getInstance().getAvabilableTime().isEmpty()) {
                    path.setAvailableTime(CachePath.getInstance().getAvabilableTime());
                    UIHelper.getInstance().textViewSetPossiblyNullString(time, path.getAvailableTime());
                }
                //标签
                getTags(CachePath.getInstance().getTags());

                if (!CachePath.getInstance().getAddress().isEmpty() && path.getType() == 2) {
                    path.setDetailAddress("");
                }
            } else {
                //不保存新建活动的信息
                CachePath.getInstance().setNothing();
            }
        }
        if (!addingNewPath) {//不是新建
            getTags(path.getTags());
            //标题
            UIHelper.getInstance().textViewSetPossiblyNullString(title, path.getTitle());
            //地址
            UIHelper.getInstance().textViewSetPossiblyNullString(address, path.getLocation());
//            if ("China".equals(path.getChosenCountry())) {
//                detailAddress.setVisibility(View.VISIBLE);
//                detailAddress.setText(path.getDetailAddress());
//                isSelected = true;
//            }
            UIHelper.getInstance().textViewSetPossiblyNullString(describe, path.getText());
            //图片
            if (FileUtil.getImg(path.getImage(), this) != null) {
                photo.setImageBitmap(FileUtil.getImg(path.getImage(), this));
            } else {
                UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, photo, path.getImage(), "original");
            }
            //时间
            UIHelper.getInstance().textViewSetPossiblyNullString(time, path.getAvailableTime());
        }
    }

    /**
     * 检查第一页数据是否完成
     *
     * @return false表示当前页数据未完成
     */
    private boolean changePageOne() {
        if (path.getTitle().isEmpty() ||
                path.getChosenCountry().isEmpty() ||
                path.getAvailableTime().isEmpty()) {
            next.setBackgroundColor(Color.rgb(201, 201, 201));
            return false;
        } else {
            next.setBackgroundColor(getResources().getColor(R.color.main_color));
            return true;
        }
    }

    /**
     * 检查第二页数据是否完成
     *
     * @return false表示当前页数据未完成
     */
    private boolean changePageTwo() {
        if (!saveTags() ||
                path.getText().isEmpty() ||
                path.getText().length() < 100) {
            next.setBackgroundColor(Color.rgb(201, 201, 201));
            return false;
        } else {
            next.setBackgroundColor(getResources().getColor(R.color.main_color));
            return true;
        }
    }

    /**
     * 检查第三页数据是否完成
     *
     * @return false表示当前页数据未完成
     */
    private boolean changePageThree() {
        if ("false".equals(giftStr) &&
                "false".equals(shareStr) &&
                "false".equals(beingStr) &&
                "false".equals(AAStr) &&
                "false".equals(othersStr)) {
            next.setBackgroundColor(Color.rgb(201, 201, 201));
            return false;
        } else {
            path.setCost(String.format(costStr, giftStr, shareStr, beingStr, AAStr, othersStr));
            next.setBackgroundColor(getResources().getColor(R.color.main_color));
            return true;
        }
    }

    /**
     * 检查第四页数据是否完成
     *
     * @return false表示当前页数据未完成
     */
    private boolean changePageFour() {
        if (!imageSelected) {
            next.setBackgroundColor(Color.rgb(201, 201, 201));
            return false;
        } else {
            next.setBackgroundColor(getResources().getColor(R.color.main_color));
            return true;
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
        addTagView(datas, tags, this);
    }

    /**
     * 设置本地人和旅行者相应显示内容
     * 已经不需要了
     * private void setLocalOrTraveler() {
     * layout.setVisibility(View.VISIBLE);
     * if (path.getType() == 1) {
     * local.setBackgroundResource(R.drawable.button_border_orange);
     * traveler.setBackgroundResource(R.drawable.button_border_blue);
     * text1.setText(getString(R.string.path_edit_0_local_1));
     * text2.setText(getString(R.string.path_edit_0_local_2));
     * text3.setText(getString(R.string.path_edit_0_local_3));
     * } else {
     * background.setBackgroundResource(R.drawable.active_background_02_320dp520dp);
     * traveler.setBackgroundResource(R.drawable.button_border_orange);
     * local.setBackgroundResource(R.drawable.button_border_blue);
     * text1.setText(getString(R.string.path_edit_0_traveler_1));
     * text2.setText(getString(R.string.path_edit_0_traveler_2));
     * text3.setText(getString(R.string.path_edit_0_traveler_3));
     * }
     * }
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.path_edit_next://0 下一页 1返回
                nextClick(0);
                break;
            case R.id.path_edit_close://关闭
                if (page == 3) {
                    saveTags();
                }
                PathEditDialog.getInstance().showDialog(this, path).show();
                break;
            case R.id.path_edit_back://返回
                nextClick(1);
                break;
            case R.id.local_edit_location://选择城市；若选择为“China”，则显示地图
                Intent intent = new Intent(this, SelectCountry.class);
                intent.putExtra(SelectCountry.SELECTLOCATION, SELECTLOCATION);
                startActivityForResult(intent, SELECTLOCATION);
                break;
//            case R.id.path_detail_address://根据地图
//                Intent intent1 = new Intent(this, AMapActivity.class);
//                if (!path.getLatitude().isEmpty() && !path.getLongitude().isEmpty()) {
//                    String[] doubles = {path.getLatitude(), path.getLongitude()};
//                    intent1.putExtra("String", doubles);
//                }
//                startActivityForResult(intent1, SELECTDETIAL);
//                break;
            case R.id.local_edit_photo://选择图片
                Intent intent4 = new Intent(this, PhotoDialog.class);
                intent4.putExtra(PhotoDialog.REQUESTED_IMAGE, PhotoDialog.REQUEST_CARD_IMAGE);
                startActivityForResult(intent4, PhotoDialog.REQUEST_CARD_IMAGE);
                break;
            case R.id.local_edit_cost_gift_layout://一个礼物
                if ((int) giftIv.getTag() == 0) {
                    //未被选择
                    giftIv.setImageAlpha(255);
                    giftIv.setTag(1);
//                    path.setCost("gift");
                    giftStr = "true";
                } else {
                    giftIv.setImageAlpha(0);
                    giftIv.setTag(0);
//                    path.setCost("");
                    giftStr = "false";
                }
                changePageThree();
                break;
            case R.id.local_edit_cost_money_layout://人均花费
                //todo 在输入人均花费的地方存入path。要考虑选择和取消不同状态
                //如果是选择状态，则编辑框可用
                if ((int) moneyIv.getTag() == 0) {
                    //未被选择
                    moneyIv.setImageAlpha(255);
                    moneyIv.setTag(1);
                    cost.setEnabled(true);
                } else {
                    cost.setEnabled(false);
                    moneyIv.setImageAlpha(0);
                    moneyIv.setTag(0);
//                    path.setCost("");
                    AAStr = "false";
                }
                changePageThree();
                break;
            case R.id.local_edit_cost_being_layout://将来做为本地人
                if ((int) beingIv.getTag() == 0) {
                    //未被选择
                    beingIv.setImageAlpha(255);
                    beingIv.setTag(1);
//                    path.setCost("being");
                    beingStr = "true";
                } else {
                    beingIv.setImageAlpha(0);
                    beingIv.setTag(0);
//                    path.setCost("");
                    beingStr = "false";
                }
                changePageThree();
                break;
            case R.id.local_edit_cost_share_layout://分享旅行经历
                if ((int) shareIv.getTag() == 0) {
                    //未被选择
                    shareIv.setImageAlpha(255);
                    shareIv.setTag(1);
//                    path.setCost("share");
                    shareStr = "true";
                } else {
                    shareIv.setImageAlpha(0);
                    shareIv.setTag(0);
//                    path.setCost("");
                    shareStr = "false";
                }
                changePageThree();
                break;
            case R.id.local_edit_cost_others_layout://自己填写
                //todo 在输入的地方存入path。要考虑选择和取消不同状态
                //如果是选择状态，则编辑框可用
                if ((int) otherIv.getTag() == 0) {
                    //从未被选择到被选择
                    otherIv.setImageAlpha(255);
                    otherIv.setTag(1);
                    others.setEnabled(true);
                } else {
                    others.setEnabled(false);
                    otherIv.setImageAlpha(0);
                    otherIv.setTag(0);
//                    path.setCost("");
                    othersStr = "false";
                }
                changePageThree();
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
                    address.setText(str);
                    //todo 判断当前页数据是否填写完成，完成则按钮变成绿色
                    changePageOne();
                    if ("China".equals(path.getChosenCountry())) {
                        //如果选择的是中国，跳转到地图
                        Intent intent1 = new Intent(this, AMapActivity.class);
                        if (!path.getLatitude().isEmpty() && !path.getLongitude().isEmpty()) {
                            String[] doubles = {path.getLatitude(), path.getLongitude()};
                            intent1.putExtra("String", doubles);
                        }
                        startActivityForResult(intent1, SELECTDETIAL);
                    } else {
                        path.setDetailAddress("");
                    }
                }
                break;
            case SELECTDETIAL://获取经纬度-----如果选择的是中国
                double[] doubles = data.getDoubleArrayExtra(SELECTDETIALLAT);
                String add = data.getStringExtra(SELECTDETIALADD);
                if (!add.isEmpty()) {
                    path.setDetailAddress(add);
                    address.setText(address.getText() + ", " + add);
                } else {
                    path.setLocation("");
                    path.setDetailAddress("");
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
                        photo.setImageBitmap(FileUtil.getImg(imageUri.getPath(), this));
                        path.setImage("");
                        imageSelected = true;
                    } else if (data.hasExtra(PathCardImgDialog.PRESET_URI)) {
                        imageUri = null;
                        String presetImageUrl = data.getStringExtra(PathCardImgDialog.PRESET_URI);
                        Picasso.with(this).load(presetImageUrl).into(photo);
                        path.setImage(presetImageUrl);
                        imageSelected = true;
                    }
                    changePageFour();
                }
                //将新建的活动缓存
                CachePath.getInstance().setPhoto(true);
                break;
        }
    }

    /**
     * 0 on behalf of the next
     * 1 on behalf of the back
     *
     * @param nextOrBack on behalf of the page number
     */
    private void nextClick(int nextOrBack) {
        if (nextOrBack == 0) {
            page++;
        } else if (nextOrBack == 1) {
            page--;
        }
        if (page <= 0) {
            finish();
            return;
        }
        switch (page) {
            case 1://第一页
                pageOne.setVisibility(View.VISIBLE);
                pageTwo.setVisibility(View.GONE);
                pageThree.setVisibility(View.GONE);
                pageFour.setVisibility(View.GONE);
                changePageOne();
                break;
            case 2://第二页
                //判断标题、地点、时间是否填写完整
                if (!changePageOne()) {
                    showToast("请将信息填写完整----2");
                    page--;
                } else {
                    changePageTwo();
                    pageTwo.setVisibility(View.VISIBLE);
                    pageOne.setVisibility(View.GONE);
                    pageThree.setVisibility(View.GONE);
                    pageFour.setVisibility(View.GONE);
                }
                break;
            case 3://第三页
                //判断标签是否选择、介绍是否填写完整
                if (!changePageTwo()) {
                    showToast("请将信息填写完整------3");
                    page--;
                } else {
                    //判断选择的标签是否含有“home meal”
                    for (int i = 0, j = path.getTags().size(); i < j; i++) {
                        if ("Home Meal".equals(datas.get(Integer.valueOf(path.getTags().get(i))).getTag())) {
                            moneyLayout.setVisibility(View.VISIBLE);
                            break;
                        } else {
                            moneyLayout.setVisibility(View.GONE);
                        }
                    }
                    changePageThree();
                    pageThree.setVisibility(View.VISIBLE);
                    pageTwo.setVisibility(View.GONE);
                    pageOne.setVisibility(View.GONE);
                    pageFour.setVisibility(View.GONE);
                }
                break;
            case 4://第四页
                //判断花费是否选择
                if (!changePageThree()) {
                    showToast("请将信息填写完整------4");
                    page--;
                } else {
                    changePageFour();
                    pageFour.setVisibility(View.VISIBLE);
                    pageTwo.setVisibility(View.GONE);
                    pageOne.setVisibility(View.GONE);
                    pageThree.setVisibility(View.GONE);
                }

                break;
            case 5://提交数据
                //判断图片是否选择
                if (!changePageFour()) {
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
        if (page >= 4) {
            next.setText("POST");
        } else {
            next.setText("NEXT");
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

    /**
     * 添加标签
     *
     * @param tags     标签集合
     * @param tagsView 显示控件
     * @param context  上下文
     */
    public void addTagView(ArrayList<PathTags> tags,
                           ViewGroup tagsView, Activity context) {
        int width = LingoXApplication.getInstance().getWidth();
        tagsView.removeAllViews();
        LayoutInflater inflater = context.getLayoutInflater();
        /** 用来测量字符的宽度 */
        Paint paint = new Paint();
        TextView textView = (TextView) inflater.inflate(R.layout.row_tag_include, null);
        int itemPadding = textView.getCompoundPaddingLeft() + textView.getCompoundPaddingRight();
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(0, 0, itemMargins, 0);
        paint.setTextSize(textView.getTextSize());
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        tagsView.addView(layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, lineMargins, DpToPx.dip2px(context, 20), 0);

        /** 一行剩下的空间 **/
        int remainWidth = width;
        // 表示数组长度
        int length = tags.size();
        PathTags pathTags;
        float itemWidth;
        for (int i = 0; i < length; ++i) {
            pathTags = tags.get(i);

            itemWidth = paint.measureText(pathTags.getTag()) + itemPadding;
            if (remainWidth - itemWidth > 25) {
                addItemView(inflater, layout, tvParams, pathTags, i);
            } else {
                resetTextViewMarginsRight(layout);
                layout = new LinearLayout(context);
                layout.setLayoutParams(params);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                /** 将前面那一个textview加入新的一行 */
                addItemView(inflater, layout, tvParams, pathTags, i);
                tagsView.addView(layout);
                remainWidth = width;
            }
            remainWidth = (int) (remainWidth - itemWidth + 0.5f) - itemMargins;
        }
        if (length > 0) {
            resetTextViewMarginsRight(layout);
        }
    }

    /*****************
     * 将每行最后一个textview的MarginsRight去掉
     *********************************/
    private void resetTextViewMarginsRight(ViewGroup viewGroup) {
        final TextView tempTextView = (TextView) viewGroup.getChildAt(viewGroup.getChildCount() - 1);
        tempTextView.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * 添加view到父容器控件中
     *
     * @param inflater  加载器
     * @param viewGroup 父容器
     * @param params    布局参数
     * @param pathTags  内容
     */
    private void addItemView(LayoutInflater inflater,
                             ViewGroup viewGroup, ViewGroup.LayoutParams params,
                             final PathTags pathTags, final int position) {
        final TextView tvItem = (TextView) inflater.inflate(R.layout.row_tag_include, null);
        tvItem.setText(pathTags.getTag());
        if (pathTags.getType() == 0) {
            //未选中
            tvItem.setBackgroundDrawable(getResources().getDrawable(R.drawable.tag_cyc));
            tvItem.setTextColor(getResources().getColor(R.color.main_color));
        } else {
            tvItem.setTextColor(Color.WHITE);
            tvItem.setBackgroundDrawable(getResources().getDrawable(R.drawable.tag_cyc_selected));
        }
        tvItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (datas.get(position).getType() == 1) {
                    checkedNum--;
                    activityTags.remove(position);
                    datas.get(position).setType(0);
                    tvItem.setBackgroundDrawable(getResources().getDrawable(R.drawable.tag_cyc));
                    tvItem.setTextColor(getResources().getColor(R.color.main_color));
                } else if (checkedNum == 3 && datas.get(position).getType() == 0) {
                    Toast.makeText(LocalEditActivity.this, "Most alternative three.", Toast.LENGTH_SHORT).show();
                } else if (datas.get(position).getType() == 0) {
                    if (checkedNum < 3) {
                        activityTags.put(position, 1);
                        checkedNum++;
                        datas.get(position).setType(1);
                        tvItem.setTextColor(Color.WHITE);
                        tvItem.setBackgroundDrawable(getResources().getDrawable(R.drawable.tag_cyc_selected));
                    }
                }
                changePageTwo();
            }
        });
        viewGroup.addView(tvItem, params);
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
//                环信群————创建
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
            if (values[0] != null) {
                pd.setMessage(values[0]);
            }
            if (values[1] != null) {
                Toast.makeText(LocalEditActivity.this, values[1], Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            next.setClickable(true);
            if (success) {
//                Intent returnIntent = new Intent();
//                returnIntent.putExtra(ADDED_PATH, newPath);
//                setResult(RESULT_OK, returnIntent);
//                finish();
                //TODO 后期修改提示语
                showToast("创建成功，正在审核中…");
            } else {
                page--;
                //TODO 后期修改提示语
                showToast("创建失败");
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
            if (values[0] != null) {
                pd.setMessage(values[0]);
            }
            if (values[1] != null) {
                Toast.makeText(LocalEditActivity.this, values[1], Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            next.setClickable(true);
            if (success) {
//                Intent returnIntent = new Intent();
//                returnIntent.putExtra(LocalViewActivity.EDITED_PATH, newPath);
//                setResult(RESULT_OK, returnIntent);
//                finish();
                //TODO 后期修改提示语
                showToast("修改成功，正在审核中…");
            } else {
                page--;
                //TODO 后期修改提示语
                showToast("修改失败");
            }
        }
    }
}