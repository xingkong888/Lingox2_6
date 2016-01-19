package cn.lingox.android.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;

import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.ServerHelper;

/**
 * like旅行者发布的体验
 * like
 */
public class LikeTravelEntity extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "LikeTravelEntity";

    private Callback callback;
    private HashMap<String, String> map;
    private TravelEntity travelEntity;
    private Context context;
    private ProgressDialog pd;

    public LikeTravelEntity(Context context,HashMap<String, String> map, Callback callback) {
        this.callback = callback;
        this.map = map;
        this.context=context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd=new ProgressDialog(context);
        pd.setMessage("Loading...");
        pd.show();
        pd.setCancelable(false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            travelEntity = ServerHelper.getInstance().likeTravel(map);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to like TravelEntity: " + e.toString());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        pd.dismiss();
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