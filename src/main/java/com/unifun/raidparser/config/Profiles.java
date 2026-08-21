package com.unifun.raidparser.config;

/** Профили режимов запуска: определяют, какие бины поднимаются. */
public final class Profiles {
    /** Интерактивная консоль (-i): без веб-контейнера и контроллеров. */
    public static final String CONSOLE = "console";
    /** Серверный режим (-d): REST API, без интерактивной консоли. */
    public static final String SERVER = "server";

    private Profiles() {
    }
}
