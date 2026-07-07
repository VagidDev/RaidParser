package com.unifun.raidparser.core.filters;


public abstract class EmptyFilter<T> extends AbstractFilter<T> {
    @Override
    public boolean filter(String text) {
        return text.isBlank();
    }

}
