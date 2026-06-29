package com.unifun.raidparser.validators;

public interface SimpleValidator<T> extends Validator<Boolean, T> {
    Boolean validate(T t);
}