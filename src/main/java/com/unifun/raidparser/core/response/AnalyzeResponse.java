package com.unifun.raidparser.core.response;

public class AnalyzeResponse<T> {
    private T status;
    private String errorText;

    public AnalyzeResponse() {
    }

    public AnalyzeResponse(T status, String errorText) {
        this.status = status;
        this.errorText = errorText;
    }

    public T getStatus() {
        return status;
    }

    public void setStatus(T status) {
        this.status = status;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }
}
