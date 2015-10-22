package cn.lingox.android.activity.imagechooser;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import cn.lingox.android.R;
import cn.lingox.android.activity.imagechooser.adapter.ImageGridAdapter;
import cn.lingox.android.activity.imagechooser.entity.ImageItem;
import cn.lingox.android.activity.imagechooser.helper.AlbumHelper;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.utils.FileUtil;
//import cn.lingox.android.utils.FileUtils;

public class AlbumActivity extends ActionBarActivity {
    // INCOMING INTENT EXTRAS
    public static final String ALBUM_IMAGE_LIST = LingoXApplication.PACKAGE_NAME + ".ALBUM_IMAGE_LIST";
    private static final String LOG_TAG = "AlbumActivity";
    private List<ImageItem> dataList;
    private ImageGridAdapter adapter;
    private AlbumHelper helper;
    private boolean selectMultiple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());

        dataList = (List<ImageItem>) getIntent().getSerializableExtra(ALBUM_IMAGE_LIST);
        selectMultiple = getIntent().getBooleanExtra(AlbumListActivity.SELECT_MULTIPLE, false);

        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_album);

        Toolbar toolbar = (Toolbar) findViewById(R.id.album_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.choose_photo));

        GridView gridView = (GridView) findViewById(R.id.album_activity_grid_view);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        adapter = new ImageGridAdapter(AlbumActivity.this, dataList, selectMultiple, new ImageGridAdapter.SelectionListener() {
            @Override
            public void onSingleItemSelected(String path) {
                Intent intent = new Intent();
                intent.putExtra(PhotoDialog.SELECTED_SINGLE_IMAGE, path);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onMultiItemSelected() {

            }
        });
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                adapter.notifyDataSetChanged();
            }

        });

        TextView ok = (TextView) findViewById(R.id.album_activity_toolbar_confirm_button);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (selectMultiple) {
                    ArrayList<String> list = new ArrayList<>(adapter.map.values());
                    ArrayList<String> imageURLs = new ArrayList<>();
                    for (int i = 0; i < list.size(); i++) {
                        String Str = list.get(i).substring(
                                list.get(i).lastIndexOf("/") + 1,
                                list.get(i).lastIndexOf("."));
                        imageURLs.add(FileUtil.SDPATH + Str + ".JPEG");
                        Log.d(LOG_TAG, imageURLs.toString());
                    }
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra(PhotoDialog.SELECTED_IMAGE_LIST, list);
                    intent.putExtra(PhotoDialog.SELECTED_SINGLE_IMAGE, list.get(0));
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Log.e(LOG_TAG, "This button should not be clickable in single image selection mode");
                }
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
