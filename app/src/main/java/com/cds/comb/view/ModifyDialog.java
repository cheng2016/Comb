package com.cds.comb.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.cds.comb.R;
import com.cds.comb.data.entity.Light;


/**
 * @Author: chengzj
 * @CreateDate: 2019/2/25 17:51
 * @Version: 3.0.0
 */
public class ModifyDialog {
    private Context context;
    private Dialog dialog;

    EditText irLightEditText;

    EditText irTimeEditText;

    EditText redLightEditText;

    EditText redTimeEditText;

    Button confirmBtn;

    Button cancelBtn;

    public ModifyDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
       Display display = windowManager.getDefaultDisplay();
    }

    public ModifyDialog builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_modify, null);
        irLightEditText = (EditText) view.findViewById(R.id.ir_light_editText);
        irTimeEditText = (EditText) view.findViewById(R.id.ir_time_editText);
        redLightEditText = (EditText) view.findViewById(R.id.red_light_editText);
        redTimeEditText = (EditText) view.findViewById(R.id.red_time_editText);

        confirmBtn = (Button) view.findViewById(R.id.confirm_btn);
        cancelBtn = (Button) view.findViewById(R.id.cancel_btn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        // 定义Dialog布局和参数
        dialog = new Dialog(context);
        dialog.setContentView(view);
        Window dialogWindow = dialog.getWindow();
//        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        lp.x = 0;
//        lp.y = 0;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);

        return this;
    }


    public ModifyDialog setData(Light light) {
        irLightEditText.setText(light.getIr());
        irTimeEditText.setText(light.getIrTime());

        redLightEditText.setText(light.getRed());
        redTimeEditText.setText(light.getRedTime());
        return this;
    }

    public ModifyDialog setPositiveButton(final OnModifyClickListener listener) {
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                listener.onClick(view, new Light(irLightEditText.getText().toString().trim(),
                        irTimeEditText.getText().toString().trim(),
                        redLightEditText.getText().toString().trim(),
                        redTimeEditText.getText().toString().trim()));
            }
        });
        return this;
    }

    public ModifyDialog setPositiveButton(String str, final OnModifyClickListener listener) {
        confirmBtn.setText(str);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                listener.onClick(view, new Light(irLightEditText.getText().toString().trim(),
                        irTimeEditText.getText().toString().trim(),
                        redLightEditText.getText().toString().trim(),
                        redTimeEditText.getText().toString().trim()));
            }
        });
        return this;
    }

    public ModifyDialog show() {
        dialog.show();
        return this;
    }

    public interface OnModifyClickListener {
        void onClick(View var1, Light light);
    }
}
