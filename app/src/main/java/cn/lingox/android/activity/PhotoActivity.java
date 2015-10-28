package cn.lingox.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.entity.Photo;
import cn.lingox.android.helper.ServerHelper;

/**
 * Created by wuyou on 2015/1/11.
 */
public class PhotoActivity extends Activity implements View.OnClickListener {
    public static final String SELECTED_PHOTO = "selectphoto";
    public static final String LOCAL_PHOTO = "selectphotourl";
    public static final String IS_MYPHOTO = "ismyphoto";
    public static final String DELETED_PHOTO = "deletedphotoid";
    public static final String EDITED_PHOTO = "editedphoto";
    public static final int RESULT_PHOTO_DELETED = 201;
    public static final int RESULT_PHOTO_EDITED = 202;
    private ArrayList<View> listViews = null;
    private ImageView iv_photo;
    private Button photo_bt_exit;
    private Button photo_bt_del;
    private Button photo_bt_enter;
    private Photo select_photo;
    private EditText et_description;

    private boolean editingMyPhoto = false;

    private RelativeLayout photo_relativeLayout;
    private Intent intent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        intent = getIntent();
        select_photo = intent.getParcelableExtra(PhotoActivity.SELECTED_PHOTO);
        initView();
        if (intent.hasExtra(PhotoActivity.IS_MYPHOTO)) {
            editingMyPhoto = true;
        }
    }

    private void initView() {
        iv_photo = (ImageView) findViewById(R.id.iv_photo);
        photo_relativeLayout = (RelativeLayout) findViewById(R.id.photo_relativeLayout);
        iv_photo.setBackgroundColor(0xff000000);
        photo_relativeLayout.setBackgroundColor(0xff000000);
        photo_bt_exit = (Button) findViewById(R.id.photo_bt_exit);
        photo_bt_exit.setOnClickListener(PhotoActivity.this);
        photo_bt_del = (Button) findViewById(R.id.photo_bt_del);
        photo_bt_del.setOnClickListener(PhotoActivity.this);
        photo_bt_enter = (Button) findViewById(R.id.photo_bt_enter);
        photo_bt_enter.setOnClickListener(this);
        et_description = (EditText) findViewById(R.id.et_description);
        et_description.setText(select_photo.getDescription());
        et_description.setBackgroundColor(0xff000000);
        if (intent.hasExtra(PhotoActivity.LOCAL_PHOTO)) {
            Picasso.with(this).load("file://" + select_photo.getUrl()).into(iv_photo);
        } else {
            Picasso.with(this).load(select_photo.getUrl()).into(iv_photo);
        }
    }

    @Override
    public void onClick(View v) {
        final Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.photo_bt_exit:
                setResult(RESULT_OK);
                PhotoActivity.this.finish();
                break;
            case R.id.photo_bt_del:
                if (editingMyPhoto) {
                    final ProgressDialog pd = new ProgressDialog(PhotoActivity.this);
                    pd.setCanceledOnTouchOutside(false);
                    pd.setCancelable(false);
                    pd.setMessage("Deleting Photo...");
                    new Thread() {
                        public void run() {
                            try {
                                Photo deletedPhoto = ServerHelper.getInstance()
                                        .deletePhoto(select_photo.getId());
                                PhotoActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pd.dismiss();
                                        Toast.makeText(PhotoActivity.this, "Photo Deleted!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                intent.putExtra(DELETED_PHOTO, deletedPhoto);
                                setResult(RESULT_PHOTO_DELETED, intent);
                                finish();

                            } catch (Exception e) {
                                Log.e("PhotoActivity",
                                        "Exception caught: " + e.getMessage());
                                if (PhotoActivity.this == null)
                                    return;
                                PhotoActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pd.dismiss();
                                        Toast.makeText(PhotoActivity.this, "Error Deleting Photo!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                setResult(RESULT_CANCELED);
                                finish();
                            }

                        }
                    }.start();
                } else {
                    intent.putExtra(DELETED_PHOTO, select_photo);
                    setResult(RESULT_PHOTO_DELETED, intent);
                    finish();
                }

                break;
            case R.id.photo_bt_enter:
                select_photo.setDescription(et_description.getText().toString());
                if (editingMyPhoto) {
                    final ProgressDialog pd = new ProgressDialog(PhotoActivity.this);
                    pd.setCanceledOnTouchOutside(false);
                    pd.setCancelable(false);
                    pd.setMessage("Updating Photo...");
                    new Thread() {
                        public void run() {
                            try {
                                Photo editedPhoto = ServerHelper.getInstance()
                                        .editPhoto(select_photo);
                                PhotoActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pd.dismiss();
                                        Toast.makeText(PhotoActivity.this, "Photo Updated!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                intent.putExtra(EDITED_PHOTO, editedPhoto);
                                setResult(RESULT_PHOTO_EDITED, intent);
                                finish();

                            } catch (Exception e) {
                                Log.e("PhotoActivity",
                                        "Exception caught: " + e.getMessage());
                                if (PhotoActivity.this == null)
                                    return;
                                PhotoActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pd.dismiss();
                                        Toast.makeText(PhotoActivity.this, "Error Updating Photo!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                setResult(RESULT_CANCELED);
                                finish();
                            }
                        }
                    }.start();
                } else {
                    intent.putExtra(EDITED_PHOTO, select_photo);
                    setResult(RESULT_PHOTO_EDITED, intent);
                    finish();
                }
                break;
        }
    }
}