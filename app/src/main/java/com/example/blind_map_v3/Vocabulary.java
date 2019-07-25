package com.example.blind_map_v3;

import java.util.Locale;

public final class Vocabulary {

    static String FeaterFormatText = "%s %s %s %d %s";

    static String DISTANCE = "Distance";
    static String YOUR_ADDRESS = "Your Address ";
    static String WRONG_COMMAND = "Wrong command!";
    static String FAILED_TO_RECOGNIZE_SPEECH = "Failed to recognize speech!";
    static String ADDRESS_NOT_FOUND = "Address not found";
    static String METERS = "meters";
    static String THERE_ARE_NO_POINTS_OF_INTEREST_AROUND= "There are no points of interest around";
    static String[] commands = {"address", "what is near", "point"};

    static String NOTHING_FOUND_YET = "nothing found yet";
    static String THERE_IS = "There is ";
    static String TURN_LEFT = "Turn left ";
    static String TURN_RIGHT = "Turn right ";
    static String TURN_BACK = "Turn around ";
    static String TURN_BACK_LEFT = "Turn back left ";
    static String TURN_BACK_RIGHT = "Turn back right ";


    public static void setLanguage() {
        String locale = Locale.getDefault().toString();

        if(locale.equals("en_US"))
            return;
        if(locale.equals("en_GB"))
            return;
        String[] commandsRu = {"адрес", "что рядом", "что там"};
        String[] commandsLv = {"adrese", "kas ir tuvu", "kas tur ir"};

        switch (locale) {
            case "lv_LV": {
                commands = commandsLv;
                 DISTANCE = "Attālums";
                 YOUR_ADDRESS = "Jūsu adrese ";
                 WRONG_COMMAND = "Nepareiza komanda!";
                 FAILED_TO_RECOGNIZE_SPEECH = "Neizdevās atpazīt runu!";
                 ADDRESS_NOT_FOUND = "Adrese nav atrasta";
                 METERS = "metri";
                 THERE_ARE_NO_POINTS_OF_INTEREST_AROUND= "Apkārt nav interesējošu punktu";
                THERE_IS = "ir ";
                TURN_LEFT = "Kreisajā pusē ";
                TURN_RIGHT = "Labajā pusē ";
                TURN_BACK = "Apgriezieties, tur ";
                TURN_BACK_LEFT = "Pagriezieties pa labi, tur ";
                TURN_BACK_RIGHT = "Pagriezieties pa kreisi, tur ";
                break;
            }
            case "ru_RU": {
                commands = commandsRu;
                DISTANCE = "Расстояние";
                YOUR_ADDRESS = "Ваш адрес ";
                WRONG_COMMAND = "Не правильная команда!";
                FAILED_TO_RECOGNIZE_SPEECH = "Не удалось распознать речь!";
                ADDRESS_NOT_FOUND = "Адрес не найден";
                METERS = "метров";
                THERE_ARE_NO_POINTS_OF_INTEREST_AROUND= "Рядом нет ничего интересного";
                THERE_IS = ", там ";
                TURN_LEFT = "Поверните налево";
                TURN_RIGHT = "Поверните направо";
                TURN_BACK = "РАЗВЕРНИТЕСЬ";
                TURN_BACK_LEFT = "Поверните налево назад";
                TURN_BACK_RIGHT = "Поверните направо назад ";
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
