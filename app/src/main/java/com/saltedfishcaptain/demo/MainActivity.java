package com.saltedfishcaptain.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import static android.icu.text.RelativeDateTimeFormatter.Direction.THIS;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new FLogTestCase().basePrintTest(this);
    }
}
