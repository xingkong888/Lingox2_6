package cn.lingox.android.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import cn.lingox.android.activity.PhotoActivity;
import cn.lingox.android.entity.Photo;
import cn.lingox.android.helper.ServerHelper;

/**
 * 提交图片
 */
public class PostPhoto extends AsyncTask<Void, Void, Boolean> {
    private ProgressDialog pd;
    private boolean isSuccess = false;
    private Button post;//提交按钮
    private Photo photo;
    private String description = "";//图片的描述
    private Activity context;

    public PostPhoto(Context context, Button post, Photo photo, String description) {
        this.context = (Activity) context;
        pd = new ProgressDialog(context);
        this.post = post;
        this.photo = photo;
        this.description = description;
    }

    @Override
    protected void onPreExecute() {
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        pd.setMessage("Uploading Image");
        post.setClickable(false);
        photo.setDescription(description);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            photo = ServerHelper.getInstance().editPhoto(photo);
            isSuccess = true;
        } catch (Exception e) {
            Log.e("PostPhoto", e.toString());
            isSuccess = false;
        }
        return isSuccess;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        post.setClickable(true);
        pd.dismiss();
        if (success) {
            Toast.makeText(context, "Uploaded successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra(PhotoActivity.EDITED_PHOTO, photo);
            context.setResult(PhotoActivity.RESULT_PHOTO_EDITED, intent);
            context.finish();
        } else {
            Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
        }
    }
}
