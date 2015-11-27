package cn.lingox.android.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.ServerHelper;

/**
 * 删除
 */
public class DeleteTravelEntity extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "DeleteTravelEntity";

    private Callback callback;
    private String id;
    private TravelEntity travelEntity;
    private ProgressDialog pd;

    public DeleteTravelEntity(Context context, String id, Callback callback) {
        this.callback = callback;
        this.id = id;
        pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");
        pd.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            travelEntity = ServerHelper.getInstance().deleteTravel(id);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to delete TravelEntity: " + e.toString());
            return false;
        }
    }

    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        pd.dismiss();
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