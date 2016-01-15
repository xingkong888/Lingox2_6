package cn.lingox.android.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.Calendar;

import cn.lingox.android.R;
import cn.lingox.android.activity.select_area.SelectCountry;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Travel;
import cn.lingox.android.helper.DatePickerFragment;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.TravelPlanAsynTask;

/**
 * 用户添加旅行计划
 * 通过“UserInfoFragment”中跳转过来
 */
public class AddTravelActivity extends ActionBarActivity implements OnClickListener {
    private static final int SELECTLOCATION = 124;//选择地址的请求码

    private TextView locationInfo, startTimeInfo, endTimeInfo;

    private Travel travel = new Travel();
    private Calendar calendar = Calendar.getInstance();

    private long start = 0, end = 0;
    /**
     * 日期选择监听器
     */
    private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day, 0, 0, 0);
            start = calendar.getTimeInMillis() / 1000L;
            if (end == 0 || end >= start) {
                UIHelper.getInstance().textViewSetPossiblyNullString(
                        startTimeInfo, JsonHelper.getInstance().parseTimestamp((int) start, 2));
                travel.setStartTime((int) start);
            } else {
                Toast.makeText(AddTravelActivity.this, getString(R.string.start_end), Toast.LENGTH_SHORT).show();
                startDatePickerDialog();
            }
        }
    };
    /**
     * 日期选择监听器
     */
    private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day, 23, 59, 59);
            end = calendar.getTimeInMillis() / 1000L;
            //获取系统当前时间
            long now = System.currentTimeMillis() / 1000L;
            if (end - now >= 0 && (start == 0 || end >= start)) {
                UIHelper.getInstance().textViewSetPossiblyNullString(
                        endTimeInfo, JsonHelper.getInstance().parseTimestamp((int) end, 2));
                travel.setEndTime((int) end);
            } else {
                Toast.makeText(AddTravelActivity.this, getString(R.string.end_start), Toast.LENGTH_SHORT).show();
                endDatePickerDialog();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        //如果是编辑，则将数据设置到对应的控件
        if (getIntent().hasExtra("edit")) {
            travel = getIntent().getParcelableExtra("edit");
            initData();
        }
    }

    /**
     * 设置数据
     */
    private void initData() {
        locationInfo.setText(LingoXApplication.getInstance().getLocation(
                travel.getCountry(), travel.getProvince(), travel.getCity()
        ));
        start = travel.getStartTime();
        end = travel.getEndTime();
        UIHelper.getInstance().textViewSetPossiblyNullString(
                startTimeInfo, JsonHelper.getInstance().parseTimestamp(start, 1));
        UIHelper.getInstance().textViewSetPossiblyNullString(
                endTimeInfo, JsonHelper.getInstance().parseTimestamp(end, 1));
    }

    /**
     * 初始化控件
     */
    private void initView() {
        setContentView(R.layout.row_travel_experiences);
        //“CANCEL”
        findViewById(R.id.travel_cancel).setOnClickListener(this);
        //“DONE”
        findViewById(R.id.travel_done).setOnClickListener(this);

        locationInfo = (TextView) findViewById(R.id.travel_location);
        locationInfo.setOnClickListener(this);
        //选择开始时间
        findViewById(R.id.start_time).setOnClickListener(this);
        startTimeInfo = (TextView) findViewById(R.id.start_time_info);
        //选择结束时间
        findViewById(R.id.end_time).setOnClickListener(this);
        endTimeInfo = (TextView) findViewById(R.id.end_time_info);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.travel_cancel://取消
                showCancel();
                break;
            case R.id.travel_done://提交
                if (locationInfo.getText().toString().isEmpty() || startTimeInfo.getText().toString().isEmpty()
                        || endTimeInfo.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Please complete the information", Toast.LENGTH_SHORT).show();
                } else {
                    if (getIntent().hasExtra("edit")) {
                        new TravelPlanAsynTask(this, travel, "edit").execute();
                    } else {
                        new TravelPlanAsynTask(this, travel, "create").execute();
                    }
                }
                break;
            case R.id.travel_location://选择地点
                Intent intent = new Intent(this, SelectCountry.class);
                intent.putExtra(SelectCountry.SELECTLOCATION, SELECTLOCATION);
                startActivityForResult(intent, SELECTLOCATION);
                break;
            case R.id.start_time://选择开始时间
                startDatePickerDialog();
                break;
            case R.id.end_time://选择结束时间
                endDatePickerDialog();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECTLOCATION://选择地点
                String str = data.getStringExtra(SelectCountry.SELECTED);
                if (!str.isEmpty()) {
                    travel.setLocation(str);
                    locationInfo.setText(str);
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showCancel();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //弹框询问用户是否确定取消
    private void showCancel() {
        new AlertDialog.Builder(this)
                .setMessage("Cancel the editor?")
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                }).setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    //时间选择器------开始
    private void startDatePickerDialog() {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setCallback(startDateListener);
        newFragment.setValues(calendar);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    //时间选择器------结束
    private void endDatePickerDialog() {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setCallback(endDateListener);
        newFragment.setValues(calendar);
        newFragment.show(getFragmentManager(), "datePicker");
    }
}