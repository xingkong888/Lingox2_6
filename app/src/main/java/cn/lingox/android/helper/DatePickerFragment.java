package cn.lingox.android.helper;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Calendar;

import cn.lingox.android.widget.MyDatePickerDialog;

/**
 * Created by wangxinxing on 2015/12/25.
 * <p/>
 * 日期选择器
 */
public class DatePickerFragment extends DialogFragment {
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
        return new MyDatePickerDialog(getActivity(), onDateSet, year, month, day);
    }
}
