package cn.lingox.android.task;

import android.os.AsyncTask;
import android.util.Log;

import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.ServerHelper;

/**
 * 获取指定id的数据
 */
public class GetTravelEntity extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "GetTravelEntity";

    private Callback callback;
    private String id;
    private TravelEntity travelEntity;

    public GetTravelEntity(String id, Callback callback) {
        this.callback = callback;
        this.id = id;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            travelEntity = ServerHelper.getInstance().getTravel(id);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to get TravelEntity: " + e.toString());
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