package cn.lingox.android.helper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import cn.lingox.android.R;
import cn.lingox.android.activity.imagechooser.AddPhotosActivity;

public class CancelDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {
        setContentView(R.layout.row_photo_cancel);

        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button leave = (Button) findViewById(R.id.leave);
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CancelDialog.this, AddPhotosActivity.class);
                intent.putExtra("close", "close");
                startActivity(intent);
            }
        });

        // TODO Check if this is still required now that the layout is an activity instead of a dialog
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = dm.widthPixels;
        dialogWindow.setAttributes(lp);
    }
}
