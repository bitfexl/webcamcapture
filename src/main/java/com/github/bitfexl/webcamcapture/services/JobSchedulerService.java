package com.github.bitfexl.webcamcapture.services;

import com.github.bitfexl.webcamcapture.util.DurationParser;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@ApplicationScoped
public class JobSchedulerService {
    private static class ScheduledJob {
        Instant nextExecution;

        long intervalMs;

        Runnable runnable;
    }

    private final Executor executor = Executors.newSingleThreadExecutor();

    private final List<ScheduledJob> jobs = new ArrayList<>();

    /**
     * Add a scheduled job. Jobs are only checked every 10 seconds.
     * @param interval The interval. A number followed by s, m or h for seconds, minutes or hours.
     * @param job The job to schedule.
     */
    public void addJob(String interval, Runnable job) {
        addJob(DurationParser.parseDuration(interval), job);
    }

    /**
     * Add a scheduled job. Jobs are only checked every 10 seconds.
     * @param intervalMs The interval in milliseconds.
     * @param jobRunnable The job to schedule.
     */
    public synchronized void addJob(long intervalMs, Runnable jobRunnable) {
        final ScheduledJob job = new ScheduledJob();
        job.runnable = jobRunnable;
        job.intervalMs = intervalMs;
        job.nextExecution = Instant.ofEpochSecond(0); // execute on next check
        jobs.add(job);
    }

    @Scheduled(every = "10s")
    synchronized void checkScheduledJobs() {
        final Instant checkTime = Instant.now();
        for (ScheduledJob job : jobs) {
            if (job.nextExecution.isBefore(checkTime)) {
                executor.execute(job.runnable);
                job.nextExecution = checkTime.plus(job.intervalMs, ChronoUnit.MILLIS);
            }
        }
    }
}
