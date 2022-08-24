package com.github.mtxn.utils;

import com.github.mtxn.exception.ApiException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class ExceptionUtils {
    private static final String NULL = null;

    private ExceptionUtils() {
    }

    public static String getMessage(Throwable e) {
        return null == e ? NULL : String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
    }

    public static String getSimpleMessage(Throwable e) {
        return null == e ? NULL : e.getMessage();
    }

    public static RuntimeException wrapRuntime(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        } else if (throwable instanceof Error) {
            throw (Error) throwable;
        } else {
            return new RuntimeException(throwable);
        }
    }

    public static Throwable unwrap(Throwable wrapped) {
        Throwable unwrapped = wrapped;

        while (true) {
            while (!(unwrapped instanceof InvocationTargetException)) {
                if (!(unwrapped instanceof UndeclaredThrowableException)) {
                    return unwrapped;
                }

                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            }

            unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
        }
    }

    public static StackTraceElement[] getStackElements() {
        return Thread.currentThread().getStackTrace();
    }

    public static StackTraceElement getStackElement(int i) {
        return getStackElements()[i];
    }

    public static StackTraceElement getRootStackElement() {
        StackTraceElement[] stackElements = getStackElements();
        return stackElements[stackElements.length - 1];
    }

    public static boolean isCausedBy(Throwable throwable, Class<? extends Exception>... causeClasses) {
        return null != getCausedBy(throwable, causeClasses);
    }

    public static Throwable getCausedBy(Throwable throwable, Class<? extends Exception>... causeClasses) {
        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            Class[] var3 = causeClasses;
            int var4 = causeClasses.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Class<? extends Exception> causeClass = var3[var5];
                if (causeClass.isInstance(cause)) {
                    return cause;
                }
            }
        }

        return null;
    }

    public static RuntimeException runtime(Throwable throwable) {
        return wrapRuntime(throwable);
    }

    public static RuntimeException runtime(String message, Throwable throwable) {
        return new RuntimeException(message, throwable);
    }

    public static RuntimeException runtime(String format, Object... args) {
        return new RuntimeException(String.format(format, args));
    }

    public static RuntimeException runtime(Throwable throwable, String format, Object... args) {
        return new RuntimeException(String.format(format, args), throwable);
    }

    public static NullPointerException nullPointer(String format, Object... args) {
        return new NullPointerException(String.format(format, args));
    }

    public static IllegalArgumentException argument(String format, Object... args) {
        return new IllegalArgumentException(String.format(format, args));
    }

    public static IllegalArgumentException argument(Throwable throwable, String format, Object... args) {
        return new IllegalArgumentException(String.format(format, args), throwable);
    }

    public static IllegalArgumentException argument(String message, Throwable throwable) {
        return new IllegalArgumentException(message, throwable);
    }

    public static UnsupportedOperationException unsupported(Throwable throwable, String format, Object... args) {
        return new UnsupportedOperationException(String.format(format, args), throwable);
    }

    public static UnsupportedOperationException unsupported(String format, Object... args) {
        return new UnsupportedOperationException(String.format(format, args));
    }

    public static UnsupportedOperationException unsupported(String message, Throwable throwable) {
        return new UnsupportedOperationException(message, throwable);
    }

    public static ApiException api(Throwable throwable, String format, Object... args) {
        return new ApiException(0L, String.format(format, args), throwable);
    }

    public static ApiException api(String format, Object... args) {
        return new ApiException(0L, String.format(format, args));
    }

    public static ApiException api(String message, Throwable throwable) {
        return new ApiException(0L, message, throwable);
    }
}