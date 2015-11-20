package cn.lingox.android.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import cn.lingox.android.entity.Travel;
import cn.lingox.android.helper.ServerHelper;

/**
 * 创建和修改旅行计划
 * Create by wangxinxing on 04/11/2015
 */
public class TravelPlanAsynTask extends AsyncTask<Void, Void, Boolean> {
    private ProgressDialog pd;
    private Activity context;
    private Travel travel;
    private String flag = "";

    public TravelPlanAsynTask(Context context, Travel travel, String flag) {
        this.context = (Activity) context;
        this.travel = travel;
        pd = new ProgressDialog(context);
        this.flag = flag;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        pd.setMessage("Submiting……");
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            ServerHelper.getInstance().travel(flag, travel);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.setMessage("Success");
                }
            });
            Intent returnIntent = new Intent();
            returnIntent.putExtra("Travel", travel);
            context.setResult(Activity.RESULT_OK, returnIntent);
            context.finish();
        }
    }
}
