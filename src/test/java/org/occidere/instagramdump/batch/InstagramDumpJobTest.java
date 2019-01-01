package org.occidere.instagramdump.batch;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author occidere
 * @since 2018-12-31
 * Blog: https://blog.naver.com/occidere
 * Github: https://github.com/occidere
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/application-test.xml", "classpath:/job/instagramDumpJob.xml" })
public class InstagramDumpJobTest {
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Test
	public void runJobTest() throws Exception {
		JobParametersBuilder builder = new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters())
				.addString("threadSize", "1")
				.addString("dateRange", "1")
				.addString("url", "https://www.instagram.com/twicetagram/?hl=ko");
		JobParameters jobParameters = builder.toJobParameters();

		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
}
