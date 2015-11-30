package cn.lingox.android.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import cn.lingox.android.R;
import cn.lingox.android.activity.select_area.SelectCountry;
import cn.lingox.android.adapter.MyAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.PathTags;
import cn.lingox.android.entity.Travel;
import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.TimeHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.CreateTravelEntity;
import cn.lingox.android.task.EditTravelEntity;
import cn.lingox.android.task.TravelPlanAsynTask;

/**
 * 创建travel数据
 */
public class TravelEditActivity extends FragmentActivity implements OnClickListener {
    public static final String TRAVEL_EDIT = "TravelEdit";
    public static final String TRAVEL_CREATE = "TravelCreate";
    private static final int SELECT_LOCATION = 2013;


    private ImageView bg, close, back;
    private LinearLayout page1, page2, page3, page4;
    private EditText describe;//traveling
    private MyAdapter adapter;
    private ArrayList<PathTags> datas;
    private int checkedNum = 0;
    private HashMap<Integer, Integer> activityTags = new HashMap<>();

    private TextView from, to;
    private EditText provide;
    private Button next;
    private TextView pageNum;
    private Button selectLocation;

    private TravelEntity travelEntity;

    private boolean isEdit = false;//表示是否为修改 false创建、true修改

    private int page = 1;
    private long start = 0, end = 0, now = System.currentTimeMillis() / 1000L;
    private Calendar calendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day, 0, 0, 0);
            start = calendar.getTimeInMillis() / 1000L;
            if (end == 0 || end >= start) {
                UIHelper.getInstance().textViewSetPossiblyNullString(from, TimeHelper.getInstance().parseTimestampToDate(start));
                travelEntity.setStartTime(start);
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
                UIHelper.getInstance().textViewSetPossiblyNullString(to, TimeHelper.getInstance().parseTimestampToDate(end));
                travelEntity.setEndTime(end);
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
     * 设置数据
     * 若是修改，则将原数据设置到控件上
     */
    private void setData() {
        selectLocation.setText(travelEntity.getLocation());
        describe.setText(travelEntity.getText());
        //设置tag
        if (travelEntity.getTags().size() > 0) {
            for (int i = 0, j = travelEntity.getTags().size(); i < j; i++) {
                activityTags.put(Integer.valueOf(travelEntity.getTags().get(i)), 1);
                if (checkedNum < 3) {
                    checkedNum++;
                    datas.get(Integer.valueOf(travelEntity.getTags().get(i))).setType(1);
                }
            }
            adapter.notifyDataSetChanged();
        }
        /************************/
        from.setText(TimeHelper.getInstance().parseTimestampToDate(travelEntity.getStartTime()));
        to.setText(TimeHelper.getInstance().parseTimestampToDate(travelEntity.getEndTime()));

        provide.setText(travelEntity.getProvide());
    }

    /**
     * 实例化控件
     */
    private void initView() {
        bg = (ImageView) findViewById(R.id.travel_bg);
        next = (Button) findViewById(R.id.travel_edit_next);
        next.setOnClickListener(this);
        back = (ImageView) findViewById(R.id.travel_edit_back);
        back.setOnClickListener(this);
        close = (ImageView) findViewById(R.id.travel_edit_close);
        close.setOnClickListener(this);
        pageNum = (TextView) findViewById(R.id.travel_edit_num);
//        第一页
        page1 = (LinearLayout) findViewById(R.id.travel_edit_page_1);
        selectLocation = (Button) findViewById(R.id.travel_page_1_country);
        selectLocation.setOnClickListener(this);
//        第二页
        page2 = (LinearLayout) findViewById(R.id.travel_edit_page_2);
        describe = (EditText) findViewById(R.id.travel_page_2_describe);
        ListView listView = (ListView) findViewById(R.id.travel_page_2_tags);
        datas = new ArrayList<>();
        datas.addAll(LingoXApplication.getInstance().getDatas());
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
                    }
                } else {
                    checkedNum--;
                    activityTags.remove(position);
                    datas.get(position).setType(0);
                }
                adapter.notifyDataSetChanged();
            }
        });
//        第三页
        page3 = (LinearLayout) findViewById(R.id.travel_edit_page_3);
        from = (TextView) findViewById(R.id.travel_page_3_from);
        from.setOnClickListener(this);
        to = (TextView) findViewById(R.id.travel_page_3_to);
        to.setOnClickListener(this);
//        第四页
        page4 = (LinearLayout) findViewById(R.id.travel_edit_page_4);
        provide = (EditText) findViewById(R.id.travel_page_4_edit);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.travel_edit_next:
                nextClick(0);
                break;
            case R.id.travel_page_3_from:
                //选择时间
                startDatePickerDialog();
                break;
            case R.id.travel_page_3_to:
                //选择时间
                endDatePickerDialog();
                break;
            //选择地点
            case R.id.travel_page_1_country:
                Intent intent = new Intent(this, SelectCountry.class);
                startActivityForResult(intent, SELECT_LOCATION);
                break;
            case R.id.travel_edit_back:
                nextClick(1);
                break;
            case R.id.travel_edit_close:
                new AlertDialog.Builder(this)
                        .setMessage("Whether to give up the content you are editing?")
                        .setNegativeButton("no", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create().show();
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
     * 0 on behalf of the next
     * 1 on behalf of the back
     * page1-->填写要去的地方
     * page2-->填写详细描述及选择类型
     * page3-->选择时间
     * page4-->填写当你作为本地人的时候，你能提供什么
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
                case 1://填写要去的地方、选择时间
                    page1.setVisibility(View.VISIBLE);
                    page2.setVisibility(View.GONE);
                    page3.setVisibility(View.GONE);
                    page4.setVisibility(View.GONE);
                    pageNum.setText("1/4");
                    bg.setImageResource(R.drawable.active_map_01_320dp520dp);
                    break;
                case 2://填写详细描述
                    if (selectLocation.getText().toString().trim().isEmpty()
                            || from.getText().toString().isEmpty() || to.getText().toString().isEmpty()) {
                        page--;
                        break;
                    }
                    page2.setVisibility(View.VISIBLE);
                    page1.setVisibility(View.GONE);
                    page3.setVisibility(View.GONE);
                    page4.setVisibility(View.GONE);
                    pageNum.setText("2/4");
                    bg.setImageResource(R.drawable.active_map_02_320dp520dp);
                    break;
                case 3://选择类型
                    if (describe.getText().toString().isEmpty()) {
                        page--;
                        break;
                    }
                    travelEntity.setText(describe.getText().toString().trim());//设置数据

                    page3.setVisibility(View.VISIBLE);
                    page2.setVisibility(View.GONE);
                    page1.setVisibility(View.GONE);
                    page4.setVisibility(View.GONE);
                    pageNum.setText("3/4");
                    bg.setImageResource(R.drawable.active_map_03_320dp520dp);
                    break;
                case 4://填写当你作为本地人的时候，你能提供什么
                    if (!saveTags()) {
                        page--;
                        break;
                    }
                    page4.setVisibility(View.VISIBLE);
                    page2.setVisibility(View.GONE);
                    page3.setVisibility(View.GONE);
                    page1.setVisibility(View.GONE);
                    pageNum.setText("4/4");
                    bg.setImageResource(R.drawable.active_map_04_320dp520dp);
                    break;
                case 5://提交数据
                    if (provide.getText().toString().isEmpty()) {
                        page--;
                        break;
                    }
                    travelEntity.setProvide(provide.getText().toString().trim());
                    if (!isEdit) {
                        //创建
                        create();
                    } else {
                        //修改
                        edit();
                    }
                    break;
            }
            if (page >= 4) {
                next.setText("COMPLETE");
            } else {
                next.setText(getString(R.string.path_edit_next));
            }
        } else {
            if (page <= 0) {
                finish();
            } else {
                back.setVisibility(View.VISIBLE);
                pageNum.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_LOCATION://选择地点
                String str = data.getStringExtra(SelectCountry.SELECTED);
                if (!str.isEmpty()) {
                    selectLocation.setText(str);
                    travelEntity.setLocation(str);
                }
                break;
        }
    }
    //todo 添加和修改旅行计划
//        new TravelPlanAsynTask(this, travel, "edit").execute();
//        new TravelPlanAsynTask(this, travel, "create").execute();

    /**
     * 创建
     */
    private void create() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Creating Activity...");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        next.setClickable(false);

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
                new TravelPlanAsynTask(TravelEditActivity.this, travel, "create").execute();
            }

            @Override
            public void onFail() {
                Toast.makeText(TravelEditActivity.this, "Create a failure", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        }).execute();
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
        next.setClickable(false);
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
            }

            @Override
            public void onFail() {
                Toast.makeText(TravelEditActivity.this, "Modify the failure", Toast.LENGTH_SHORT).show();
                pd.dismiss();
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
}