package cn.lingox.android.helper;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.activity.ChatActivity;
import cn.lingox.android.activity.PathEditActivity;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.Indent;

public class CreateIndentDialog extends ActionBarActivity implements View.OnClickListener {
    private long start = 0, end = 0, now = System.currentTimeMillis() / 1000L;
    private Calendar calendar = Calendar.getInstance();

    private Indent indent = new Indent();
    private TextView startTime, endTime;

    private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day);
            start = calendar.getTimeInMillis() / 1000L;
            if (end == 0 ? true : end >= start) {
                UIHelper.getInstance().textViewSetPossiblyNullString(
                        startTime, TimeHelper.getInstance().parseTimestampToDate(start));
                indent.setStartTime(start);
            } else {
                startDatePickerDialog();
            }
        }
    };
    private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day);
            end = calendar.getTimeInMillis() / 1000L + 100;
            if (end - now >= 0 && (start == 0 ? true : end >= start)) {
                UIHelper.getInstance().textViewSetPossiblyNullString(
                        endTime, TimeHelper.getInstance().parseTimestampToDate(end));
                indent.setEndTime(end);
            } else {
                endDatePickerDialog();
            }
        }
    };
    private EditText num, describe;
    private String pathId, tarId;
    private Button send, cancel;
    private String username, nickname;
    private HashMap<String, String> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_indent);
        initView();
        if (getIntent().hasExtra("pathId")) {
            pathId = getIntent().getStringExtra("pathId");
        }
        if (getIntent().hasExtra("tarId")) {
            tarId = getIntent().getStringExtra("tarId");
        }
        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        }
        if (getIntent().hasExtra(StringConstant.nicknameStr)) {
            nickname = getIntent().getStringExtra(StringConstant.nicknameStr);
        }
    }

    private void initView() {
        startTime = (TextView) findViewById(R.id.indent_start);
        startTime.setOnClickListener(this);
        endTime = (TextView) findViewById(R.id.indent_end);
        endTime.setOnClickListener(this);
        num = (EditText) findViewById(R.id.indent_num);
        describe = (EditText) findViewById(R.id.indent_describe);
        send = (Button) findViewById(R.id.indent_send);
        send.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.indent_cancel);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.indent_start:
                startDatePickerDialog();
                break;
            case R.id.indent_end:
                endDatePickerDialog();
                break;
            case R.id.indent_send:
                map = new HashMap<>();
                map.put("userId", CacheHelper.getInstance().getSelfInfo().getId());
                map.put("tarId", tarId);
                map.put("pathId", pathId);
                if (start > 0 && end > 0 && !num.getText().toString().isEmpty()) {
                    map.put("startTime", String.valueOf(start));
                    map.put("endTime", String.valueOf(end));
                    map.put("participants", num.getText().toString().trim());
                    map.put("state", "1");
                    new CreateIndent().execute();
                } else {
                    Toast.makeText(this, "Please complete the information", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.indent_cancel:
                finish();
                break;
        }
    }

    public void startDatePickerDialog() {
        PathEditActivity.DatePickerFragment newFragment = new PathEditActivity.DatePickerFragment();
        newFragment.setCallback(startDateListener);
        newFragment.setValues(calendar);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void endDatePickerDialog() {
        PathEditActivity.DatePickerFragment newFragment = new PathEditActivity.DatePickerFragment();
        newFragment.setCallback(endDateListener);
        newFragment.setValues(calendar);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private class CreateIndent extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog bar;
        private Indent indent;

        @Override
        protected void onPreExecute() {
            bar = new ProgressDialog(CreateIndentDialog.this);
            bar.setMessage("In the submission");
            bar.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                indent = ServerHelper.getInstance().createApplication(map);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("CreateIndentDialog", e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            bar.dismiss();
            if (success) {
                Intent chatIntent = new Intent(CreateIndentDialog.this, ChatActivity.class);
                chatIntent.putExtra("username", username);
                chatIntent.putExtra(StringConstant.nicknameStr, nickname);
                chatIntent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                chatIntent.putExtra("describe", describe.getText().toString());
                chatIntent.putExtra("Indent", indent);
                startActivity(chatIntent);
                overridePendingTransition(R.anim.zoom_exit, R.anim.zoom_enter);
            }
        }
    }
}
