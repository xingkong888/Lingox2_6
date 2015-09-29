package cn.lingox.android.helper;

import android.content.Context;
import android.content.res.Configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.lingox.android.R;

/**
 * Created by hugo on 2015/4/10.
 */
public class TimeHelper {
    private static TimeHelper instance;
    private Context context;
    private Locale locale;

    public static TimeHelper getInstance() {
        if (instance == null)
            instance = new TimeHelper();
        return instance;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setContext(Context context) {
        this.context = context;
        if (CacheHelper.getInstance().getSettingLanguage() != null)
            locale = new Locale(CacheHelper.getInstance().getSettingLanguage());
        else {
            Configuration conf = context.getResources().getConfiguration();
            locale = conf.locale;
        }

    }

    public String parseTimestampToDate(String time) {
        Calendar c = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CHINA);
            SimpleDateFormat output = new SimpleDateFormat("yyyy/MM", Locale.CHINA);
            Date d = sdf.parse(time);
            c.setTimeInMillis(d.getTime());
            c.add(Calendar.HOUR, 8);
            String formattedTime = output.format(c.getTime());

            return formattedTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public int comparePathDate(long time) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(System.currentTimeMillis());
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(time);
        if (c2.get(Calendar.YEAR) < c1.get(Calendar.YEAR)) {
            return 1;//年份不同
        } else if (c2.get(Calendar.MONTH) < c1.get(Calendar.MONTH)) {
            return 2;//月份不同
        } else if (c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH) > 2) {
            return 3;//大于两天
        } else if (c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH) == 1) {
            return 4;//昨天
        } else {
            return 0;//当天信息
        }
    }

    public String parseTimestampToDate(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp * 1000L);
        SimpleDateFormat format = new SimpleDateFormat("MMM-dd E", locale);
        return (format.format(c.getTime()));
    }

    public String parseTimestampToTime(long timestamp) {

        int i = comparePathDate(timestamp);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        SimpleDateFormat format;
        switch (i) {
            case 0://今天
                format = new SimpleDateFormat("HH:mm", locale);
                return format.format(c.getTime());
            case 1://年份不同
            case 2://月份不同
            case 3://大于两天
                format = new SimpleDateFormat("yy/MM/dd", locale);
                return format.format(c.getTime());
            case 4://昨天
                return context.getString(R.string.date);
        }
        return null;
    }
}
