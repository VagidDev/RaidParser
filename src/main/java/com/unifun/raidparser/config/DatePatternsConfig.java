package com.unifun.raidparser.config;
//Need to import annotations from spring-boot
import java.util.List;

@ConfigurationProperties("util.date.formats")
public class DatePatetrnsConfig {
    private List<String> formats;

    @ConstructorBindings
    public DatePatetrnsConfig(List<String> formats) {
        this.formats = formats;
    }
}