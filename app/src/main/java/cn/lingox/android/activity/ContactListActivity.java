package cn.lingox.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.adapter.ContactAdapter;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.task.GetContactList;

public class ContactListActivity extends ActionBarActivity implements OnClickListener {
    private static final String LOG_TAG = "ContactListActivity";

    // UI Elements
    private ContactAdapter contactAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        initView();
    }

    private void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.contact_list_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        ListView listView = (ListView) findViewById(R.id.list);
        contactAdapter = new ContactAdapter(ContactListActivity.this, R.layout.row_contact, CacheHelper.getInstance().getContactList(), null);
        listView.setAdapter(contactAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onListItemClick(position);
            }
        });
        listView.setFastScrollEnabled(true);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.contact_list_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ContactAdapter getContactAdapter() {
        return contactAdapter;
    }

    public void onListItemClick(int position) {
        if (position != 0) {
            Intent intent = new Intent(ContactListActivity.this,
                    UserInfoActivity.class);
            intent.putExtra(UserInfoActivity.INTENT_USER_ID, contactAdapter.getItem(position).getId());
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_back:
                ContactListActivity.this.finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        super.onResume();
        if (CacheHelper.getInstance().getContactList().isEmpty()) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            refreshList();
        }
    }

    private void refreshList() {
        new GetContactList(new GetContactList.Callback() {
            @Override
            public void onSuccess(ArrayList<User> contactList) {
                CacheHelper.getInstance().setContactList(contactList);
                contactAdapter.notifyDataSetChanged();
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFail() {
                Toast.makeText(ContactListActivity.this, "Failed to load your contacts!", Toast.LENGTH_LONG).show();
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).execute();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }
}
