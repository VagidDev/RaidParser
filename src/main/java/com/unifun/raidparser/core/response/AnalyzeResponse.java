package com.unifun.raidparser.core.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AnalyzeResponse<T> {
    private T status;
    private String errorText;

    public AnalyzeResponse() {
    }

    public AnalyzeResponse(T status, String errorText) {
        this.status = status;
        this.errorText = errorText;
    }

}
