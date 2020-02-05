package com.codisats.blower;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class DeleteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        String id = getIntent().getStringExtra("id");

        Helper.removeMessage(id, this);

        finish();

    }

}
