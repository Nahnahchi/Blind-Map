package com.example.blind_map_v3;

import java.util.Locale;

public final class Vocabulary {

    static String DISTANCE;
    static String YOUR_ADDRESS;
    static String WRONG_COMMAND;
    static String FAILED_TO_RECOGNIZE_SPEECH;
    static String ADDRESS_NOT_FOUND;
    static String METERS;
    static String NOTHING_FOUND_YET;


    public Vocabulary(String[] commands) {
        DISTANCE = "";
    }


    final String[] commandsEn = {"address", "what nearest", "what there"};
    final String[] commandsLv = {"adrese", "kas ir tuvu", "kas tur ir"};
    final String[] commandsRu = {"адрес", "что рядом", "что там"};
    String[] commands;

    public void setLanguage() {


        switch (Locale.getDefault().toString()) {
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
                commands = commandsEn;
                break;
            }
        }
    }

    /*
    TODO Feature tybes needed to be translate
    Bank
Cafe
Car Parking
Shop
Nightclub
Hotel
Convenience Store
Restaurant
Butcher
Liquor Store
Greengrocer
Mobile Phone Store
Bakery
Pet Store
Optician
Furniture Store
Hospital
Cosmetics Store
Dry Cleaner
Gift Shop
Mall
TODO find them all
     */
}
