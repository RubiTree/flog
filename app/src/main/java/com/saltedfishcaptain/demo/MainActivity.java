package com.saltedfishcaptain.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static android.icu.text.RelativeDateTimeFormatter.Direction.THIS;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.print_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FLogTestCase().basePrintTest(this);
            }
        });
    }
}