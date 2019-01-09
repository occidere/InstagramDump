package org.occidere.instagramdump.batch;

import org.occidere.instagramdump.common.InstagramCrawler;
import org.occidere.instagramdump.domain.InstagramPhoto;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration extends DefaultBatchConfigurer {
	@Override
	public void setDataSource(DataSource dataSource) {
	}

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job instagramDumpJob(Step instagramDumpStep) {
		return jobBuilderFactory.get("instagramDumpJob")
				.incrementer(new RunIdIncrementer())
				.flow(instagramDumpStep)
				.end()
				.build();
	}

	@Bean
	public Step instagramDumpStep() {
		return stepBuilderFactory.get("instagramDumpStep")
				.transactionManager(dummyTransactionManager())
				.<InstagramPhoto, InstagramPhoto>chunk(1)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}

	@Bean
	public ResourcelessTransactionManager dummyTransactionManager() {
		return new ResourcelessTransactionManager();
	}

	@Bean
	@StepScope
	public InstagramCrawler instagramCrawler(
			@Value("#{jobParameters[url]}") String url,
			@Value("#{jobParameters[dateRange] == null ? 1 : jobParameters[dateRange]}") int dateRange) {
		InstagramCrawler crawler = new InstagramCrawler();
		crawler.setUrl(url);
		crawler.setRange(dateRange);
		return crawler;
	}

	@Bean
	public ItemReader<InstagramPhoto> reader() {
		return new InstagramReader();
	}

	@Bean
	public ItemProcessor<InstagramPhoto, InstagramPhoto> processor() {
		return new InstagramProcessor();
	}

	@Bean
	public ItemWriter<InstagramPhoto> writer() {
		return new InstagramWriter();
	}


}
