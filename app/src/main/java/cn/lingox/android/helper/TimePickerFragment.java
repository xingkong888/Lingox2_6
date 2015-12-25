package cn.lingox.android.helper;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Calendar;

/**
 * Created by wangxinxing on 2015/12/25.
 * <p/>
 * 时间选择器
 */
public class TimePickerFragment extends DialogFragment {
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
