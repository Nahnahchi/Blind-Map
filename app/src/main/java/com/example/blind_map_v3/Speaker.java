package com.example.blind_map_v3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class Speaker {

    private static boolean lock = false;
    private static TextToSpeech t2s; // Text to speech

    // Init object
    Speaker(final Activity activity, final String language, final String toSpeak) {
        t2s = new TextToSpeech(activity.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Locale locale;
                if (status == TextToSpeech.SUCCESS) {
                    if (language != null) {
                        Locale[] locales = Locale.getAvailableLocales();
                        int i = 0;
                        while (true) {
                             try {
                                 if ((locales[i].toString()).equals(language)) {
                                     break;
                                 }
                                 i++;
                             } catch (Exception e) {
                                 AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                                 alertDialog.setTitle("Language not supported");
                                 alertDialog.setMessage(String.format("Language %s is not supported", language));
                                 alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                         new DialogInterface.OnClickListener() {
                                             public void onClick(DialogInterface dialog, int which) {
                                                 dialog.dismiss();
                                             }
                                         });
                                 alertDialog.show();
                                 return;
                             }
                        }
                        locale = new Locale(language);
                    } else {
                        locale = activity.getResources().getConfiguration().locale;
                    }
                    int result = t2s.setLanguage(locale);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        new AlertDialog.Builder(activity)
                                .setTitle("Language not supported by current TTS engine")
                                .setMessage("Do you want to install eSpeak Text-To-Speech engine?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("https://play.google.com/store/apps/details?id=com.redzoc.ramees.tts.espeak"));
                                        activity.startActivity(browserIntent);
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                            Log.e("TTS", "Language " + locale +" is not supported");
                    } else {
                        speak(toSpeak);
                    }
                } else {
                    Log.e("TTS", "Initilization Failed");
                }
            }
        });
    }

    // Pass string of text to be spoken
    private void speak(String toSpeak) {
        boolean wasDigit = false;
        StringBuilder words = new StringBuilder();
        for (char ch : toSpeak.toCharArray()) {
            if (Character.isDigit(ch) || ch == '-') {
                wasDigit = true;
                words.append(ch);
            }  else {
                if (wasDigit) {
                    words.append("|");
                    wasDigit = false;
                }
                words.append(ch);
            }
        }
        t2s.speak(words.toString(), TextToSpeech.QUEUE_FLUSH, null);
    }

    static void onPause() {
        if (t2s != null) {
            t2s.stop();
            t2s.shutdown();
        }
    }

}
