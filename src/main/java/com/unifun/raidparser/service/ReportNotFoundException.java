package com.unifun.raidparser.service;

import java.time.LocalDate;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(LocalDate date) {
        super("Report for date " + date + " is not available locally or on sftp");
    }
}
