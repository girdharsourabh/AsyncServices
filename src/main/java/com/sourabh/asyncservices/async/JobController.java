package com.sourabh.asyncservices.async;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asyncjobs")
public class JobController {

    private final JobService jobService;
    private final JobLauncher jobLauncher;

    public JobController(JobService jobService, JobLauncher jobLauncher) {
        this.jobService = jobService;
        this.jobLauncher = jobLauncher;
    }

    private static String jobStartedMessage(int id) {
        return "Job started for id :: " + id;
    }

    private static String alreadyProcessingMessage(int id) {
        return "Already Processing job with id: " + id;
    }

    @GetMapping("/{id}")
    public String getJobStatus(@PathVariable int id) {
        return jobLauncher.getExecutorById(id).map(JobLauncher.JobExecutor::getStatus)
                .orElse("Job with id: " + id + " not found");
    }

    @PostMapping("/async")
    public String startJobAsync(@RequestBody JobRequest request) {
        int id = request.jobId();

        if (jobLauncher.getExecutorById(id).isPresent())
            return alreadyProcessingMessage(id);

        jobLauncher.runAsync(id, () -> jobService.longRunningJob(request));
        return jobStartedMessage(id);
    }
}
