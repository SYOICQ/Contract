package com.suyong.contractmanager.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.suyong.contractmanager.Interfaces.TextviewListener;
import com.suyong.contractmanager.R;

public class MyTextListener implements TextWatcher {

    TextviewListener listener;
    public MyTextListener (TextviewListener listener){
        this.listener = listener;
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
       listener.before();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        listener.after(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
