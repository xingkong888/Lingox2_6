package cn.lingox.android.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.adapter.PathAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.utils.SkipDialog;

/**
 * 活动展示
 */
public class PathFragment extends Fragment implements OnClickListener {
    // Request Codes
    public static final int ADD_PATH = 101;
    public static final int EDIT_PATH = 102;
    static final String LOG_TAG = "PathFragment";
    // Bundle Args
    static final String USER = "USER";
    // Data Elements
    private ArrayList<Path> pathList = new ArrayList<>();
    private String userId;
    // UI Elements
    private ImageView addPathButton;

    private PullToRefreshListView listView;
    private PathAdapter adapter;

    private int page = 1;

    private ImageView img, refresh;
    private AnimationDrawable animationDrawable;

    private int clickPosition = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_path, container, false);

        if (getArguments() != null) {
            User user = getArguments().getParcelable(USER);
            userId = user != null ? user.getId() : null;
        } else {
            userId = null;
        }
        initView(view);
        refreshList();
        if (userId != null && !userId.equals(CacheHelper.getInstance().getSelfInfo().getId())) {
            addPathButton.setVisibility(View.GONE);
        } else {
            addPathButton.setVisibility(View.VISIBLE);
        }
        return view;
    }

    private void initView(View v) {
        img = (ImageView) v.findViewById(R.id.anim);
        animationDrawable = (AnimationDrawable) img.getBackground();
        refresh = (ImageView) v.findViewById(R.id.refresh_view);
        refresh.setOnClickListener(this);

        adapter = new PathAdapter(getActivity(), pathList);
        listView = (PullToRefreshListView) v.findViewById(R.id.path_pto_listview);
        listView.setAdapter(adapter);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setRefreshing(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = new HashMap<>();
                map.put("discover", pathList.get(position - 1).getTitle());
                MobclickAgent.onEvent(getActivity(), "click_discover", map);

                clickPosition = position - 1;

                Intent intent = new Intent(getActivity(), PathViewActivity.class);
                intent.putExtra(PathViewActivity.PATH_TO_VIEW, pathList.get(position - 1));
                startActivityForResult(intent, PathFragment.EDIT_PATH);
            }
        });
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //下拉刷新
                page = 1;
                pathList.clear();
                adapter.notifyDataSetChanged();
                refreshList();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //上拉加载更多
                page++;
                refreshList();
            }
        });
        addPathButton = (ImageView) v.findViewById(R.id.iv_add_path);
        addPathButton.setOnClickListener(this);
    }

    private void startAnim() {
        if (!animationDrawable.isRunning()) {
            img.setVisibility(View.VISIBLE);
            animationDrawable.start();
        }
    }

    private void stopAnim() {
        if (animationDrawable.isRunning()) {
            img.setVisibility(View.GONE);
            animationDrawable.stop();
        }
    }

    private void refreshList() {
        new GetPaths().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_PATH:
                if (resultCode == PathEditActivity.RESULT_OK && data.hasExtra(PathEditActivity.ADDED_PATH)) {
                    final Path path = data.getParcelableExtra(PathEditActivity.ADDED_PATH);
                    addPath(path);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                EMGroupManager.getInstance().createPublicGroup(path.getTitle().equals("") ? CacheHelper.getInstance().getSelfInfo().getNickname() :
                                        path.getTitle(), path.getText(), null, true);
                            } catch (EaseMobException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                break;
            case EDIT_PATH:
                if (resultCode == PathViewActivity.RESULT_OK) {
                    if (data.hasExtra(PathViewActivity.EDITED_PATH)) {
                        modifyPath((Path) data.getParcelableExtra(PathViewActivity.EDITED_PATH));
                    } else if (data.hasExtra(PathViewActivity.DELETED_PATH)) {
                        removePath((Path) data.getParcelableExtra(PathViewActivity.DELETED_PATH));
                    }
                }
                break;
        }
    }

    private void addPath(Path path) {
        //TODO Scroll so that the view is visible
        pathList.add(0, path);
        adapter.notifyDataSetChanged();
    }

    private void removePath(Path path) {
        pathList.remove(path);
        adapter.notifyDataSetChanged();
    }

    // If this becomes problematic just replace the view with a newly generated one from getPathView()
    private void modifyPath(Path path) {
        pathList.set(clickPosition, path);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add_path:
                if (LingoXApplication.getInstance().getSkip()) {
                    SkipDialog.getDialog(getActivity()).show();
                } else {
                    MobclickAgent.onEvent(getActivity(), "add_discover");
                    Intent intent = new Intent(getActivity(), PathEditActivity.class);
                    intent.putExtra("PathFragment", 0);
                    startActivityForResult(intent, ADD_PATH);
                }
                break;
            case R.id.refresh_view:
                refreshList();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("pathFragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("pathFragment");
    }

    private class GetPaths extends AsyncTask<Void, String, Boolean> {
        ArrayList<Path> tempPathList;

        @Override
        protected void onPreExecute() {
            tempPathList = new ArrayList<>();
            if (listView.getCurrentMode()== PullToRefreshBase.Mode.PULL_FROM_START){
                listView.setMode(PullToRefreshBase.Mode.DISABLED);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (userId == null) {
                    tempPathList.addAll(ServerHelper.getInstance().getAllPaths(page));
                } else {
                    tempPathList.addAll(ServerHelper.getInstance().getUsersPaths(userId, page));
                }
                if (!LingoXApplication.getInstance().getSkip()) {
                    for (Path path : tempPathList) {
                        User tempUser = CacheHelper.getInstance().getUserInfo(path.getUserId());
                        if (tempUser == null) {
                            CacheHelper.getInstance().addUserInfo(ServerHelper.getInstance().getUserInfo(path.getUserId()
                            ));
                        }
                    }
                }
                // 将数据添加到集合中
                pathList.addAll(tempPathList);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "getData() error:" + e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (listView.getCurrentMode() == PullToRefreshBase.Mode.DISABLED) {
                listView.setMode(PullToRefreshBase.Mode.BOTH);
            }
            listView.onRefreshComplete();
            if (success) {
                if (pathList.size() != 0 && tempPathList.size() == 0) {
                    Toast.makeText(getActivity(), "No more data", Toast.LENGTH_SHORT).show();
                }
                refresh.setVisibility(View.GONE);
                try {
                    if (pathList.size() > 0) {
                        stopAnim();
                    } else {
                        startAnim();
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "GetPaths.onPostExecute: " + e.toString());
                }
            } else {
                refresh.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), getString(R.string.check_internet_connection), Toast.LENGTH_SHORT).show();
            }
        }
    }
}