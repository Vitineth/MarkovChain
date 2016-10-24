package com.github.vitineth.mkvc.util.time;

import com.github.vitineth.mkvc.util.output.Logger;

import java.util.concurrent.TimeUnit;

/**
 * A timer tool to measure a single task.
 * <br>
 * Created by Ryan on 22/10/2016.
 */
public class TimerTool {

    /**
     * The time at which the task started
     */
    private long startTime;
    /**
     * The time at which the task stopped
     */
    private long endTime;
    /**
     * The difference between the start and end times or how long the task took to complete
     */
    private long difference;
    /**
     * The precision at which the output should be given
     */
    private Precision precision;
    /**
     * The name of the task being measured
     */
    private String task = "The requested task";

    public enum Precision {
        SECOND(TimeUnit.SECONDS), MILLISECOND(TimeUnit.MILLISECONDS), NANOSECOND(TimeUnit.NANOSECONDS);

        private TimeUnit timeUnit;

        Precision(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }
    }

    public TimerTool(Precision precision) {
        this.precision = precision;
    }

    public TimerTool(Precision precision, String task) {
        this.precision = precision;
        this.task = task;
    }

    /**
     * Starts the given task by setting the starting {@link System#nanoTime()}.
     */
    public void start() {
        startTime = System.nanoTime();
    }

    /**
     * Ends the given task by setting the end time and calculating the difference. This function returns this instance
     * which means that another call can be chained onto it.
     *
     * @return TimerTool this instance
     */
    public TimerTool end() {
        endTime = System.nanoTime();
        difference = endTime - startTime;
        return this;
    }

    /**
     * Returns the output of task in the form <code>[TASK] completed in [TIME] [PRECISION].</code>
     *
     * @return String the output of the task.
     */
    public String getOutput() {
        long prec = precision.getTimeUnit().convert(difference, TimeUnit.NANOSECONDS);
        return this.task + " completed in '" + prec + "' " + precision + ".";
    }

    /**
     * Prints the output produced by {@link #getOutput()} to the logger using {@link Logger#i(String, String)} using
     * 'Timer' plus the hash code as the label (meaning more than one timer can be differentiated.
     */
    public void printOutput() {
        Logger.i("Timer[" + hashCode() + "]", getOutput());
    }

    /**
     * Resets the timer tool by setting the start, end and difference times to 0 before returning this instance allowing
     * another call to be chained onto it.
     *
     * @return TimeTool this instance
     */
    public TimerTool reset() {
        startTime = 0;
        endTime = 0;
        difference = 0;
        return this;
    }
}
