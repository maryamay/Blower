package com.codisats.blower;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Map;


public class Helper {
    public static String saveMessage(Message message, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String id = String.valueOf(System.currentTimeMillis());
        editor.putString(id, message.getTitle());


        editor.commit();
        return id;

    }

    public static ArrayList<Message> getAllMessages(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        ArrayList<Message> messages = new ArrayList<>();

        Map<String, ?> key = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : key.entrySet()) {
            String savedData = (String) entry.getValue();

            if (savedData != null) {
                Message message = new Message(entry.getKey(), savedData);

                messages.add(message);
            }

        }

        return messages;
    }

    public static void removeMessage(String id, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(id);

        editor.commit();

    }

//    public static  void displayConfirmation(String msg, Context context){
//        Intent intent = new Intent(context, ConfirmationActivity.class);
//        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
//
//        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, msg);
//        context.startActivity(intent);
//
//    }
}
