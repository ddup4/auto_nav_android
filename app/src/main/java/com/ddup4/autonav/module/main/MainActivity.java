package com.ddup4.autonav.module.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ddup4.autonav.R;

public class MainActivity extends AppCompatActivity {

    public static Intent startIntent(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        return starter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ddup4_autonav_module_main_view);
    }

}
