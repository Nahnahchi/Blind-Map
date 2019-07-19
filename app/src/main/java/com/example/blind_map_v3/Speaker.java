package com.example.blind_map_v3;

import android.app.Activity;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class Speaker {

    private TextToSpeech t2s; // Text to speech

    // Init object
    public Speaker(Activity activity) {
        t2s = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                setLanguage("ENG");
            }
        });
    }

    // Pass string of text to be spoken
    public void speak(String toSpeak) {
        t2s.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    // Pause the speaker
    public void onPause(Activity activity) {
        if (t2s != null) {
            t2s.stop();
            t2s.shutdown();
        }
    }

    // Change the language of the speaker
    public void setLanguage(String locale) {
        switch (locale) {
            case "ENG":
                t2s.setLanguage(Locale.US);
                break;
            case "JPN":
                t2s.setLanguage(Locale.JAPAN);
                break;

        }
    }

}
