package com.codisats.blower;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity {

  ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListPView) findViewById(R.id.list_view);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    displaySpeechScreen();
                }else{
                    Message message = (Message) parent.getItemAtPosition(position);

                    Intent intent = new Intent(getApplicationContext(), DeleteActivity.class);
                    intent.putExtra("id", message.getId());

                    startActivity(intent);
                }
            }
        });

        updateUI();
    }

    @Override
    public  void onResume() {
        super.onResume();

        updateUI();
    }

    private void updateUI() {
        ArrayList<Message> messages = Helper.getAllMessages(this);

        messages.add(0, new Message(" ",""));

        listView.setAdapter(new ListViewAdapter(this, 0, messages));


    }

    public void displaySpeechScreen() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What is the title for the message?");

                //Starts the activity, intent will be populated with speech data text
                startActivityForResult(intent, 1001);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1001 && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            String msg = results.get(0);

            Message message = new Message(null, msg);

            Helper.saveMessage(message, this);

            Helper.displayConfirmation("Message Saved!", this);

            updateUI();

        }
    }


}
