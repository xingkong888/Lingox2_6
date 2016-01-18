package cn.lingox.android.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.ServerHelper;

/**
 * 根据国家、省份、城市搜索
 */
public class SearchTravelEntity extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "GetAllTravelEntity";

    private Callback callback;
    private int page;
    private ArrayList<TravelEntity> list;
    private String country, province, city;
    private Context context;
//    private ProgressDialog pd;

    public SearchTravelEntity(
            String country, String province, String city, int page, Callback callback, Context context) {
        this.callback = callback;
        this.page = page;
        list = new ArrayList<>();
        this.country = country;
        this.province = province;
        this.city = city;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        pd=new ProgressDialog(context);
//        pd.setMessage("Loading...");
//        pd.show();
//        pd.setCancelable(false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            list.addAll(ServerHelper.getInstance().getAllTravel(country, province, city, page));
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to search TravelEntity: " + e.toString());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
//        pd.dismiss();
        if (success) {
            callback.onSuccess(list);
        } else {
            callback.onFail();
        }
    }

    /**
     * 回调接口
     */
    public interface Callback {
        void onSuccess(ArrayList<TravelEntity> list);

        void onFail();
    }
}