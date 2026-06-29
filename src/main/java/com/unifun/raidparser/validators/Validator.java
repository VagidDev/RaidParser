package com.unifun.raidparser.validators;

public interface Validator<R, T> {
    R validate(T t);
}