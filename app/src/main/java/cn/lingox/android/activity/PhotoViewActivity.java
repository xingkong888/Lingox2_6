package cn.lingox.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.activity.imagechooser.EditPhotoActivity;
import cn.lingox.android.adapter.UserPhotoPagerAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Photo;
import cn.lingox.android.helper.ServerHelper;

public class PhotoViewActivity extends Activity implements View.OnClickListener {
    // Incoming Intent Extras
    public static final String PHOTO_LIST = LingoXApplication.PACKAGE_NAME + "PHOTO_LIST";
    public static final String PHOTO_POSITION = LingoXApplication.PACKAGE_NAME + "PHOTO_POSITION";
    public static final String OTHERS_PHOTOS = LingoXApplication.PACKAGE_NAME + "OTHERS_PHOTOS";
    // Result Codes
    public static final int EDIT_PICTURES = 202;
    private UserPhotoPagerAdapter adapter;
    private ArrayList<Photo> photoList = new ArrayList<>();
    private int current_page;
    private TextView description;
    private boolean othersPhotos;

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        public void onPageSelected(int position) {
            current_page = position;
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
            description.setText(photoList.get(current_page).getDescription());
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (!intent.hasExtra(OTHERS_PHOTOS) || !intent.hasExtra(PHOTO_LIST) || !intent.hasExtra(PHOTO_POSITION)) {
            Toast.makeText(this, "Error: required intent extra wasn't passed", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }
        othersPhotos = intent.getBooleanExtra(OTHERS_PHOTOS, true);
        photoList.clear();
        photoList.addAll(intent.<Photo>getParcelableArrayListExtra(PHOTO_LIST));
        current_page = intent.getIntExtra(PHOTO_POSITION, 0);
        initView();
    }

    private void initView() {

        setContentView(R.layout.activity_view_photo);
        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);

        RelativeLayout photo_relativeLayout = (RelativeLayout) findViewById(R.id.photo_relativeLayout);
        photo_relativeLayout.setBackgroundColor(Color.BLACK);

        adapter = new UserPhotoPagerAdapter(this, photoList);
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(pageChangeListener);

        pager.setCurrentItem(current_page);
        ImageView photo_back = (ImageView) findViewById(R.id.photo_bt_back);
        ImageView photo_edit = (ImageView) findViewById(R.id.photo_bt_edit);
        ImageView photo_del = (ImageView) findViewById(R.id.photo_bt_delete);
        photo_back.setOnClickListener(this);
        photo_edit.setOnClickListener(this);
        photo_del.setOnClickListener(this);
        description = (TextView) findViewById(R.id.tv_description);
        description.setText(photoList.get(current_page).getDescription());
        if (othersPhotos) {
            photo_edit.setVisibility(View.GONE);
            photo_del.setVisibility(View.GONE);
        } else {
            photo_edit.setVisibility(View.VISIBLE);
            photo_del.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo_bt_back:
                Intent intent1 = new Intent();
                intent1.putParcelableArrayListExtra(PHOTO_LIST, photoList);
                setResult(RESULT_OK, intent1);
                finish();
                break;
            case R.id.photo_bt_edit:
                Intent intent = new Intent(this,
                        EditPhotoActivity.class);
                intent.putExtra("photo", photoList.get(current_page));
                startActivityForResult(intent, EDIT_PICTURES);
                break;
            case R.id.photo_bt_delete:
                new AlertDialog.Builder(this)
                        .setMessage("Sure to delete?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                new DeletePhoto().execute();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .create().show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case EDIT_PICTURES:
                switch (resultCode) {
                    case PhotoActivity.RESULT_PHOTO_EDITED:
                        int editIndex = findPhotoInList((Photo) intent.getParcelableExtra(PhotoActivity.EDITED_PHOTO));
                        photoList.set(editIndex, (Photo) intent.getParcelableExtra(PhotoActivity.EDITED_PHOTO));
                        adapter.notifyDataSetChanged();
                        break;
                }
                break;
        }
    }

    // Helper methods
    private int findPhotoInList(Photo photo) {
        for (Photo photos : photoList) {
            if (photos.getId().equals(photo.getId())) {
                return photoList.indexOf(photos);
            }
        }
        return -1;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent1 = new Intent();
            intent1.putParcelableArrayListExtra(PHOTO_LIST, photoList);
            setResult(RESULT_OK, intent1);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    private class DeletePhoto extends AsyncTask<Void, String, Boolean> {
        final ProgressDialog pd = new ProgressDialog(PhotoViewActivity.this);
        Photo deletedPhoto;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
            pd.setMessage("Deleting Photo...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                deletedPhoto = ServerHelper.getInstance()
                        .deletePhoto(photoList.get(current_page).getId());
                return true;
            } catch (Exception e) {
                Log.e("PhotoActivity",
                        "Exception caught: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (success) {
                int deleteIndex = findPhotoInList(deletedPhoto);
                photoList.remove(deleteIndex);
                if (photoList.isEmpty()) {
                    Intent intent1 = new Intent();
                    intent1.putParcelableArrayListExtra(PHOTO_LIST, photoList);
                    setResult(RESULT_OK, intent1);
                    finish();
                }
                Toast.makeText(PhotoViewActivity.this, "Photo Deleted!", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(PhotoViewActivity.this, "Error Deleting Photo!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
