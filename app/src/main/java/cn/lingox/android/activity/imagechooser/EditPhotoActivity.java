package cn.lingox.android.activity.imagechooser;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import cn.lingox.android.R;
import cn.lingox.android.entity.Photo;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.PostPhoto;
import cn.lingox.android.utils.FileUtil;
import cn.lingox.android.utils.ImageCache;

public class EditPhotoActivity extends ActionBarActivity implements View.OnClickListener {

    private ImageView showPhoto;
    private EditText editText;
    private Button post;
    private Photo photo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photos);

        if (getIntent().hasExtra("photo")) {
            photo = getIntent().getParcelableExtra("photo");
        }
        initView();
        setData();
    }

    private void initView() {
        showPhoto = (ImageView) findViewById(R.id.show_photo);
        editText = (EditText) findViewById(R.id.photo_describe);
        post = (Button) findViewById(R.id.photo_post);
        post.setOnClickListener(this);
        Button cancel = (Button) findViewById(R.id.edit_photo_cancel);
        cancel.setOnClickListener(this);
    }

    private void setData() {
        Bitmap bitmap;
        if (!photo.getDescription().isEmpty()) {
            editText.setText(photo.getDescription());
        }
        String url = photo.getUrl();
        if (ImageCache.getInstance().get(url) != null) {
            bitmap = ImageCache.getInstance().get(photo.getUrl());
            showPhoto.setImageBitmap(bitmap);
        }
        if (FileUtil.getImg(url, this) != null) {
            bitmap = FileUtil.getImg(photo.getUrl(), this);
            showPhoto.setImageBitmap(bitmap);
        } else {
            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, showPhoto, url, "circular");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_photo_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.photo_post:
                new PostPhoto(this, post, photo, editText.getText().toString().trim()).execute();
                break;
        }
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
}
