package com.unifun.raidparser.validators.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RaidCommandValidator implements CommandValidator {
    private static final Logger LOGGER = LogManager.getLogger(RaidCommandValidator.class);

    List<Pattern> LINUX_MDADM = List.of(
            Pattern.compile("^cat /proc/mdstat$"),
            Pattern.compile("^mdadm --detail$"),
            Pattern.compile("^mdadm --detail --scan$")
    );

    List<Pattern> MEGARAID = List.of(
            Pattern.compile("^megacli -ldinfo -lall -aall$"),
            Pattern.compile("^megacli -pdlist -aall$"),
            Pattern.compile("^storcli /c\\d+ show$"),
            Pattern.compile("^storcli /c\\d+/vall show$"),
            Pattern.compile("^storcli /call/vall show$")
    );

    List<Pattern> HP_RAID = List.of(
            Pattern.compile("^hpssacli ctrl all show config$"),
            Pattern.compile("^ssacli ctrl all show config$")
    );

    List<Pattern> DELL_RAID = List.of(
            Pattern.compile("^perccli /c\\d+ show$"),
            Pattern.compile("^perccli /call/vall show$")
    );

    List<Pattern> GENERIC = List.of(
            Pattern.compile("^lsblk$"),
            Pattern.compile("^lsblk -o NAME,SIZE,TYPE,MOUNTPOINT$"),
            Pattern.compile("^df -h$")
    );

    private final List<Pattern> whiteList = Stream.of(
            LINUX_MDADM,
            MEGARAID,
            HP_RAID,
            DELL_RAID,
            GENERIC
    ).flatMap(List::stream).toList();


    @Override
    public Boolean validate(String command) {
        if (command == null || command.isBlank()) {
            return false;
        }

        boolean allowed = whiteList.stream()
                .anyMatch(pattern -> pattern.matcher(command).matches());

        if (!allowed) {
            LOGGER.warn("Command `{}` is not allowed!", command);
            return false;
        }

        return true;
    }
}
