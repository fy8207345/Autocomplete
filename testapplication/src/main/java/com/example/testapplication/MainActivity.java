package com.example.testapplication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.PopupWindowCompat;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity implements KeyboardHeightProvider.KeyboardHeightObserver, ScrollViewListener {

    private EditText title;
    private PopupWindow popupWindow;
    private ViewTreeObserver.OnGlobalLayoutListener listener;
    private KeyboardHeightProvider keyboardHeightProvider;
    private int keyboardHeight = 0;
    private ScrollViewExt scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        keyboardHeightProvider = new KeyboardHeightProvider(this);

        View rootView = getWindow().getDecorView();
        listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);
                int screenHeight = rootView.getHeight();
                int heightDifference = screenHeight - (rect.bottom - rect.top);
                Log.d("Keyboard Size", "Size: " + heightDifference);
            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(listener);

        scrollView = findViewById(R.id.scroll);
        scrollView.setScrollViewListener(this);
        title = findViewById(R.id.test);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if(text.length() > 0){
                    showPop();
                }else {
                    hidePop();
                }
            }
        });
        title.setOnFocusChangeListener((v, hasFocus) -> {
            System.out.println("focus: " + hasFocus);
            if(hasFocus){

            }
        });
        title.setOnClickListener(v -> {
            System.out.println("clicked");
            int top = title.getTop();
            int windowHeight;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                WindowMetrics windowMetrics = getWindow().getWindowManager().getCurrentWindowMetrics();
                Rect bounds = windowMetrics.getBounds();
                windowHeight = bounds.height();
            }else{
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                windowHeight = displayMetrics.heightPixels;
            }
            int actionHeight = 0;
            ActionBar supportActionBar = getSupportActionBar();
            if(supportActionBar != null){
                System.out.println("actionbar height: " + supportActionBar.getHeight());
                actionHeight = supportActionBar.getHeight();
            }
            int space = windowHeight - actionHeight - keyboardHeight - title.getHeight();
            int scrollTo = top - space + title.getHeight();
            scrollView.smoothScrollTo(0, scrollTo);
        });
        title.post(() -> keyboardHeightProvider.start());
    }

    private void showPop(){
        View pop = LayoutInflater.from(this).inflate(R.layout.pop, null);
        popupWindow = new PopupWindow(this);
        popupWindow.setContentView(pop);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(300);
        popupWindow.showAsDropDown(title, 0, -(title.getHeight() + popupWindow.getHeight()));
        popupWindow.showAtLocation(title, Gravity.TOP, 0, 0);
        int maxAvailableHeight = popupWindow.getMaxAvailableHeight(title);
        System.out.println("maxAvailableHeight: " + maxAvailableHeight);
    }

    private void hidePop(){
        if(popupWindow != null){
            if(popupWindow.isShowing()){
                popupWindow.dismiss();
            }
        }
        popupWindow = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        keyboardHeightProvider.close();
    }

    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        System.out.println("onKeyboardHeightChanged: " + height + "-" + orientation);
        keyboardHeight = height;
    }

    @Override
    protected void onPause() {
        super.onPause();
        keyboardHeightProvider.setKeyboardHeightObserver(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
    }

    @Override
    public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
        System.out.println("y:" + y + "-" + oldy);
    }
}