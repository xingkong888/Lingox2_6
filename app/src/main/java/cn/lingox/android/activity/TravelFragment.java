package cn.lingox.android.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.adapter.TravelAdapter;

/**
 * 展示travel数据
 */
public class TravelFragment extends Fragment implements View.OnClickListener {
    private static TravelFragment fragment;
    private ImageView anim;
    private AnimationDrawable animationDrawable;
    private PullToRefreshListView listView;
    private ImageView add;
    private int clickPosition = 0;
    private TravelAdapter adapter;

    public static synchronized TravelFragment newInstance() {
        if (fragment == null) {
            fragment = new TravelFragment();
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travel, null);

        anim = (ImageView) view.findViewById(R.id.anim);
        animationDrawable = (AnimationDrawable) anim.getBackground();

        add = (ImageView) view.findViewById(R.id.iv_add_travel);
        add.setOnClickListener(this);

        listView = (PullToRefreshListView) view.findViewById(R.id.travel_listview);
//        adapter=new TravelAdapter(getActivity(),null);
//        listView.setAdapter(adapter);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setRefreshing(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = new HashMap<>();
//                map.put("discover", pathList.get(position - 1).getTitle());
                MobclickAgent.onEvent(getActivity(), "click_discover", map);

                clickPosition = position - 1;

                Intent intent = new Intent(getActivity(), LocalViewActivity.class);
//                intent.putExtra(LocalViewActivity.PATH_TO_VIEW, pathList.get(position - 1));
                startActivityForResult(intent, LocalFragment.EDIT_PATH);
            }
        });
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //下拉刷新
//                page = 1;
//                pathList.clear();
//                adapter.notifyDataSetChanged();
//                refreshList();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //上拉加载更多
//                page++;
//                refreshList();
            }
        });
        return view;
    }

    private void startAnim() {
        if (!animationDrawable.isRunning()) {
            anim.setVisibility(View.VISIBLE);
            animationDrawable.start();
        }
    }

    private void stopAnim() {
        if (animationDrawable.isRunning()) {
            anim.setVisibility(View.GONE);
            animationDrawable.stop();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add_travel:
                //添加新的 TravelEditActivity
                Intent intent = new Intent(getActivity(), TravelEditActivity.class);
                startActivity(intent);
                break;
        }
    }
}