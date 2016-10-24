package com.github.vitineth.mkvc.util.time;

import com.github.vitineth.mkvc.util.output.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A timer tool that breaks a task into parts and measures each one individually. Each task is stored and will be
 * outputted as a block either to a string or to the console using the {@link Logger#info(String, String)} from the
 * Logger.
 * <br><br>
 * Created by Ryan on 22/10/2016.
 */
public class SegmentedTimerTool {

    /**
     * The list of all timer regions that have been lapped so far by the timer
     */
    private List<TimerRegion> regionList;
    /**
     * The nanosecond value of the time the timer was stopped completely
     */
    private long finalEnd;
    /**
     * The precision to which the output should be displayed
     */
    private TimerTool.Precision precision;
    /**
     * The region currently being measured
     */
    private TimerRegion activeRegion;

    /**
     * Creates a timer tool with the given precision
     *
     * @param precision Precision the precision to which the output should be given. The total time will always be
     *                  printed in seconds
     */
    public SegmentedTimerTool(TimerTool.Precision precision) {
        regionList = new ArrayList<>();
        this.precision = precision;
        activeRegion = new TimerRegion(0, 0);
    }

    /**
     * Starts the active timer region ({@link #lap(String)} should be called before this if start has already been
     * called)
     */
    public void start() {
        activeRegion.setStartTime(System.nanoTime());
    }

    /**
     * Laps the current time with the name 'An unlabelled task' as the name. This is a wrapper for {@link #lap(String)}
     *
     * @see #lap(String)
     */
    public void lap() {
        lap("An unlabelled task");
    }

    /**
     * Laps the given task with the given task name. It will reset the timer region back to 0 ready to be used again.
     *
     * @param task String the task name that was measured
     */
    public void lap(String task) {
        activeRegion.setEndTime(System.nanoTime());
        activeRegion.setTask(task);
        regionList.add(activeRegion);
        activeRegion = new TimerRegion(0, 0);
    }

    /**
     * Concludes the entire timer task by setting the final end time.
     */
    public void end() {
        finalEnd = System.nanoTime();
    }

    /**
     * Prints all the statistics of the timer to the console in the form:
     * <p>
     * <code>
     * Segmented Timer Statistics: <br>
     * |-- '[TASK]' completed in '[TIME]' [PRECISION].<br>
     * |-- '[TASK]' completed in '[TIME]' [PRECISION].<br>
     * |-- '[TASK]' completed in '[TIME]' [PRECISION].<br>
     * |-- Total timer task completed in '[TIME]' seconds.<br>
     * |--------------------->
     * </code>
     */
    public void printStatistics() {
        Logger.i("SegmentedTimerTool[" + hashCode() + "]", "Segmented Timer Statistics:");
        for (TimerRegion region : regionList) {
            Logger.i("SegmentedTimerTool[" + hashCode() + "]", "|-- '" + region.getTask() + "' completed in '" + precision.getTimeUnit().convert(region.getDifference(), TimeUnit.NANOSECONDS) + "' milliseconds.");
        }
        Logger.i("SegmentedTimerTool[" + hashCode() + "]", "|-- Total timer task completed in '" + TimeUnit.SECONDS.convert(finalEnd - regionList.get(0).getStartTime(), TimeUnit.NANOSECONDS) + "' seconds.");
        Logger.i("SegmentedTimerTool[" + hashCode() + "]", "|--------------------->");
    }

    /**
     * Returns the timer statistics in the same form as printed by {@link #printStatistics()} but as a plain string.
     * <h4>Form</h4>
     * <p>
     * <code>
     * Segmented Timer Statistics: <br>
     * |-- '[TASK]' completed in '[TIME]' [PRECISION].<br>
     * |-- '[TASK]' completed in '[TIME]' [PRECISION].<br>
     * |-- '[TASK]' completed in '[TIME]' [PRECISION].<br>
     * |-- Total timer task completed in '[TIME]' seconds.<br>
     * |--------------------->
     * </code>
     *
     * @return The timer statistics
     */
    public String getStatistics() {
        StringBuilder builder = new StringBuilder();

        builder.append("Segmented Timer Statistics:");
        for (TimerRegion region : regionList) {
            builder.append("|-- '").append(region.getTask()).append("' completed in '").append(precision.getTimeUnit().convert(region.getDifference(), TimeUnit.NANOSECONDS)).append("' milliseconds.");
        }
        builder.append("|-- Total timer task completed in '").append(TimeUnit.SECONDS.convert(finalEnd - regionList.get(0).getStartTime(), TimeUnit.NANOSECONDS)).append("' seconds.");
        builder.append("|--------------------->");

        return builder.toString();
    }

}

/**
 * A single timer region or single measured task.
 */
class TimerRegion {

    private long startTime;
    private long endTime;
    private long difference;
    private String task;

    /**
     * Creates a time task with the given start and end times and will calculate the difference automatically.
     *
     * @param start long the start time
     * @param end   long the end time
     */
    public TimerRegion(long start, long end) {
        this.startTime = start;
        this.endTime = end;
        this.difference = end - start;
    }

    /**
     * Returns the difference between the start and the end time.
     *
     * @return long the difference between the start and the end time.
     */
    public long getDifference() {
        return difference;
    }

    /**
     * Returns the time at which the region was started
     *
     * @return long the time at which the region was started
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the time at which the region was ended
     *
     * @return long the time at which the region was ended
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Returns the name of the task
     *
     * @return String the name of the task
     */
    public String getTask() {
        return task;
    }

    /**
     * Sets the time at which the region started
     *
     * @param startTime long the time at which the region should be said to have started
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Sets the itme at which the region ended and recalculates the difference
     *
     * @param endTime long the time at which the region should be said to have ended
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
        setDifference(endTime - startTime);
    }

    /**
     * Sets the difference between the start and the end times (but does not alter the start or end times)
     *
     * @param difference long the new difference between the times
     */
    public void setDifference(long difference) {
        this.difference = difference;
    }

    /**
     * Sets the name of the task
     *
     * @param task String the new name of the task
     */
    public void setTask(String task) {
        this.task = task;
    }
}
