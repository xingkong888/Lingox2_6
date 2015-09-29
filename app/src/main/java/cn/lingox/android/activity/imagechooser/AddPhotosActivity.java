package cn.lingox.android.activity.imagechooser;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.activity.PhotoActivity;
import cn.lingox.android.activity.select_area.SelectCountry;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Photo;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ImageHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.video.util.AsyncTask;
import cn.lingox.android.widget.PhotoTagsSelectDialog;

public class AddPhotosActivity extends ActionBarActivity implements View.OnClickListener {
    // Constants
    public static final int MAX_ADD_PHOTO_COUNT = 9;
    // RESULT CODES
    public static final int SELECT_PHOTOS = 101;
    private static final String LOG_TAG = "AddPhotosActivity";

    private static final int SELECTLOCATION = 125;

    /**
     * 标签之间的间距 px
     */
    final int itemMargins = 50;
    /**
     * 标签的行间距 px
     */
    final int lineMargins = 50;
    //TODO 设置标签的方法
    int length;
    private ImageView photo;
    private EditText edit;
    private TextView location;
    private Button post, cancel;
    private LinearLayout photoLocation, photoTags;
    //    private Country country = null;
//    private City city;
    private RelativeLayout noTags;
    private Photo photo1 = new Photo();
    private ArrayList<String> tagsList;
    private ViewGroup tagsView;
    private String postTags = new String();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                addTagView(((Photo) msg.obj).getTags());
                postTags = ((Photo) msg.obj).getTags();
            } else if (msg.what == 0) {
                noTags.setVisibility(View.VISIBLE);
                postTags = "";
                tagsView.removeAllViews();
                tagsView.setVisibility(View.GONE);
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        if (getIntent().hasExtra("photo")) {
            photo1 = getIntent().getParcelableExtra("photo");
            initDate();
        } else {
            Intent intent = new Intent(this, PhotoDialog.class);
            intent.putExtra(PhotoDialog.REQUESTED_IMAGE, PhotoDialog.REQUEST_PHOTO);
            startActivityForResult(intent, SELECT_PHOTOS);
        }
    }

    private void initDate() {
        if (!photo1.getDescription().isEmpty()) {
            edit.setText(photo1.getDescription());
        }
        String[] strs = photo1.getTags().split(",");
        if (strs.length > 0) {
            if (!strs[0].isEmpty()) {
                noTags.setVisibility(View.GONE);
                tagsView.setVisibility(View.VISIBLE);
                addTagView(photo1.getTags());
            } else {
                noTags.setVisibility(View.VISIBLE);
                tagsView.setVisibility(View.GONE);
            }
        }
        if (!photo1.getLocation().isEmpty()) {
            location.setText(photo1.getLocation());
        }
        if (!photo1.getUrl().isEmpty()) {
            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, photo, photo1.getUrl());
        }
    }

    private void initView() {
        tagsList = new ArrayList<>();
        tagsList.addAll(JsonHelper.getInstance().getAllTags());
        setContentView(R.layout.activity_add_photos_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.add_photos_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        if (getIntent().hasExtra("photo")) {
            getSupportActionBar().setTitle("Edit Picture");
        } else {
            getSupportActionBar().setTitle(getString(R.string.add_pic));
        }

        tagsView = (ViewGroup) findViewById(R.id.photo_tag);
        photo = (ImageView) findViewById(R.id.add_photo);
        edit = (EditText) findViewById(R.id.photo_describe);
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        location = (TextView) findViewById(R.id.photo_address);

        post = (Button) findViewById(R.id.photo_post);
        post.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.add_photo_cancel);
        cancel.setOnClickListener(this);

        photoLocation = (LinearLayout) findViewById(R.id.photo_location);
        photoLocation.setOnClickListener(this);
        photoTags = (LinearLayout) findViewById(R.id.photo_tags);
        photoTags.setOnClickListener(this);

        noTags = (RelativeLayout) findViewById(R.id.photo_no_tag);
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
            case R.id.add_photo_cancel://取消相片编辑
                new AlertDialog.Builder(this)
                        .setMessage("Whether to cancel editing？")
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
                break;
            case R.id.photo_location:
                Intent intent = new Intent(this, SelectCountry.class);
//                intent.putExtra();
                startActivityForResult(intent, SELECTLOCATION);
                break;
            case R.id.photo_tags:
            case R.id.photo_tag:
                PhotoTagsSelectDialog.newInstance("photo", this, photo1, handler).show(getSupportFragmentManager(), "photo");
                break;
            case R.id.photo_post:
                if (photo1.getLocation() != null) {
                    new PostPhoto().execute();
                } else {
                    new AlertDialog.Builder(this).setMessage("Please select a location")
                            .create().show();
                }
                break;
        }
    }


    public void addTagView(String tags) {
        String[] strTags = tags.replace("[", "").replace("]", "").split(",");
        if (strTags.length > 0) {
            noTags.setVisibility(View.GONE);
            tagsView.setVisibility(View.VISIBLE);
        } else {
            noTags.setVisibility(View.VISIBLE);
            tagsView.setVisibility(View.GONE);
        }
        tagsView.removeAllViews();
        final int containerWidth = LingoXApplication.getInstance().getWidth();
        final LayoutInflater inflater = getLayoutInflater();
        /** 用来测量字符的宽度 */
        final Paint paint = new Paint();
        TextView textView = (TextView) inflater.inflate(R.layout.row_tag_include, null);
        int itemPadding = textView.getCompoundPaddingLeft() + textView.getCompoundPaddingRight();
        final LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(0, 0, itemMargins, 0);

        paint.setTextSize(textView.getTextSize());

        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        tagsView.addView(layout);

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, lineMargins, 0, 0);
        /** 一行剩下的空间 **/
        int remainWidth = containerWidth;
        // 表示数组长度
        length = strTags.length;
        String text;
        float itemWidth;
        for (int i = 0; i < length; ++i) {
            text = tagsList.get(Integer.valueOf(strTags[i].trim()));
            itemWidth = paint.measureText(text) + itemPadding;
            if (remainWidth > itemWidth) {
                addItemView(inflater, layout, tvParams, text);
            } else {
                resetTextViewMarginsRight(layout);
                layout = new LinearLayout(this);
                layout.setLayoutParams(params);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                /** 将前面那一个textview加入新的一行 */
                addItemView(inflater, layout, tvParams, text);
                tagsView.addView(layout);
                remainWidth = containerWidth;
            }
            remainWidth = (int) (remainWidth - itemWidth + 0.5f) - itemMargins;
        }
        if (length > 0) {
            resetTextViewMarginsRight(layout);
        }
    }

    /*****************
     * 将每行最后一个textview的MarginsRight去掉
     *********************************/
    private void resetTextViewMarginsRight(ViewGroup viewGroup) {
        final TextView tempTextView = (TextView) viewGroup.getChildAt(viewGroup.getChildCount() - 1);
        tempTextView
                .setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addItemView(LayoutInflater inflater, ViewGroup viewGroup, ViewGroup.LayoutParams params, final String text) {
        final TextView tvItem = (TextView) inflater.inflate(R.layout.row_tag_include, null);
        tvItem.setText(text);
        viewGroup.addView(tvItem, params);
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
        super.onRestart();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECTLOCATION:
                String str = data.getStringExtra(SelectCountry.SELECTED);
                if (!str.isEmpty()) {
                    photo1.setLocation(str);
                    location.setText(str);
                }
                break;
            case SELECT_PHOTOS:
                if (resultCode == PhotoDialog.RESULT_OK) {
                    if (data.hasExtra(PhotoDialog.SELECTED_IMAGE_LIST)) {
                        ArrayList<String> imageURLs = data.getStringArrayListExtra(PhotoDialog.SELECTED_IMAGE_LIST);
                        photo1.setUrl(imageURLs.get(0));
                    } else if (data.hasExtra(PhotoDialog.SELECTED_SINGLE_IMAGE)) {
                        Uri uri = data.getParcelableExtra(PhotoDialog.SELECTED_SINGLE_IMAGE);
                        photo1.setDescription(edit.getText().toString());
                        photo1.setUrl(uri.getPath());
                    } else {
                        Log.e(LOG_TAG, "onActivityResult(): SELECT_PHOTOS: Data did not have the expected extra");
                    }
                    //展示图片
                    Picasso.with(this).load("file://" + photo1.getUrl()).into(photo);
                } else if (resultCode == 111) {
                    finish();
                } else {
                    Log.e(LOG_TAG, "onActivityResult(): SELECT_PHOTOS: not RESULT_OK");
                }
                break;
        }
    }

    private class PostPhoto extends AsyncTask<Void, Void, Boolean> {
        final ProgressDialog pd = new ProgressDialog(AddPhotosActivity.this);
        boolean isSuccess = false;
        private Photo photo;

        @Override
        protected void onPreExecute() {
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            pd.setMessage("Uploading Image");
            post.setClickable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (getIntent().hasExtra("photo")) {
                    photo = ServerHelper.getInstance().editPhoto(photo1);
                } else {
                    if (location.getText().toString().isEmpty()) {
                        ServerHelper.getInstance().uploadPhoto(
                                CacheHelper.getInstance().getSelfInfo().getId(),
                                photo1.getDescription(),
                                ImageHelper.getInstance().resizeBitmap(photo1.getUrl()),
                                photo1.getCountry(), "", "", postTags);
                    } else if (!photo1.getProvince().isEmpty()) {
                        ServerHelper.getInstance().uploadPhoto(
                                CacheHelper.getInstance().getSelfInfo().getId(),
                                photo1.getDescription(),
                                ImageHelper.getInstance().resizeBitmap(photo1.getUrl()),
                                photo1.getCountry(), photo1.getProvince(), "", postTags
                        );
                    } else {
                        ServerHelper.getInstance().uploadPhoto(
                                CacheHelper.getInstance().getSelfInfo().getId(),
                                photo1.getDescription(),
                                ImageHelper.getInstance().resizeBitmap(photo1.getUrl()),
                                photo1.getCountry(), photo1.getProvince(), photo1.getCity(), postTags
                        );
                    }
                }
                isSuccess = true;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
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
                Toast.makeText(AddPhotosActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra(PhotoActivity.EDITED_PHOTO, photo);
                setResult(PhotoActivity.RESULT_PHOTO_EDITED, intent);
                finish();
            } else {
                Toast.makeText(AddPhotosActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}