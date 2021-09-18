package com.spdbccc.task;

/**
 * @Date 2021/9/17
 * @Author xujin
 * @TODO
 * @Version 1.0
 */
public class ExecuteResult {

    private boolean success;
    private String message;
    private Exception e;

    public static ExecuteResult success(String message) {
        return new ExecuteResult(true, message, null);
    }

    public static ExecuteResult failed(String message) {
        return new ExecuteResult(false, message, new Exception(message));
    }

    public static ExecuteResult failed(Exception e) {
        return new ExecuteResult(false, e.getMessage(), null);
    }

    public ExecuteResult(boolean success, String message, Exception e) {
        this.success = success;
        this.message = message;
        this.e = e;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Exception getE() {
        return e;
    }

    public void setE(Exception e) {
        this.e = e;
    }

    @Override
    public String toString() {
        return "ExecuteResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", e=" + e +
                '}';
    }
}
