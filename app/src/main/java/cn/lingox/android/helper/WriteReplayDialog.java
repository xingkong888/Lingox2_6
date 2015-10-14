package cn.lingox.android.helper;

import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import cn.lingox.android.R;
import cn.lingox.android.entity.Reference;

public class WriteReplayDialog extends DialogFragment implements View.OnClickListener {

    private static Reference reference;
    private static Handler handler;
    private EditText editText;
    private Button yes, no;

    public static WriteReplayDialog newInstance(Handler handler1, Reference reference1) {

        reference = reference1;
        handler = handler1;

        WriteReplayDialog editer = new WriteReplayDialog();
        Bundle bundle = new Bundle();
        editer.setArguments(bundle);
        return editer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_write_replay, null);
        editText = (EditText) view.findViewById(R.id.write_replay);
        yes = (Button) view.findViewById(R.id.yes);
        yes.setOnClickListener(this);
        no = (Button) view.findViewById(R.id.no);
        no.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yes:
                new MyAsynTask().execute();
//                reference.setContent();
                break;
            case R.id.no:
                dismiss();
                break;
        }
    }

    class MyAsynTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            dismiss();
            if (true) {
//            if (aBoolean){
                handler.sendMessage(new Message());
            }
        }
    }
}
