package org.occidere.instagramdump.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@RestController
public class BatchController {
	private Logger logger = LoggerFactory.getLogger(BatchController.class);
	private Logger errorLogger = LoggerFactory.getLogger("errorLogger");

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job job;

	@GetMapping(value = "/launchJob")
	public String launchJob(
			@RequestParam(value = "url") String url, // url 없으면 에러
			@RequestParam(value = "dateRange", defaultValue = "1") int dateRange) {

		StopWatch stopWatch = new StopWatch("InstagramDump");
		stopWatch.start("launchJob");

		logger.info("Job Started!");
		logger.info("url: {}, dateRange: {}", url, dateRange);

		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("timestamp", System.currentTimeMillis()) // jobId 대용
				.addString("url", url)
				.addString("dateRange", "" + dateRange)
				.toJobParameters();

		JobExecution jobExecution;
		String status;

		try {
			jobExecution = jobLauncher.run(job, jobParameters);
			status = jobExecution.getExitStatus().getExitCode();
		} catch (Exception e) {
			errorLogger.error("RestAPI job call failed", e);
			status = ExitStatus.FAILED.getExitCode();
		}

		stopWatch.stop();
		long elapsed = stopWatch.getTotalTimeMillis();

		logger.info("Exit Status: {}", status);
		logger.info("Elapsed Time: {} ms", elapsed);
		logger.info(stopWatch.toString());
		return String.format("Exit Status: %s, Elapsed: %,3d ms", status, elapsed);
	}
}
