package com.unifun.raidparser.validators.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Stream;

public class RaidCommandValidator implements CommandValidator {
    private static final Logger LOGGER = LogManager.getLogger(RaidCommandValidator.class);

    List<String> LINUX_MDADM = List.of(
            "cat /proc/mdstat",
            "mdadm --detail",
            "mdadm --detail --scan"
    );

    List<String> MEGARAID = List.of(
            "megacli -ldinfo -lall -aall",
            "megacli -pdlist -aall",
            "storcli /c0 show",
            "storcli /c0/vall show",
            "storcli /call/vall show"
    );

    List<String> HP_RAID = List.of(
            "hpssacli ctrl all show config",
            "ssacli ctrl all show config"
    );

    List<String> DELL_RAID = List.of(
            "perccli /c0 show",
            "perccli /call/vall show"
    );

    List<String> GENERIC = List.of(
            "lsblk",
            "lsblk -o NAME,SIZE,TYPE,MOUNTPOINT",
            "df -h"
    );

    private List<String> getWhitelist() {
        return Stream.of(
                LINUX_MDADM,
                MEGARAID,
                HP_RAID,
                DELL_RAID,
                GENERIC
        ).flatMap(List::stream).toList();
    }

    @Override
    public Boolean validate(String command) {
        if (command == null) {
            return false;
        }

        boolean allowed = getWhitelist().stream()
                .anyMatch(command::startsWith);

        if (!allowed) {
             LOGGER.warn("Command `{}` is not allowed!", command);
             return false;
        }

        return true;
    }
}
