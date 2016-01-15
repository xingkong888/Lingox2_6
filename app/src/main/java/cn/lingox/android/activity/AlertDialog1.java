package cn.lingox.android.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.util.ImageUtils;

import java.io.File;

import cn.lingox.android.R;
import cn.lingox.android.task.DownloadImageTask;
import cn.lingox.android.utils.ImageCache;

//自定义dialog
public class AlertDialog1 extends BaseActivity {
    private int position;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_alert);
        TextView mTextView = (TextView) findViewById(R.id.title);
        Button mButton = (Button) findViewById(R.id.btn_cancel);
        ImageView imageView = (ImageView) findViewById(R.id.image);
        editText = (EditText) findViewById(R.id.edit);
        String msg = getIntent().getStringExtra("msg");
        String title = getIntent().getStringExtra("title");
        position = getIntent().getIntExtra("position", -1);
        boolean isCanceTitle = getIntent().getBooleanExtra("titleIsCancel", false);
        boolean isCanceShow = getIntent().getBooleanExtra("cancel", false);
        boolean isEditextShow = getIntent().getBooleanExtra("editTextShow", false);
        String path = getIntent().getStringExtra("forwardImage");
        if (msg != null)
            ((TextView) findViewById(R.id.alert_message)).setText(msg);
        if (title != null)
            mTextView.setText(title);
        if (isCanceTitle) {
            mTextView.setVisibility(View.GONE);
        }
        if (isCanceShow)
            mButton.setVisibility(View.VISIBLE);
        if (path != null) {
            if (!new File(path).exists()) {
                path = DownloadImageTask.getThumbnailImagePath(path);
            }
            imageView.setVisibility(View.VISIBLE);
            findViewById(R.id.alert_message).setVisibility(View.GONE);
            if (ImageCache.getInstance().get(path) != null) {
                imageView.setImageBitmap(ImageCache.getInstance().get(path));
            } else {
                Bitmap bm = ImageUtils.decodeScaleImage(path, 150, 150);
                imageView.setImageBitmap(bm);
                ImageCache.getInstance().put(path, bm);
            }
        }
        if (isEditextShow) {
            editText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 点击事件
     *
     * @param view 被点击的控件
     */
    public void ok(View view) {
        setResult(RESULT_OK, new Intent().putExtra("position", position).putExtra("edittext", editText.getText().toString()));
        if (position != -1) {
            ChatActivity.resendPos = position;
        }
        finish();
    }

    /**
     * 点击事件
     *
     * @param view 被点击的控件
     */
    public void cancel(View view) {
        finish();
    }

    /**
     * 触摸事件
     *
     * @param event 事件
     * @return boolean
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }
}