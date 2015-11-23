package cn.lingox.android.helper;

import android.content.Context;
import android.content.res.Configuration;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.lingox.android.R;

public class TimeHelper {
    private static TimeHelper instance;
    private Context context;
    private Locale locale;

    public static TimeHelper getInstance() {
        if (instance == null)
            instance = new TimeHelper();
        return instance;
    }

    /**
     * @param inputTime 传入的时间格式必须类似于“yyyy-MM-dd HH:mm:ss”这样的格式
     * @return 结果
     */
    private String getInterval(String inputTime) {

        if (inputTime.length() != 19) {
            return inputTime;
        }
        String result;
        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ParsePosition pos = new ParsePosition(0);
            Date d1 = sd.parse(inputTime, pos);
            // 用现在距离1970年的时间间隔new
            // Date().getTime()减去以前的时间距离1970年的时间间隔
            // d1.getTime()得出的就是以前的时间与现在时间的时间间隔
            long time = new Date().getTime() - d1.getTime();// 得出的时间间隔是毫秒
            if (time / 1000 <= 0) {
                // 如果时间间隔小于等于0秒则显示“刚刚”time/10得出的时间间隔的单位是秒
                result = "刚刚";
            } else if (time / 1000 < 60) {
                // 如果时间间隔小于60秒则显示多少秒前
                int se = (int) ((time % 60000) / 1000);
                result = se + "秒前";
            } else if (time / 60000 < 60) {
                // 如果时间间隔小于60分钟则显示多少分钟前
                int m = (int) ((time % 3600000) / 60000);// 得出的时间间隔的单位是分钟
                result = m + "分钟前";
            } else if (time / 3600000 < 24) {
                // 如果时间间隔小于24小时则显示多少小时前
                int h = (int) (time / 3600000);// 得出的时间间隔的单位是小时
                result = h + "小时前";
            } else if (time / 86400000 < 2) {
                // 如果时间间隔小于2天则显示昨天
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                result = sdf.format(d1.getTime());
                result = "昨天" + result;
            } else if (time / 86400000 < 3) {
                // 如果时间间隔小于3天则显示前天
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                result = sdf.format(d1.getTime());
                result = "前天" + result;
            } else if (time / 86400000 < 30) {
                // 如果时间间隔小于30天则显示多少天前
                SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
                result = sdf.format(d1.getTime());
            } else if (time / 2592000000l < 12) {
                // 如果时间间隔小于12个月则显示多少月前
                SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
                result = sdf.format(d1.getTime());
            } else {
                // 大于1年，显示年月日时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                result = sdf.format(d1.getTime());
            }
        } catch (Exception e) {
            return inputTime;
        }
        return result;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setContext(Context context) {
        this.context = context;
        if (CacheHelper.getInstance().getSettingLanguage() != null) {
            locale = new Locale(CacheHelper.getInstance().getSettingLanguage());
        } else {
            Configuration conf = context.getResources().getConfiguration();
            locale = conf.locale;
        }
    }

    /**
     *
     * @param time 时间戳
     * @return 格式化后的时间
     */
    public String parseTimestampToDate(String time, String flg) {
        Calendar c = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", locale);
            SimpleDateFormat output;
            String returnTime = "";
            switch (flg) {
                case "UserInfo":
                    output = new SimpleDateFormat("yyyy/MM", locale);
                    Date d = sdf.parse(time);
                    c.setTimeInMillis(d.getTime());
                    c.add(Calendar.HOUR, 8);
                    returnTime = output.format(c.getTime());
                    break;
                case "TravelEntity":
                    output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
                    Date d1 = sdf.parse(time);
                    c.setTimeInMillis(d1.getTime());
                    c.add(Calendar.HOUR, 8);
                    returnTime = getInterval(output.format(c.getTime()));
                    break;
            }

            return returnTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private int comparePathDate(long time) {
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
        SimpleDateFormat format = new SimpleDateFormat("MMM-dd EEE", locale);
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
