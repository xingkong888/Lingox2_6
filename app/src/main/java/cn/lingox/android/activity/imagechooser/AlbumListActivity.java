package cn.lingox.android.activity.imagechooser;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.lingox.android.R;
import cn.lingox.android.activity.imagechooser.adapter.ImageBucketAdapter;
import cn.lingox.android.activity.imagechooser.entity.ImageBucket;
import cn.lingox.android.activity.imagechooser.helper.AlbumHelper;
import cn.lingox.android.app.LingoXApplication;

/**
 * 从本地读取图片文件夹
 */
public class AlbumListActivity extends ActionBarActivity {
    // INCOMING INTENT EXTRAS
    public static final String SELECT_MULTIPLE = LingoXApplication.PACKAGE_NAME + ".SELECT_MULTIPLE";
    // OUTGOING REQUEST CODES
    public static final int SELECT_PHOTOS = 101;
    public static Bitmap bitmap;
    public List<ImageBucket> dataList = new ArrayList<>();
    private AlbumHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());
        initData();
    }

    /**
     * 实例化控件
     */
    private void initView() {
        setContentView(R.layout.activity_album_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.album_list_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.choose_photo));

        GridView gridView = (GridView) findViewById(R.id.album_list_activity_grid_view);
        ImageBucketAdapter adapter = new ImageBucketAdapter(AlbumListActivity.this, dataList);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(AlbumListActivity.this,
                        AlbumActivity.class);
                intent.putExtra(AlbumActivity.ALBUM_IMAGE_LIST,
                        (Serializable) dataList.get(position).imageList);
                intent.putExtra(SELECT_MULTIPLE,
                        getIntent().getBooleanExtra(SELECT_MULTIPLE, false));
                startActivityForResult(intent, SELECT_PHOTOS);
            }
        });
    }

    private void initData() {
        dataList.clear();
        //获取本地的图片的uri
        dataList.addAll(helper.getImagesBucketList(true));
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_addpic_unfocused);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_PHOTOS:
                if (resultCode == AlbumActivity.RESULT_OK) {
                    if (data.hasExtra(PhotoDialog.SELECTED_IMAGE_LIST)) {
                        setResult(RESULT_OK, data);
                        finish();
                    } else if (data.hasExtra(PhotoDialog.SELECTED_SINGLE_IMAGE)) {
                        setResult(RESULT_OK, data);
                        finish();
                    }
                }
                break;
        }
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
}
