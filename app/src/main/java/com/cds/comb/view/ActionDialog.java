package com.cds.comb.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cds.comb.R;

/**
 * @Author: chengzj
 * @CreateDate: 2019/2/27 17:37
 * @Version: 3.0.0
 */
public class ActionDialog extends Dialog implements View.OnClickListener {

    protected Context mContext;

    protected WindowManager.LayoutParams mLayoutParams;

    Button button1;

    Button button2;

    Button button3;

    public ActionDialog(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public ActionDialog(@NonNull Context context, String name, String phone) {
        super(context);
        initView(context);

    }

    public ActionDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        initView(context);
    }

    protected ActionDialog(@NonNull Context context, boolean cancelable, @Nullable DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setBackgroundDrawableResource(R.mipmap.transparent_bg);
        Window window = this.getWindow();
        mLayoutParams = window.getAttributes();
        mLayoutParams.alpha = 1f;
        window.setAttributes(mLayoutParams);
        mLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mLayoutParams.gravity = Gravity.CENTER;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_action, null);

        button1 = (Button) dialogView.findViewById(R.id.button1);
        button2 = (Button) dialogView.findViewById(R.id.button2);
        button3 = (Button) dialogView.findViewById(R.id.button3);

        button1.setOnClickListener(this);

        button2.setOnClickListener(this);

        button3.setOnClickListener(this);


//        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(dialogView);
    }

    int model;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                model = 1;
                break;
            case R.id.button2:
                model = 2;
                break;
            case R.id.button3:
                model = 3;
                break;

        }
        if (listener != null) {
            listener.onActionClick(model);
        }
        dismiss();
    }

    onActionClickListener listener;

    public ActionDialog setListener(onActionClickListener listener) {
        this.listener = listener;
        return this;
    }

    public interface onActionClickListener {
        void onActionClick(int index);
    }
}