package com.codisats.blower;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.Delayed;

public class DeleteActivity extends WearableActivity implements DelayedConfirmationView.DelayedConfirmationListener {

    DelayedConfirmationView delayedConfirmationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        delayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.delayed_confirm);

        delayedConfirmationView.setListener(this);
        delayedConfirmationView.setTotalTimeMs(3000);

        delayedConfirmationView.start();



    }

    @Override
    public void onTimerFinished(View v) {
        Helper.displayConfirmation("Deleted", this);

        String id = getIntent().getStringExtra("id");

        Helper.removeMessage(id, this);


        finish();
    }

    @Override
    public void onTimerSelected(View v) {
        Helper.displayConfirmation("Cancelled", this);
        delayedConfirmationView.reset();
        finish();

    }
}
