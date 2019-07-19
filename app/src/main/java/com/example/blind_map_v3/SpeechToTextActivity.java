package com.example.blind_map_v3;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechToTextActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 10;
    ImageButton mVoiceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text);

        mVoiceBtn = findViewById(R.id.voiceBtn);

        mVoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak();
            }
        });
    }

    private void speak() {
        // intent to show speech to a text dialog
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // start intent
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // get voice input and handle it
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            final String[] commandsEn = {"address", "places", "interest"};
            final String[] commandsLv = {"adrese", "interesantas", "vietas"};
            final String[] commandsRu = {"адрес", "достопримечательности", "места"};
            String[] commands;
            switch(Locale.getDefault().toString()) {
                case "en_GB":
                case "en_US": {
                    commands = commandsEn;
                    break;
                }
                case "lv_LV": {
                    commands = commandsLv;
                    break;
                }
                case "ru_RU": {
                    commands = commandsRu;
                    break;
                }
                default: {
                    commands = null;
                    Toast.makeText(getApplicationContext(), "Please set your phone language to EN, RU or LV for voice commands!", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            if(commands != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result.get(0).contains(commands[0])) {
                    Toast.makeText(getApplicationContext(), commands[0], Toast.LENGTH_SHORT).show();
                } else if (result.get(0).contains(commands[1]) || result.get(0).contains(commands[2])) {
                    Toast.makeText(getApplicationContext(), commands[1] + " " + commands[2], Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Wrong command!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to recognize speech!", Toast.LENGTH_SHORT).show();
        }
    }
}
