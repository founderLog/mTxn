package com.github.mtxn.exception;

public class ApiException extends RuntimeException {
    private static final long serialVersionUID = 7419115665474379007L;
    private long code;

    public ApiException() {
    }

    public ApiException(long code, String message) {
        super(message);
        this.code = code;
    }

    public ApiException(long code, Throwable throwable) {
        super(throwable);
        this.code = code;
    }

    public ApiException(long code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
    }

    public long getCode() {
        return this.code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "ApiException(code=" + this.getCode() + ")";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ApiException)) {
            return false;
        } else {
            ApiException other = (ApiException) o;
            if (!other.canEqual(this)) {
                return false;
            } else if (!super.equals(o)) {
                return false;
            } else {
                return this.getCode() == other.getCode();
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof ApiException;
    }

    public int hashCode() {
        int result = super.hashCode();
        long $code = this.getCode();
        result = result * 59 + (int) ($code >>> 32 ^ $code);
        return result;
    }
}