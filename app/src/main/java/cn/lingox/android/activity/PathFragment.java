//package cn.lingox.android.activity;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.FrameLayout;
//
//import cn.lingox.android.R;
//
//public class PathFragment extends Fragment implements View.OnClickListener {
//
//    private int fragmentContainerId;
//    private Fragment mContent;
//    private Button local, travel;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_path, container, false);
//        FrameLayout fragmentContainer = (FrameLayout) view.findViewById(R.id.path_framelayout);
//
//        fragmentContainerId = R.id.path_framelayout;
//        fragmentContainer.setId(fragmentContainerId);
//
//        FragmentManager fm = getFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//        Fragment oldFragment = fm.findFragmentById(fragmentContainerId);
//        if (oldFragment != null) {
//            ft.remove(oldFragment);
//        }
//        mContent = LocalFragment.newInstance();
//        ft.add(fragmentContainerId, mContent);
//        ft.commit();
//        //本地人
//        local = (Button) view.findViewById(R.id.path_local);
//        local.setOnClickListener(this);
//        //旅行者
//        travel = (Button) view.findViewById(R.id.path_travel);
//        travel.setOnClickListener(this);
//
//        return view;
//    }
//
//    @Override
//    public void onClick(View v) {
//        Fragment newFragment;
//        switch (v.getId()) {
//            case R.id.path_local:
//                newFragment = LocalFragment.newInstance();
//                switchContent(newFragment);
//                local.setBackgroundColor(Color.WHITE);
//                travel.setBackgroundColor(getResources().getColor(R.color.three_c7));
//                local.setTextColor(Color.BLACK);
//                travel.setTextColor(Color.rgb(153, 153, 153));
//                break;
//            case R.id.path_travel:
//                newFragment = TravelFragment.newInstance();
//                switchContent(newFragment);
//                travel.setBackgroundColor(Color.WHITE);
//                local.setBackgroundColor(getResources().getColor(R.color.three_c7));
//                travel.setTextColor(Color.BLACK);
//                local.setTextColor(Color.rgb(153, 153, 153));
//                break;
//        }
//    }
//
//    /**
//     * 修改fragment的可见度，不会导致fragment重新加载
//     **/
//    public void switchContent(Fragment to) {
//        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//        if (!to.isAdded()) { // 先判断是否被add过
//            transaction.hide(mContent).add(fragmentContainerId, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
//        } else {
//            transaction.hide(mContent).show(to).commit(); // 隐藏当前的fragment，显示下一个
//        }
//        mContent = to;
//    }
//}