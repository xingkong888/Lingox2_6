package cn.lingox.android.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.video.util.AsyncTask;


public class AddTravelActivity extends ActionBarActivity implements OnClickListener {
    private static final int SELECTLOCATION = 124;

    private TextView cancel, done, locationInfo, startTime, startTimeInfo, endTime, endTimeInfo;

    private Travel travel = new Travel();
    private Calendar calendar = Calendar.getInstance();

    private long start = 0, end = 0, now = System.currentTimeMillis() / 1000L;

    private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day, 0, 0, 0);
            start = calendar.getTimeInMillis() / 1000L;
            if (end == 0 ? true : end >= start) {
                UIHelper.getInstance().textViewSetPossiblyNullString(
                        startTimeInfo, JsonHelper.getInstance().parseTimestamp((int) start, 1));
                travel.setStartTime((int) start);
            } else {
                Toast.makeText(AddTravelActivity.this, getString(R.string.start_end), Toast.LENGTH_SHORT).show();
                startDatePickerDialog();
            }
        }
    };
    private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day, 23, 59, 59);
            end = calendar.getTimeInMillis() / 1000L;
            if (end - now >= 0 && (start == 0 ? true : end >= start)) {
                UIHelper.getInstance().textViewSetPossiblyNullString(
                        endTimeInfo, JsonHelper.getInstance().parseTimestamp((int) end, 1));
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
        if (getIntent().hasExtra("edit")) {
            travel = getIntent().getParcelableExtra("edit");
            initData();
        }
    }

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

    private void initView() {
        setContentView(R.layout.row_travel_experiences);

        cancel = (TextView) findViewById(R.id.travel_cancel);
        cancel.setOnClickListener(this);

        done = (TextView) findViewById(R.id.travel_done);
        done.setOnClickListener(this);

        locationInfo = (TextView) findViewById(R.id.travel_location);
        locationInfo.setOnClickListener(this);

        startTime = (TextView) findViewById(R.id.start_time);
        startTime.setOnClickListener(this);

        startTimeInfo = (TextView) findViewById(R.id.start_time_info);
        startTimeInfo.setOnClickListener(this);

        endTime = (TextView) findViewById(R.id.end_time);
        endTime.setOnClickListener(this);

        endTimeInfo = (TextView) findViewById(R.id.end_time_info);
        endTimeInfo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.travel_cancel:
                new android.app.AlertDialog.Builder(this)
                        .setMessage("Cancel Edit?")
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
                }).create()
                        .show();
                break;
            case R.id.travel_done:
                if (locationInfo.getText().toString().isEmpty() || startTimeInfo.getText().toString().isEmpty()
                        || endTimeInfo.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Please complete the information", Toast.LENGTH_SHORT).show();
                } else {
                    if (getIntent().hasExtra("edit")) {
                        new editExperience().execute();
                    } else {
                        new createExperience().execute();
                    }
                }
                break;
            case R.id.travel_location:
                Intent intent = new Intent(this, SelectCountry.class);
                intent.putExtra(SelectCountry.SELECTLOCATION, SELECTLOCATION);
                startActivityForResult(intent, SELECTLOCATION);
                break;
            case R.id.start_time:
            case R.id.start_time_info:
                //选择开始时间
                startDatePickerDialog();
                break;
            case R.id.end_time:
            case R.id.end_time_info:
                //选择结束时间
                endDatePickerDialog();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode==RESULT_OK)
        switch (requestCode) {
            case SELECTLOCATION:
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
            }).create()
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

    private class createExperience extends AsyncTask<Void, String, Boolean> {
        final ProgressDialog pd = new ProgressDialog(AddTravelActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            pd.setMessage("Submiting……");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ServerHelper.getInstance().createExperiences(
                        CacheHelper.getInstance().getSelfInfo().getId(),
                        String.valueOf(travel.getStartTime()),
                        String.valueOf(travel.getEndTime()),
                        travel.getCountry(), travel.getProvince(), travel.getCity(), "[]");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                AddTravelActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.setMessage("Success");
                    }
                });
                Intent returnIntent = new Intent();
                returnIntent.putExtra("Travel", travel);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        }
    }

    private class editExperience extends AsyncTask<Void, String, Boolean> {
        final ProgressDialog pd = new ProgressDialog(AddTravelActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            pd.setMessage("Submiting……");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ServerHelper.getInstance().editExperiences(
                        travel.getId(),
                        String.valueOf(travel.getStartTime()),
                        String.valueOf(travel.getEndTime()),
                        travel.getCountry(), travel.getProvince(), travel.getCity(), "[]");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                AddTravelActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                    }
                });
                Intent returnIntent = new Intent();
                returnIntent.putExtra("Travel", travel);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        }
    }
}