package cn.lingox.android.task;

import android.os.AsyncTask;
import android.util.Log;

import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.ServerHelper;

/**
 * 获取指定id的数据
 */
public class GetTravel extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "GetTravel";

    private Callback callback;
    private String id;
    private TravelEntity travelEntity;

    public GetTravel(String id, Callback callback) {
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

    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            callback.onSuccess(travelEntity);
        } else {
            callback.onFail();
        }
    }

    public interface Callback {
        void onSuccess(TravelEntity entity);

        void onFail();
    }
}