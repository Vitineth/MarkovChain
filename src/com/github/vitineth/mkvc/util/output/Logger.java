package com.github.vitineth.mkvc.util.output;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ryan on 20/10/2016.
 */
public class Logger {

    private static OutputStream stdoutOutput = System.out;
    private static OutputStream stderrOutput = System.err;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss z]");

    public static void setStdoutOutput(OutputStream stdoutOutput) {
        Logger.stdoutOutput = stdoutOutput;
    }

    public static OutputStream getStdoutOutput() {//[yyyy/MM/dd HH:mm:ss z]
        return stdoutOutput;
    }

    public static void setStderrOutput(OutputStream stderrOutput) {
        Logger.stderrOutput = stderrOutput;
    }

    public static OutputStream getStderrOutput() {
        return stderrOutput;
    }

    public static void setDateFormat(SimpleDateFormat dateFormat) {
        Logger.dateFormat = dateFormat;
    }

    public static SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    private static void out(String level, String label, String message, boolean useError) {
        try {
            if (useError) {
                stderrOutput.write((dateFormat.format(new Date()) + "[" + level + "\\" + label + "]: " + message + "\n").getBytes());
                stderrOutput.flush();
            } else {
                stdoutOutput.write((dateFormat.format(new Date()) + "[" + level + "\\" + label + "]: " + message + "\n").getBytes());
                stdoutOutput.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void debug(String label, String message) {
        out("DEBUG", label, message, false);
    }

    public static void debug(String label, String message, Throwable throwable) {
        out("DEBUG", label, message, false);
        for (StackTraceElement element : throwable.getStackTrace()) {
            out("DEBUG", label, element.toString(), false);
        }
    }

    public static void d(String label, String message) {
        debug(label, message);
    }

    public static void d(String label, String message, Throwable throwable) {
        debug(label, message, throwable);
    }

    public static void info(String label, String message) {
        out("INFO", label, message, false);
    }

    public static void info(String label, String message, Throwable throwable) {
        out("INFO", label, message, false);
        for (StackTraceElement element : throwable.getStackTrace()) {
            out("INFO", label, element.toString(), false);
        }
    }

    public static void i(String label, String message) {
        info(label, message);
    }

    public static void i(String label, String message, Throwable throwable) {
        info(label, message, throwable);
    }

    public static void warn(String label, String message) {
        out("WARN", label, message, true);
    }

    public static void warn(String label, String message, Throwable throwable) {
        out("WARN", label, message, true);
        for (StackTraceElement element : throwable.getStackTrace()) {
            out("WARN", label, element.toString(), true);
        }
    }

    public static void w(String label, String message) {
        warn(label, message);
    }

    public static void w(String label, String message, Throwable throwable) {
        warn(label, message, throwable);
    }

    public static void error(String label, String message) {
        out("ERROR", label, message, true);
    }

    public static void error(String label, String message, Throwable throwable) {
        out("ERROR", label, message, true);
        for (StackTraceElement element : throwable.getStackTrace()) {
            out("ERROR", label, element.toString(), true);
        }
    }

    public static void e(String label, String message) {
        error(label, message);
    }

    public static void e(String label, String message, Throwable throwable) {
        error(label, message, throwable);
    }

    public static void severe(String label, String message) {
        out("SEVERE", label, message, true);
    }

    public static void severe(String label, String message, Throwable throwable) {
        out("SEVERE", label, message, true);
        for (StackTraceElement element : throwable.getStackTrace()) {
            out("SEVERE", label, element.toString(), true);
        }
    }

    public static void s(String label, String message) {
        severe(label, message);
    }

    public static void s(String label, String message, Throwable throwable) {
        severe(label, message, throwable);
    }
}
