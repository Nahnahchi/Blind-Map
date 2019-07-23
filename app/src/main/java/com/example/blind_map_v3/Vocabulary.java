package com.example.blind_map_v3;

import java.util.Locale;

public class Vocabulary {

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
}
