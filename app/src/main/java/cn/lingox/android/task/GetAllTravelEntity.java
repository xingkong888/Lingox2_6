package cn.lingox.android.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.ServerHelper;

/**
 * 获取所有的旅行者发布的信息
 */
public class GetAllTravelEntity extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "GetAllTravelEntity";

    private Callback callback;
    private int page;
    private ArrayList<TravelEntity> list = new ArrayList<>();
    private Context context;

    public GetAllTravelEntity(int page, Callback callback, Context context) {
        this.callback = callback;
        this.page = page;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            list.addAll(ServerHelper.getInstance().getAllTravel(page));
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to get all TravelEntity: " + e.toString());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
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