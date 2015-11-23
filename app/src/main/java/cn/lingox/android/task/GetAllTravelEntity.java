package cn.lingox.android.task;

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

    public GetAllTravelEntity(int page, Callback callback) {
        this.callback = callback;
        this.page = page;
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

    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            callback.onSuccess(list);
        } else {
            callback.onFail();
        }
    }

    public interface Callback {
        void onSuccess(ArrayList<TravelEntity> list);

        void onFail();
    }
}