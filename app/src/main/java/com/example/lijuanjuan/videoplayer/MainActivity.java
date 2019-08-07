package com.example.lijuanjuan.videoplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int i = 1;
        ClassLoader classLoader = getClassLoader();
        if (classLoader != null) {
            Log.i(TAG, "[onCreate] classLoader " + i + " : " + classLoader.toString());
            while (classLoader.getParent() != null) {
                classLoader = classLoader.getParent();
                i++;
                Log.i(TAG, "[onCreate] classLoader " + i + " : " + classLoader.toString());
            }
        }
    }
}
