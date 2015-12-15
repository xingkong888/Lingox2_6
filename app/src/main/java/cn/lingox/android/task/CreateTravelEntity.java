package cn.lingox.android.task;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;

import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.ServerHelper;

/**
 * 创建旅行者体验的异步任务
 */
public class CreateTravelEntity extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "CreateTravelEntity";

    private Callback callback;
    private HashMap<String, String> maps;
    private TravelEntity travelEntity;

    public CreateTravelEntity(HashMap<String, String> params, Callback callback) {
        this.callback = callback;
        this.maps = params;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            travelEntity = ServerHelper.getInstance().createTravel(maps);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to create TravelEntity: " + e.toString());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            callback.onSuccess(travelEntity);
        } else {
            callback.onFail();
        }
    }

    /**
     * 回调接口
     */
    public interface Callback {
        void onSuccess(TravelEntity entity);

        void onFail();
    }
}