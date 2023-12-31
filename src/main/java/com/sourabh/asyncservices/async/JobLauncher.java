package com.sourabh.asyncservices.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
@Component
public class JobLauncher {

    private static final Map<Integer, JobExecutor> jobExecutors = new ConcurrentHashMap<>(); //1

    @Async
    public void runAsync(int id, Supplier<Integer> supplier) {
        JobExecutor jobExecutor = new JobExecutor(id, supplier);
        jobExecutors.putIfAbsent(id, jobExecutor);
        jobExecutor.run();
    }

    public Optional<JobExecutor> getExecutorById(int id) {
        return Optional.ofNullable(jobExecutors.get(id));
    }


    public static class JobExecutor {
        private final int id;
        private final Supplier<Integer> supplier;
        private String status;
        private Future<Integer> cf1;

        private JobExecutor(final int id, final Supplier<Integer> supplier) {
            this.id = id;
            this.supplier = supplier;
        }

        private void run() {
            status = "Started";
            Integer result = supplier.get();
            cf1 = new AsyncResult<>(result);
        }

        public String getStatus() {
            if (cf1 != null && cf1.isDone()) {
                log.info("Status is finished :: " + Thread.currentThread().getName());
                status = "Job with id: " + id + "finished with result " + safeGetFuture();
                jobExecutors.remove(id);
            }
            return status;
        }

        private Integer safeGetFuture() {
            Integer result = -1;
            try {
                result = cf1.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                log.error("SafeGetFuture threw " + e);
            } catch (InterruptedException e) {
                log.error("Future timed out before completing");
                Thread.currentThread().interrupt();
            }
            return result;
        }
    }
}
