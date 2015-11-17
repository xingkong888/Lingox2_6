package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.utils.DpToPx;
import cn.lingox.android.video.util.AsyncTask;

public class PathCardImgDialog extends Activity {
    public static final String PRESET_URI = LingoXApplication.PACKAGE_NAME + ".PRESET_URI";
    private LinearLayout recommendImgList;
    private ArrayList<String> imgUrl = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_img_pathcard);
        init();
    }

    private void init() {
        recommendImgList = (LinearLayout) findViewById(R.id.list_img_recommend);
        new getImgUrl().execute();
    }

    private View getPathImg(final String path) {
        View pathImg = getLayoutInflater().inflate(R.layout.row_img_recommend, recommendImgList, false);

        CardView cardView = (CardView) pathImg.findViewById(R.id.path_cardView);
        ViewGroup.LayoutParams params = cardView.getLayoutParams();
        params.height = LingoXApplication.getInstance().getWidth() - DpToPx.dip2px(getApplicationContext(), 20);
        cardView.setLayoutParams(params);

        ImageView imageView = (ImageView) pathImg.findViewById(R.id.path_img);

        Picasso.with(this).load(path).into(imageView);
        /*Picasso.with(this).load()*/
        pathImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pathImg = new Intent();
                pathImg.putExtra(PRESET_URI, path);
                setResult(RESULT_OK, pathImg);
                finish();
            }
        });
        return pathImg;
    }


    private class getImgUrl extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            imgUrl.clear();
            imgUrl.addAll(JsonHelper.getInstance().getAllPathImg());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            for (int i = 0, j = imgUrl.size(); i < j; i++)
                recommendImgList.addView(getPathImg(imgUrl.get(i)));
        }
    }
}