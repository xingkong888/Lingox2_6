package cn.lingox.android.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cn.lingox.android.R;
import cn.lingox.android.adapter.FollowersAdapter;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;

public class ContactsFragment extends Fragment {
    private static final String LOG_TAG = "ContactsFragment";

    private ListView listView;
    private ArrayList<User> datas;
    private FollowersAdapter adapter;
    private ImageView anim;
    private AnimationDrawable animationDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followers, container, false);

        anim = (ImageView) view.findViewById(R.id.anim);
        animationDrawable = (AnimationDrawable) anim.getBackground();

        listView = (ListView) view.findViewById(R.id.followers_list);
        datas = new ArrayList<>();
        new LoadUserFollowing().execute();

        adapter = new FollowersAdapter(getActivity(), R.layout.row_contact, datas);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onListItemClick(position);
            }
        });
        listView.setFastScrollEnabled(true);
        return view;
    }

    public void onListItemClick(int position) {
        Intent intent = new Intent(getActivity(),
                UserInfoActivity.class);
        intent.putExtra(UserInfoActivity.INTENT_USER_ID, adapter.getItem(position).getId());
        startActivity(intent);
    }

    private void startAnim() {
        if (!animationDrawable.isRunning()) {
            anim.setVisibility(View.VISIBLE);
            animationDrawable.start();
        }
    }

    private void stopAnim() {
        if (animationDrawable.isRunning()) {
            anim.setVisibility(View.INVISIBLE);
            animationDrawable.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        MobclickAgent.onPageStart("ContactsFragment");
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("ContactsFragment");
    }

    private class LoadUserFollowing extends AsyncTask<Void, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            datas.clear();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                datas.addAll(ServerHelper.getInstance().getContactList(CacheHelper.getInstance().getSelfInfo().getId()));
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception caught: " + e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                if (datas.size() > 0) {
                    stopAnim();
                    Collections.sort(datas, new Comparator<User>() {
                        @Override
                        public int compare(User lhs, User rhs) {
                            return lhs.getHeader().compareTo(rhs.getHeader());
                        }
                    });
                } else {
                    startAnim();
                }
                adapter.notifyDataSetChanged();
            } else {
                startAnim();
                Toast.makeText(getActivity(), "Failed to get User's Contacts", Toast.LENGTH_LONG).show();
            }
        }
    }
}