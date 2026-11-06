package br.edu.unifor.nubex.common.util;

import java.time.LocalDate;
import java.time.ZoneId;

public final class FusoNubex {

    public static final ZoneId FORTALEZA = ZoneId.of("America/Fortaleza");

    private FusoNubex() {
    }

    public static LocalDate hoje() {
        return LocalDate.now(FORTALEZA);
    }
}
