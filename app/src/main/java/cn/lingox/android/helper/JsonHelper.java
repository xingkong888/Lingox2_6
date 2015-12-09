package cn.lingox.android.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import cn.lingox.android.R;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.Travel;
import cn.lingox.android.entity.location.Country1;
import cn.lingox.android.entity.location.Provinces;

public class JsonHelper {
    private static final String LOG_TAG = "JsonHelper";

    //    private static final String COUNTRIES_AND_CITIES_JSON = "json/countries_and_cities.json";
    private static final String LANGUAGES_JSON = "json/languages.json";
    private static final String PATHTAGS_JSON = "json/pathTags.json";

    private static JsonHelper instance = null;
    private Context context;
    private Gson gson = null;
    private ArrayList<Country1> countriesList = null;
    private ArrayList<String> pathImgList = new ArrayList<>();
    private String[] allLanguagesList;
    private Locale locale;
    private ArrayList<String> allTags = null;

    /**
     * 创建JsonHelper的实例
     * 同时创建Gson的实例
     */
    private JsonHelper() {
        if (null == gson) {
            gson = new Gson();
        }
    }

    /**
     * 单例模式
     *
     * @return JsonHelper的实例
     */
    public static JsonHelper getInstance() {
        if (instance == null) {
            instance = new JsonHelper();
        }
        return instance;
    }

    /**
     * @param url url
     * @return JSONObject
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String jsonText = readAll(rd);
        is.close();
        return new JSONObject(jsonText);
    }

    /**
     * 读取输入流中的数据
     *
     * @param rd ""
     * @return String
     * @throws IOException
     */
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * 将文件转换成String
     *
     * @param context  上下文
     * @param fileName 文件名
     * @return String
     * @throws IOException
     */
    private static String readFileAsString(Context context, String fileName)
            throws IOException {
        InputStream is = context.getAssets().open(fileName);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, "UTF-8");
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * 将json解析成实体类
     *
     * @param jsonStr json字符串
     * @param cl      实体类（不知道如何描述）
     * @param <T>     实体类（不知道如何描述）
     * @return 实体类（不知道如何描述）
     */
    @SuppressWarnings("unchecked")
    public <T> T jsonToBean(String jsonStr, Class<?> cl) {
        Object obj = null;
        try {
            if (instance.gson != null) {
                obj = instance.gson.fromJson(jsonStr, cl);
            }
            return (T) obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将json解析成Travel实体类
     *
     * @param jsonStr json字符串
     * @return Travel实例的集合
     */
    public ArrayList<Travel> jsonToTravel(String jsonStr) {
        ArrayList<Travel> list = new ArrayList<>();
        Travel travel;
        JSONObject jsonObj;
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0, j = jsonArray.length(); i < j; i++) {
                jsonObj = jsonArray.getJSONObject(i);
                travel = new Travel();
                travel.setId(jsonObj.optString("id"));

                travel.setStartTime(jsonObj.optLong("startTime"));
                travel.setEndTime(jsonObj.optLong("endTime"));

                travel.setTags(jsonObj.getJSONArray("tags").toString());
                travel.setCountry(jsonObj.optString("country"));
                if (!jsonObj.optString("city").isEmpty()) {
                    travel.setCity(jsonObj.optString("city"));
                }
                if (!jsonObj.optString("province").isEmpty()) {
                    travel.setProvince(jsonObj.optString("province"));
                }
                list.add(travel);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }
    }

    /**
     * 获取用户id
     * （没看到干嘛要这样）
     *
     * @param data json字符串
     * @return String
     */
    public String getUserId(JSONObject data) {
        try {
            return data.getString(StringConstant.userIdStr);
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * 根据给定的long类型格式化时间
     *
     * @param timestamp long类型的时间
     * @param type      1：带时、分 2：不带时、分
     * @return 格式化后的时间
     */
    public String parseTimestamp(long timestamp, int type) {
        if (locale == null) {
            getLocal();
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp * 1000L);
        SimpleDateFormat format = null;
        switch (type) {
            case 1://
                format = new SimpleDateFormat("yyyy-MM-dd kk:mm EEE", locale == null ? Locale.CHINA : locale);
                break;
            case 2://
                format = new SimpleDateFormat("yyyy-MM-dd", locale == null ? Locale.CHINA : locale);
                break;
        }
        return format == null ? "DateTime Parse Error!" : format.format(c.getTime());
    }

    /**
     * 格式化时间
     *
     * @param date yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     * @return 格式化后的时间
     */
    public String parseSailsJSDate(String date) {
        try {
            // Locale is the servers location (ie China)
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", locale == null ? Locale.CHINA : locale);
            df.setTimeZone(TimeZone.getTimeZone("Zulu"));
            Date parsedDate;
            parsedDate = df.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(parsedDate);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm", locale == null ? Locale.CHINA : locale);
            return (format.format(c.getTime()));
        } catch (ParseException e) {
            Log.e(LOG_TAG, "parseSailsJSDate(): Error: " + e.getMessage());
            return "DateTime Parse Error!";
        }
    }

    /**
     * 获取语言设置
     */
    private void getLocal() {
        if (CacheHelper.getInstance().getSettingLanguage() != null) {
            locale = new Locale(CacheHelper.getInstance().getSettingLanguage());
        } else {
            Configuration conf = context.getResources().getConfiguration();
            locale = conf.locale;
        }
    }

    /**
     * 根据时间，判断距当前时间的间隔
     *
     * @param date yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     * @param type 无用，为了与parseSailsJSDate(String date)区别
     * @return 距当前时间的间隔（描述的不恰当）
     */
    public String parseSailsJSDate(String date, int type) {
        getLocal();
        try {
            // Locale is the servers location (ie China)
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", locale == null ? Locale.CHINA : locale);
            df.setTimeZone(TimeZone.getTimeZone("Zulu"));
            Date parsedDate;
            parsedDate = df.parse(date);
            int i = comparePathDate(parsedDate);
            Calendar c = Calendar.getInstance();
            c.setTime(parsedDate);
            SimpleDateFormat format;
            switch (i) {
                case 0://今天
                    format = new SimpleDateFormat("HH:mm", locale == null ? Locale.CHINA : locale);
                    return format.format(c.getTime());
                case 1://年份不同
                case 2://月份不同
                case 3://大于两天
                    format = new SimpleDateFormat("yy/MM/dd", locale == null ? Locale.CHINA : locale);
                    return format.format(c.getTime());
                case 4://昨天
                    return context.getString(R.string.date);
            }
            return null;
        } catch (ParseException e) {
            Log.e(LOG_TAG, "parseSailsJSDate(): Error: " + e.getMessage());
            return "DateTime Parse Error!";
        }
    }

    public int comparePathDate(Date time) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(System.currentTimeMillis());
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(time.getTime());
        if (c2.get(Calendar.YEAR) < c1.get(Calendar.YEAR)) {
            return 1;//年份不同
        } else if (c2.get(Calendar.MONTH) < c1.get(Calendar.MONTH)) {
            return 2;//月份不同
        } else if (c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH) >= 2) {
            return 3;//大于一天
        } else if (c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH) == 1) {
            return 4;//昨天
        } else {
            return 0;//当天信息
        }
    }

    public long sailsJSDateToTimestamp(String date) throws ParseException {
        // Locale is the servers location (ie China)
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", locale == null ? Locale.CHINA : locale);
        df.setTimeZone(TimeZone.getTimeZone("Zulu"));
        Date parsedDate;
        parsedDate = df.parse(date);
        Calendar c = Calendar.getInstance();
        c.setTime(parsedDate);
        return (c.getTimeInMillis());
    }

    public String getLocationStr(double[] longNlat) {
        return "[" + String.valueOf(longNlat[0]) + "," + String.valueOf(longNlat[1]) + "]";
    }

    /**
     * 通过给定的条件，分割字符串
     *
     * @param interests 待分割的字符串
     * @return 分割后的字符串
     */
    public String getInterestsJson(String interests) {
        JSONArray jsonArray = new JSONArray();
        String[] interestSplit = interests.split(",");
        for (String anInterestSplit : interestSplit) {
            jsonArray.put(anInterestSplit.trim());
        }
        return jsonArray.toString();
    }

    /**
     * 获取所有的图片链接
     * 应该是提供的默认图片的链接
     *
     * @return 链接的集合
     */
    public ArrayList<String> getAllPathImg() {
        if (pathImgList.isEmpty()) {
            try {
                JSONArray tempArray = readJsonFromUrl("http://lingox.cn/json/activity_images.json").getJSONArray("images");
                for (int i = 0, j = tempArray.length(); i < j; i++) {
                    pathImgList.add(tempArray.getString(i));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        return pathImgList;
    }

    /**
     * 获取所有国家和城市
     *
     * @return 国家、城市集合
     */
    public ArrayList<Country1> getCountries() {
        if (countriesList == null) {
            try {
                countriesList = new ArrayList<>();
                // Read from local file
                String allCountriesString = readFileAsString(context, "json/location.json");
                JSONObject jsonObject = new JSONObject(allCountriesString);
                Iterator<?> keys = jsonObject.keys();
                JSONArray array;
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    Country1 country1 = new Country1();
                    country1.setCountryCode(key);
                    country1.setCountry(jsonObject.getJSONObject(key).getString("name"));
                    JSONObject obj = jsonObject.getJSONObject(key).getJSONObject("provinces");
                    Iterator<?> keys1 = obj.keys();
                    ArrayList<Provinces> provincesDatas = new ArrayList<>();
                    while (keys1.hasNext()) {
                        String key1 = (String) keys1.next();
                        Provinces provinces = new Provinces();
                        provinces.setProvinces(key1);
                        array = obj.getJSONArray(key1);
                        ArrayList<String> city = new ArrayList<>();
                        for (int i = 0, j = array.length(); i < j; i++) {
                            city.add(array.getString(i));
                        }
                        provinces.setCity(city);
                        provincesDatas.add(provinces);
                    }
                    country1.setProvinces(provincesDatas);
                    countriesList.add(country1);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "getCountries()：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return countriesList;
    }

    /**
     * 获取标签
     *
     * @return 标签集合
     */
    public ArrayList<String> getAllTags() {
        if (allTags == null) {
            try {
                allTags = new ArrayList<>();
                // Read from local file
                String allCountriesString = readFileAsString(context, PATHTAGS_JSON);
                JSONObject jsonObject = new JSONObject(allCountriesString);
                for (int i = 1, j = jsonObject.length(); i <= j; i++) {
                    allTags.add(jsonObject.getString(String.valueOf(i)));
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "getAllCountries() Exception Caught");
                e.printStackTrace();
            }
        }
        return allTags;
    }

    // TODO make us retrieve this JSON file from the server,
    // this will allow us to keep the countries/cities list up to date even if
    // the user doesn't update their app

    /**
     * 获取语言
     *
     * @return 语言的集合
     */
    public String[] getLanguages() {
        if (allLanguagesList == null) {
            try {
                // Read from local file
                String allLanguagesString = readFileAsString(context, LANGUAGES_JSON);
                JSONObject jsonObject = new JSONObject(allLanguagesString);
                JSONArray jsonArray = jsonObject.getJSONArray("languages");

                ArrayList<String> tempList = new ArrayList<>();
                for (int i = 0, j = jsonArray.length(); i < j; i++) {
                    tempList.add(jsonArray.getString(i).trim());
                }
                allLanguagesList = new String[tempList.size()];
                allLanguagesList = tempList.toArray(allLanguagesList);
            } catch (Exception e) {
                Log.e(LOG_TAG, "getAllLanguages() Exception Caught");
                e.printStackTrace();
            }
        }
        return allLanguagesList;
    }

    /**
     * 通过国家名，获取对应的代码，用于查找国家国旗
     *
     * @param country 国家名
     * @return 国家代码
     */
    public String getCodeFromCountry(String country) {
        if (TextUtils.isEmpty(country)) {
            return null;
        }
        getCountries();
        for (Country1 country1 : countriesList) {
            if (country1.getCountry().contentEquals(country))
                return country1.getCountryCode();
        }
        return null;
    }
}
