package org.occidere.instagramdump.batch;

import org.occidere.instagramdump.domain.InstagramPhoto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

/**
 * @author occidere
 * @since 2018-12-31
 * Blog: https://blog.naver.com/occidere
 * Github: https://github.com/occidere
 */
@ComponentScan(basePackages = "org.occidere.instagramdump")
public class InstagramWriter implements ItemWriter<InstagramPhoto>, StepExecutionListener {
	private Logger logger = LoggerFactory.getLogger(InstagramWriter.class);

	@Autowired
	private InstagramRepository repository;

	@Override
	public void beforeStep(StepExecution stepExecution) {

	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public void write(List<? extends InstagramPhoto> items) {
		logger.info("DB 저장 시작 ({} 건)", items.size());
		for (InstagramPhoto photo : items) {
			InstagramPhoto result = repository.save(photo);
		}
	}
}
