package com.unifun.raidparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class RaidParserApp {
    public static void main(String[] args) {
        SpringApplication.run(RaidParserApp.class);
        System.out.println(Arrays.toString(args));
    }
}
