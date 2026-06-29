package com.unifun.raidparser.dto;

public interface ParseResponse<T> {
    boolean isParsed();
    T result();
}
