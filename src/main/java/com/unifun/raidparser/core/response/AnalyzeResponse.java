package com.unifun.raidparser.core.response;

import com.unifun.raidparser.core.filters.Status;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AnalyzeResponse<T extends Status> {
    private T status;
    private String errorText;

    public AnalyzeResponse() {
    }

    public AnalyzeResponse(T status, String errorText) {
        this.status = status;
        this.errorText = errorText;
    }

}
