package com.kpi.web.systems.lab3.entity.enums;

import java.util.stream.Stream;

public enum Language {
    UA,
    EN;

    public static Language getLanguage(String languageCode) {
        return Stream.of(Language.values())
                .filter(l->l.toString().equalsIgnoreCase(languageCode))
                .findFirst().orElse(UA);
    }
}
