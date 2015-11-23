package cn.lingox.android.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.CreateTravelEntity;
import cn.lingox.android.task.EditTravelEntity;

/**
 * 创建travel数据
 */
public class TravelEditActivity extends FragmentActivity implements OnClickListener {

    private ImageView bg, colse, back;
    private LinearLayout page1, page2, page3, page4;
    private EditText traveling, describe;
    private ListView listView;
    private Button from, to;
    private EditText provide;
    private Button next;
    private TextView pageNum;

    private TravelEntity travelEntity = new TravelEntity();

    private int page = 0;
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
            if (end - now >= 0 && (start == 0 || end >= start)) {
                UIHelper.getInstance().textViewSetPossiblyNullString(endTime, JsonHelper.getInstance().parseTimestamp((int) end, 1));
                travelEntity.setEndTime(end);
            } else {
                Toast.makeText(TravelEditActivity.this, getString(R.string.end_start), Toast.LENGTH_SHORT).show();
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
            if (end == 0 || end >= start) {
                UIHelper.getInstance().textViewSetPossiblyNullString(startTime, JsonHelper.getInstance().parseTimestamp((int) start, 1));
                travelEntity.setStartTime(start);
            } else {
                Toast.makeText(TravelEditActivity.this, getString(R.string.start_end), Toast.LENGTH_SHORT).show();
                startDatePickerDialog();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_travel_edit);

        initView();
    }

    private void initView() {
        bg = (ImageView) findViewById(R.id.travel_bg);
        next = (Button) findViewById(R.id.travel_edit_next);
        next.setOnClickListener(this);
//        colse=(ImageView)findViewById(R.id.travel_edit_close);
//        colse.setOnClickListener(this);
        back = (ImageView) findViewById(R.id.travel_edit_back);
        back.setOnClickListener(this);
        pageNum = (TextView) findViewById(R.id.travel_edit_num);
//        第一页
        page1 = (LinearLayout) findViewById(R.id.travel_edit_page_1);
        traveling = (EditText) findViewById(R.id.travel_page_1_edit);
//        第二页
        page2 = (LinearLayout) findViewById(R.id.travel_edit_page_2);
        describe = (EditText) findViewById(R.id.travel_page_2_describe);
        listView = (ListView) findViewById(R.id.travel_page_2_tags);
//        第三页
        page3 = (LinearLayout) findViewById(R.id.travel_edit_page_3);
        from = (Button) findViewById(R.id.travel_page_3_from);
        to = (Button) findViewById(R.id.travel_page_3_to);
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
//            case R.id.travel_edit_close:
////                nextClick(0);
//                break;
            case R.id.travel_edit_back:
                nextClick(1);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        nextClick(1);
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
                case 1://填写要去的地方
                    page1.setVisibility(View.VISIBLE);
                    page2.setVisibility(View.GONE);
                    page3.setVisibility(View.GONE);
                    page4.setVisibility(View.GONE);
                    pageNum.setText("1/4");
                    bg.setImageResource(R.drawable.active_map_01_320dp520dp);
                    break;
                case 2://填写详细描述及选择类型
                    if (traveling.getText().toString().isEmpty()) {
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
                case 3://选择时间
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
//                    if (from.getText().toString().isEmpty() || to.getText().toString().isEmpty()) {
//                        page--;
//                        break;
//                    }
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
                    if (" ".equals(" ")) {
                        //创建
                        create();
                    } else {
                        //修改
                        edit();
                    }
                    break;
            }
            if (page >= 4) {
                next.setText(getString(R.string.create));
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

    /**
     * 创建
     */
    private void create() {
        HashMap<String, String> params = new HashMap<>();

        params.put("userId", CacheHelper.getInstance().getSelfInfo().getId());
        params.put("text", travelEntity.getText());
        params.put("provide", travelEntity.getProvide());
        params.put("startTime", String.valueOf(travelEntity.getStartTime()));
        params.put("endTime", String.valueOf(travelEntity.getEndTime()));
        params.put("country", travelEntity.getCountry());
        params.put("province", travelEntity.getProvince());
        params.put("city", travelEntity.getCity());
        params.put("tags", travelEntity.getTags().toString().replace("[", "").replace("]", ""));
        new CreateTravelEntity(params, new CreateTravelEntity.Callback() {
            @Override
            public void onSuccess(TravelEntity entity) {
                Toast.makeText(TravelEditActivity.this, "成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail() {

            }
        }).execute();
    }

    /**
     * 编辑
     */
    private void edit() {
        new EditTravelEntity(null, new EditTravelEntity.Callback() {
            @Override
            public void onSuccess(TravelEntity entity) {

            }

            @Override
            public void onFail() {

            }
        }).execute();
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