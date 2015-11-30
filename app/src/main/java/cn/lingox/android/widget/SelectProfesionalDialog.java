package cn.lingox.android.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;

public class SelectProfesionalDialog extends DialogFragment implements View.OnClickListener {

    private static TextView text;
    private static Context context;
    private static User user;
    private ListView listView;
    private ArrayList<String> datas;

    public static SelectProfesionalDialog newInstance(Context context1, User user1, TextView text1) {

        context = context1;
        user = user1;
        text = text1;

        SelectProfesionalDialog editer = new SelectProfesionalDialog();
        Bundle bundle = new Bundle();
        bundle.putString("title", "professional");
        editer.setArguments(bundle);
        return editer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_dialog, null);

        listView = (ListView) view.findViewById(R.id.listview);

        Button cancel = (Button) view.findViewById(R.id.select_cancel);
        cancel.setOnClickListener(this);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        initData();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_cancel:
                dismiss();
                break;
        }
    }

    private void initData() {
        datas = new ArrayList<>();
        for (String str : getResources().getStringArray(R.array.major)) {
            datas.add(str);
        }
        MySelcetAdapter adapter = new MySelcetAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    user.setProfession(datas.get(position));
                    text.setTextColor(Color.rgb(25, 143, 153));
                    text.setText(datas.get(position));
                }
                new UpdateUserInfo("professional").execute();
                dismiss();
            }
        });
    }

    //TODO 提交数据更新
    private class UpdateUserInfo extends cn.lingox.android.video.util.AsyncTask<Void, String, Boolean> {
        private ProgressDialog pd;
        private String flag;

        public UpdateUserInfo(String flg) {
            pd = new ProgressDialog(getActivity());
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
            flag = flg;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage(getString(R.string.updating_account_info));
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HashMap<String, String> updateParams = new HashMap<>();
            updateParams.put(StringConstant.userIdStr, user.getId());
            switch (flag) {
                case "speak":
                    updateParams.put(StringConstant.speakStr, user.getSpeak());
                    break;
                case "professional":
                    updateParams.put(StringConstant.professionStr, user.getProfession());
                    break;
            }
            try {
                User returnUser = ServerHelper.getInstance().updateUserInfo(updateParams);

                CacheHelper.getInstance().setSelfInfo(returnUser);
                return true;
            } catch (final Exception e) {
                publishProgress(null, "Error updating account information");
                return false;
            }
        }

        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null)
                pd.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (!success) {
                Toast.makeText(getActivity(), "Failure to submit", Toast.LENGTH_LONG).show();
            }
        }
    }

    class MySelcetAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.textview, null);
                viewHolder.item = (TextView) convertView.findViewById(R.id.item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.item.setText(datas.get(position));
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView item;
        }
    }
}
