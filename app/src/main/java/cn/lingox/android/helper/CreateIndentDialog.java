package cn.lingox.android.helper;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.activity.ChatActivity;
import cn.lingox.android.activity.PathEditActivity;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.Indent;

public class CreateIndentDialog extends DialogFragment implements View.OnClickListener {
    private long start = 0, end = 0, now = System.currentTimeMillis() / 1000L;
    private Calendar calendar = Calendar.getInstance();

    private Indent indent = new Indent();
    private TextView startTime, endTime;
    private LinearLayout local,traveler;

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
    private EditText num, travelerDescribe,time,localDescribe;
    private String pathId, tarId;
    private int type;
    private Button send, cancel;
    private String username, nickname;
    private HashMap<String, String> map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_indent, null);
        Bundle bundle = getArguments();
        if (!bundle.isEmpty()) {
            if (!bundle.getString("pathId").isEmpty()) {
                pathId = bundle.getString("pathId");
            }
            if (!bundle.getString("tarId").isEmpty()) {
                tarId = bundle.getString("tarId");
            }
            if (!bundle.getString("username").isEmpty()) {
                username = bundle.getString("username");
            }
            if (!bundle.getString(StringConstant.nicknameStr).isEmpty()) {
                nickname = bundle.getString(StringConstant.nicknameStr);
            }
            if (bundle.getInt("type")!=0) {
                type=bundle.getInt("type");
            }
        }

        initView(view);
        return view;
    }

    private void initView(View view) {
        startTime = (TextView) view.findViewById(R.id.indent_traveler_start);
        startTime.setOnClickListener(this);
        endTime = (TextView) view.findViewById(R.id.indent_traveler_end);
        endTime.setOnClickListener(this);
        num = (EditText) view.findViewById(R.id.indent_traveler_num);
        travelerDescribe = (EditText) view.findViewById(R.id.indent_traveler_describe);
        localDescribe = (EditText)view.findViewById(R.id.indent_local_describe);
        time = (EditText)view.findViewById(R.id.indent_local_time);

        send = (Button) view.findViewById(R.id.indent_send);
        send.setOnClickListener(this);
        cancel = (Button) view.findViewById(R.id.indent_cancel);
        cancel.setOnClickListener(this);

        local=(LinearLayout)view.findViewById(R.id.local);
        traveler=(LinearLayout)view.findViewById(R.id.traveler);

        local.setVisibility(View.VISIBLE);

        switch (type){
            case 1://local
                getDialog().setTitle("Application");
                break;
            case 2://traveler
                getDialog().setTitle("Acceptance");
                localDescribe.setHint("Help "+username+" know you better.");
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.indent_traveler_start:
                startDatePickerDialog();
                break;
            case R.id.indent_traveler_end:
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
                    Toast.makeText(getActivity(), "Please complete the information", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.indent_cancel:
//                finish();
                dismiss();
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
            bar = new ProgressDialog(getActivity());
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
                dismiss();
                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra("username", username);
                chatIntent.putExtra(StringConstant.nicknameStr, nickname);
                chatIntent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                chatIntent.putExtra("describe", travelerDescribe.getText().toString());
                chatIntent.putExtra("Indent", indent);
                startActivity(chatIntent);
                getActivity().overridePendingTransition(R.anim.zoom_exit, R.anim.zoom_enter);
            }
        }
    }
}
