package cn.lingox.android.helper;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.lingox.android.R;
import cn.lingox.android.entity.Reference;

public class WriteReplayDialog extends DialogFragment implements View.OnClickListener {

    private static Context context;

    private static Reference reference;
    private static Handler handler;
    private EditText editText;
    private Button yes, no;
    private String content;

    private InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

    public static WriteReplayDialog newInstance(Handler handler1,
                                                Reference reference1, Context context1) {

        reference = reference1;
        handler = handler1;
        context = context1;

        WriteReplayDialog editer = new WriteReplayDialog();
        Bundle bundle = new Bundle();
        editer.setArguments(bundle);
        return editer;
    }

    public static void hideSoftInput(IBinder token, InputMethodManager im) {
        if (token != null) {
            im.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
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
                hideSoftInput(v.getWindowToken(), im);
                content = editText.getText().toString().trim();
                if (!editText.getText().toString().trim().isEmpty()) {
                    new Thread() {
                        public void run() {
                            try {
                                ServerHelper.getInstance()
                                        .replyReference(reference.getId(), content);
                                handler.sendMessage(new Message());
                                dismiss();
                            } catch (Exception e) {
                                Toast.makeText(context, "ERROR", Toast.LENGTH_LONG).show();
                                dismiss();
                            }
                        }
                    }.start();
                } else {
                    Toast.makeText(context, "Please write something", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.no:
                break;
        }
        dismiss();
    }
}
