package cn.lingox.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.lingox.android.R;
import cn.lingox.android.adapter.ShowTravelAdapter;
import cn.lingox.android.entity.Travel;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.utils.CircularImageView;
import cn.lingox.android.video.util.AsyncTask;


public class ShowTravelActivity extends ActionBarActivity implements OnClickListener {
    private static final int EDIT_TRAVEL = 105;

    private ListView listView;
    private TextView userName;
    private ImageView back;
    private CircularImageView userAvatar;

    private ArrayList<Travel> datas = new ArrayList<>();
    private ShowTravelAdapter adapter;

    private boolean isSelf = false;

    private User user = null;
    private int delIndex = -1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    delIndex = msg.arg1;
                    new DeleteTravel().execute(delIndex);
                    break;
                case 1:
                    Intent intent = new Intent(ShowTravelActivity.this, AddTravelActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("edit", datas.get(msg.arg1));
                    intent.putExtras(bundle);
                    startActivityForResult(intent, EDIT_TRAVEL);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra("user")) {
            user = getIntent().getParcelableExtra("user");
            isSelf = CacheHelper.getInstance().getSelfInfo().getId().equals(user.getId());
        }
        if (getIntent().hasExtra("TravelList")) {
            datas.clear();
            datas.addAll((List) getIntent().getParcelableArrayListExtra("TravelList"));
        }
        initView();
        initData();
    }

    private void initView() {
        setContentView(R.layout.activity_show_travel);

        back = (ImageView) findViewById(R.id.show_travel_back);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("TravelList", datas);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        userName = (TextView) findViewById(R.id.show_travel_name);
        userAvatar = (CircularImageView) findViewById(R.id.show_travel_avatar);
        listView = (ListView) findViewById(R.id.show_travel_list);

        adapter = new ShowTravelAdapter(this, datas, handler, isSelf);
        listView.setAdapter(adapter);
    }

    private void initData() {
        UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, userAvatar, user.getAvatar());
        userName.setText(user.getNickname());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_travel_back:
                Intent intent = new Intent();
                if (datas.size() > 0) {
                    Collections.reverse(datas);
                }
                intent.putParcelableArrayListExtra("TravelList", datas);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EDIT_TRAVEL:
                if (data.hasExtra("Travel")) {
                    Travel travel;
                    travel = data.getParcelableExtra("Travel");
                    for (int i = 0; i < datas.size(); i++) {
                        if (travel.getId().equals(datas.get(i).getId())) {
                            datas.remove(i);
                            datas.add(i, travel);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra("TravelList", datas);
            setResult(RESULT_OK, intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private class DeleteTravel extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                ServerHelper.getInstance().deleteExperiences(datas.get(params[0]).getId());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("showTravel", e.getMessage());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                if (delIndex != -1) {
                    datas.remove(delIndex);
                    adapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(ShowTravelActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}