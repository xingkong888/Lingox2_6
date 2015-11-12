package cn.lingox.android.activity.imagechooser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.activity.imagechooser.adapter.AddPhotosAdapter;
import cn.lingox.android.entity.Photo;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ImageHelper;
import cn.lingox.android.helper.ServerHelper;

public class AddPhotosActivity extends ActionBarActivity implements View.OnClickListener {
    // Constants
    public static final int MAX_ADD_PHOTO_COUNT = 9;
    private static final String LOG_TAG = "AddPhotosActivity";
    // RESULT CODES
    private static final int SELECT_PHOTOS = 101;
    // Data elements
    private ArrayList<Photo> photoList = new ArrayList<>();
    private AddPhotosAdapter addPhotosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();

        Intent intent = new Intent(this, PhotoDialog.class);
        intent.putExtra(PhotoDialog.REQUESTED_IMAGE, PhotoDialog.REQUEST_PHOTO);
        startActivityForResult(intent, SELECT_PHOTOS);
    }

    private void initView() {
        setContentView(R.layout.activity_add_photos);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_photos_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.add_pic));

        ImageView addPhotoButton = (ImageView) findViewById(R.id.add_photos_activity_add_photo_button);
        addPhotoButton.setOnClickListener(this);
        TextView confirmButton = (TextView) findViewById(R.id.add_photos_activity_toolbar_confirm_button);
        confirmButton.setOnClickListener(this);
        addPhotosAdapter = new AddPhotosAdapter(this, photoList);
        ListView listView = (ListView) findViewById(R.id.add_photos_activity_photo_list);
        listView.setAdapter(addPhotosAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    //空闲
                    //加载
                    addPhotosAdapter.isScroll(false);
                    addPhotosAdapter.notifyDataSetChanged();
                } else {
                    addPhotosAdapter.isScroll(true);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // TODO Add ability to add photos to this list by taking them using camera
            case R.id.add_photos_activity_add_photo_button:
                Intent intent = new Intent(this, PhotoDialog.class);
                intent.putExtra(PhotoDialog.REQUESTED_IMAGE, PhotoDialog.REQUEST_PHOTO);
                startActivityForResult(intent, SELECT_PHOTOS);
                break;

            case R.id.add_photos_activity_toolbar_confirm_button:
                final ProgressDialog pd = new ProgressDialog(this);
                pd.setCanceledOnTouchOutside(false);
                // pd.setCancelable(false);
                pd.show();
                pd.setMessage(getString(R.string.uploading_image_1));
                // TODO AsyncTask
                new Thread() {
                    public void run() {
                        int imageCount = 1;
                        for (Photo photos : photoList) {
                            try {
                                ServerHelper.getInstance().uploadPhoto(
                                        CacheHelper.getInstance().getSelfInfo().getId(),
                                        photos.getDescription(),
                                        ImageHelper.getInstance().resizeBitmap(photos.getUrl()));
                                imageCount++;
                                final String message = getString(R.string.uploading_image) + imageCount;
                                AddPhotosActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pd.setMessage(message);
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(LOG_TAG, e.toString());
                            }
                        }
                        AddPhotosActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.dismiss();
                            }
                        });
                        finish();
                    }
                }.start();
                break;
        }
    }

    private void update() {
        addPhotosAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    protected void onRestart() {
        update();
        super.onRestart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_PHOTOS:
                if (resultCode == PhotoDialog.RESULT_OK) {
                    if (data.hasExtra(PhotoDialog.SELECTED_IMAGE_LIST)) {
                        ArrayList<String> imageURLs = data.getStringArrayListExtra(PhotoDialog.SELECTED_IMAGE_LIST);
                        for (int i = 0; i < imageURLs.size(); i++) {
                            photoList.add(new Photo("", "", imageURLs.get(i)));
                        }
                    } else if (data.hasExtra(PhotoDialog.SELECTED_SINGLE_IMAGE)) {
                        Uri uri = data.getParcelableExtra(PhotoDialog.SELECTED_SINGLE_IMAGE);
                        photoList.add(new Photo("", "", uri.getPath()));
                    } else {
                        Log.e(LOG_TAG, "onActivityResult(): SELECT_PHOTOS: Data did not have the expected extra");
                    }
                    update();
                } else {
                    Log.e(LOG_TAG, "onActivityResult(): SELECT_PHOTOS: not RESULT_OK");
                }
                break;
        }
    }
}
