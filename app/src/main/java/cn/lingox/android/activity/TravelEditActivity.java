package cn.lingox.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import cn.lingox.android.R;
import cn.lingox.android.activity.select_area.SelectCountry;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.PathTags;
import cn.lingox.android.entity.Travel;
import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.DatePickerFragment;
import cn.lingox.android.helper.TimeHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.CreateTravelEntity;
import cn.lingox.android.task.EditTravelEntity;
import cn.lingox.android.task.TravelPlanAsynTask;
import cn.lingox.android.utils.DpToPx;

/**
 * 创建travel数据
 */
public class TravelEditActivity extends FragmentActivity implements OnClickListener {
    public static final String TRAVEL_EDIT = "TravelEdit";
    public static final String TRAVEL_CREATE = "TravelCreate";
    private static final int SELECT_LOCATION = 2013;
    /**
     * 标签之间的间距 px
     */
    private static final int itemMargins = 25;
    /**
     * 标签的行间距 px
     */
    private static final int lineMargins = 25;

    private LinearLayout pageOne;//第一页
    private TextView travelLocation;//旅行地址
    private TextView showArriveTime;//到达时间
    private TextView showLeaveTime;//离开时间
    private EditText expect;//期待
    private TextView prompt;
    private LinearLayout pageTwo;//第二页
    private EditText offer;//提供
    private Button post;//下一页及提交数据
    private ArrayList<PathTags> datas;
    //标签
    private ViewGroup addTags;
    private int checkedNum = 0;
    private HashMap<Integer, Integer> activityTags = new HashMap<>();

    private TravelEntity travelEntity;

    private boolean isEdit = false;//表示是否为修改 false创建、true修改

    private long start = 0, end = 0, now = System.currentTimeMillis() / 1000L;
    private Calendar calendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day, 0, 0, 0);
            start = calendar.getTimeInMillis() / 1000L;
            if (end == 0 || end >= start) {
                UIHelper.getInstance().textViewSetPossiblyNullString(showArriveTime, TimeHelper.getInstance().parseTimestampToDate(start));
                travelEntity.setStartTime(start);
                checkPageOne();
            } else {
                Toast.makeText(TravelEditActivity.this, getString(R.string.start_end), Toast.LENGTH_SHORT).show();
                startDatePickerDialog();
            }
        }
    };
    private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day, 23, 59, 59);
            end = calendar.getTimeInMillis() / 1000L;
            if (end - now >= 0 && (start == 0 || end >= start)) {
                UIHelper.getInstance().textViewSetPossiblyNullString(showLeaveTime, TimeHelper.getInstance().parseTimestampToDate(end));
                travelEntity.setEndTime(end);
                checkPageOne();
            } else {
                Toast.makeText(TravelEditActivity.this, getString(R.string.end_start), Toast.LENGTH_SHORT).show();
                endDatePickerDialog();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_travel_edit);
        initView();
        //判断是否为修改
        if (getIntent().hasExtra(TRAVEL_EDIT)) {
            travelEntity = getIntent().getParcelableExtra(TRAVEL_EDIT);
            isEdit = true;
            setData();
        } else {
            travelEntity = new TravelEntity();
            isEdit = false;
        }
    }

    /**
     * 实例化控件
     */
    private void initView() {
        /******************************菜单栏********************/
        ImageView back = (ImageView) findViewById(R.id.travel_edit_back);
        back.setOnClickListener(this);
        ImageView close = (ImageView) findViewById(R.id.travel_edit_close);
        close.setOnClickListener(this);
        /******************************第一页*********************/
        pageOne = (LinearLayout) findViewById(R.id.travel_edit_page_one);
        //选择旅行地点
        travelLocation = (TextView) findViewById(R.id.travel_edit_location);
        travelLocation.setOnClickListener(this);
        //到达时间
        LinearLayout arriveTime = (LinearLayout) findViewById(R.id.travel_edit_arrive_time);
        arriveTime.setOnClickListener(this);
        showArriveTime = (TextView) findViewById(R.id.travel_arrive_time);
        //离开时间
        LinearLayout leaveTime = (LinearLayout) findViewById(R.id.travel_edit_leave_time);
        leaveTime.setOnClickListener(this);
        showLeaveTime = (TextView) findViewById(R.id.travel_leave_time);
        //到当地期待或希望的活动
        expect = (EditText) findViewById(R.id.travel_edit_expect);
        prompt = (TextView) findViewById(R.id.prompt);
        expect.setOnClickListener(this);
        expect.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    travelEntity.setText(s.toString());
                } else {
                    Toast.makeText(TravelEditActivity.this, "Please fill out the content.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        /**********************************************第二页*************************/
        pageTwo = (LinearLayout) findViewById(R.id.travel_edit_page_two);
        //选择标签
        addTags = (ViewGroup) findViewById(R.id.add_tags);
        datas = new ArrayList<>();
        datas.addAll(LingoXApplication.getInstance().getDatas());
        //添加标签
        addTagView(datas, addTags, this);
        //将来作为本地人能提供的
        offer = (EditText) findViewById(R.id.travel_edit_offer);
        offer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                travelEntity.setProvide(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //下一页及最后的提交
        post = (Button) findViewById(R.id.travel_edit_next);
        post.setOnClickListener(this);
    }

    /**
     * 设置数据
     * 若是修改，则将原数据设置到控件上
     */
    private void setData() {
        travelLocation.setText(travelEntity.getLocation());
        expect.setText(travelEntity.getText());
        //设置tag
        if (travelEntity.getTags().size() > 0) {
            for (int i = 0, j = travelEntity.getTags().size(); i < j; i++) {
                activityTags.put(Integer.valueOf(travelEntity.getTags().get(i)), 1);
                if (checkedNum < 3) {
                    checkedNum++;
                    datas.get(Integer.valueOf(travelEntity.getTags().get(i))).setType(1);
                }
            }
        }
        addTagView(datas, addTags, this);
        /***************************************************************************/
        showArriveTime.setText(TimeHelper.getInstance().parseTimestampToDate(travelEntity.getStartTime()));
        showLeaveTime.setText(TimeHelper.getInstance().parseTimestampToDate(travelEntity.getEndTime()));

        offer.setText(travelEntity.getProvide());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.travel_edit_expect://期待
                prompt.setVisibility(View.GONE);
                break;
            case R.id.travel_edit_next://下一页及最后的提交
                nextClick(0);
                break;
            case R.id.travel_edit_arrive_time: //选择到达时间
                startDatePickerDialog();
                break;
            case R.id.travel_edit_leave_time://选择离开时间
                endDatePickerDialog();
                break;
            case R.id.travel_edit_location://选择旅行地点
                Intent intent = new Intent(this, SelectCountry.class);
                intent.putExtra("TravelEdit", "");
                startActivityForResult(intent, SELECT_LOCATION);
                break;
            case R.id.travel_edit_back://返回
                nextClick(1);
                break;
            case R.id.travel_edit_close://关闭
                new AlertDialog.Builder(this)
                        .setMessage("Whether to give up the content you are editing?")
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                back();
                            }
                        }).create().show();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        nextClick(1);
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
            travelEntity.setTags(postJson);
        }
        return travelEntity.getTags().size() > 0;
    }

    /**
     * 检查第一页是否填写完成
     *
     * @return
     */
    private boolean checkPageOne() {
        if (travelEntity.getCountry().isEmpty() ||
                travelEntity.getStartTime() == -1 ||
                travelEntity.getEndTime() == -1) {
            post.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_next));
            return false;
        } else {
            post.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_next_main_color));
            return true;
        }
    }

    /**
     * 检查第一页是否填写完成
     *
     * @return
     */
    private boolean checkPageTwo() {
        if (travelEntity.getTags().size() < 0 ||
                travelEntity.getText().isEmpty() ||
                !saveTags()) {
            post.setBackgroundColor(Color.rgb(201, 201, 201));
            return false;
        } else {
            post.setBackgroundColor(getResources().getColor(R.color.main_color));
            return true;
        }
    }

    /**
     * @param what 表示发出事件的控件
     */
    private void nextClick(int what) {
        switch (what) {
            case 0://“post”的事件
                //首先判断当前显示的页面
                if (pageOne.getVisibility() == View.VISIBLE) {
                    //第一页显示，判断第一页内容是否填写完全
                    if (!checkPageOne()) {
                        Toast.makeText(this, "Please fill out the complete.", Toast.LENGTH_SHORT).show();
                    } else {
                        //填写完成，显示第二页
                        pageOne.setVisibility(View.GONE);
                        pageTwo.setVisibility(View.VISIBLE);
                        post.setText("POST");
                    }
                } else if (pageTwo.getVisibility() == View.VISIBLE) {
                    //第二页显示，判断第一页内容是否填写完全
                    if (!checkPageTwo()) {
                        Toast.makeText(this, "Please fill out the complete.", Toast.LENGTH_SHORT).show();
                    } else {
                        //填写完成，显示第二页
                        if (!isEdit) {
                            //创建
                            create();
                        } else {
                            //修改
                            edit();
                        }
                    }
                }
                break;
            case 1://“back”和系统返回键的事件
                if (pageOne.getVisibility() == View.GONE) {
                    pageOne.setVisibility(View.VISIBLE);
                    pageTwo.setVisibility(View.GONE);
                    post.setText("NEXT");
                } else {
                    back();
                }
                break;
        }
    }

    //返回
    private void back() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_LOCATION://选择地点
                String str = data.getStringExtra(SelectCountry.SELECTED);
                if (!str.isEmpty()) {
                    travelLocation.setText(str);
                    travelEntity.setLocation(str);
                }
                break;
        }
    }

    /**
     * 创建travel
     */
    private void create() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Creating Activity...");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        post.setClickable(false);

        HashMap<String, String> createParams = new HashMap<>();
        setParams(createParams, "create");
        new CreateTravelEntity(createParams, new CreateTravelEntity.Callback() {
            @Override
            public void onSuccess(TravelEntity entity) {
                Intent intent = new Intent();
                intent.putExtra(TRAVEL_CREATE, entity);
                setResult(RESULT_OK, intent);
                finish();
                pd.dismiss();
                Travel travel = new Travel();
                travel.setStartTime(entity.getStartTime());
                travel.setEndTime(entity.getEndTime());
                travel.setLocation(entity.getLocation());
                //在个人信息的旅行计划中添加
                new TravelPlanAsynTask(TravelEditActivity.this, travel, "create").execute();
                //TODO 后期修改提示语
                showToast("创建成功，正在审核中…");
            }

            @Override
            public void onFail() {
                //TODO 后期修改提示语
                showToast("创建失败");
//                Toast.makeText(TravelEditActivity.this, "Create a failure", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        }).execute();
    }

    //提示用户
    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /**
     * 编辑
     */
    private void edit() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Upload...");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        post.setClickable(false);
        HashMap<String, String> editParams = new HashMap<>();
        setParams(editParams, "edit");
        new EditTravelEntity(editParams, new EditTravelEntity.Callback() {
            @Override
            public void onSuccess(TravelEntity entity) {
                Intent intent = new Intent();
                intent.putExtra(TRAVEL_EDIT, entity);
                setResult(RESULT_OK, intent);
                finish();
                pd.dismiss();
                //TODO 后期修改提示语
                showToast("修改成功，正在审核中…");
            }

            @Override
            public void onFail() {
                Toast.makeText(TravelEditActivity.this, "Modify the failure", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                //TODO 后期修改提示语
                showToast("修改失败");
            }
        }).execute();
    }

    /**
     * 设置上传数据
     */
    private void setParams(HashMap<String, String> params, String flg) {
        switch (flg) {
            case "create":
                params.put("userId", CacheHelper.getInstance().getSelfInfo().getId());
                break;
            case "edit":
                params.put("demandId", travelEntity.getId());
                break;
        }
        params.put("text", travelEntity.getText());
        params.put("provide", travelEntity.getProvide());
        params.put("startTime", String.valueOf(travelEntity.getStartTime()));
        params.put("endTime", String.valueOf(travelEntity.getEndTime()));
        params.put("country", travelEntity.getCountry());
        params.put("province", travelEntity.getProvince());
        params.put("city", travelEntity.getCity());
        params.put("tags", String.valueOf(travelEntity.getTags()));
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
                    Toast.makeText(TravelEditActivity.this, "Most alternative three.", Toast.LENGTH_SHORT).show();
                } else if (datas.get(position).getType() == 0) {
                    if (checkedNum < 3) {
                        activityTags.put(position, 1);
                        checkedNum++;
                        datas.get(position).setType(1);
                        tvItem.setTextColor(Color.WHITE);
                        tvItem.setBackgroundDrawable(getResources().getDrawable(R.drawable.tag_cyc_selected));
                    }
                }
            }
        });
        viewGroup.addView(tvItem, params);
    }
}