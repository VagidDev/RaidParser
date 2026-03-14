package com.unifun.raidparser.parser;

public interface ParseResponse<T> {
    boolean isParsed();
    T result();
}
