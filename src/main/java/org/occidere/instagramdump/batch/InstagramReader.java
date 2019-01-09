package org.occidere.instagramdump.batch;

import org.occidere.instagramdump.common.InstagramCrawler;
import org.occidere.instagramdump.domain.InstagramPhoto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;

import java.util.Iterator;


/**
 * @author occidere
 * @since 2018-12-31
 * Blog: https://blog.naver.com/occidere
 * Github: https://github.com/occidere
 */
@ComponentScan(basePackages = "org.occidere.instagramdump")
public class InstagramReader implements ItemReader<InstagramPhoto>, StepExecutionListener {
	private Logger log = LoggerFactory.getLogger(InstagramReader.class);

	@Autowired
	private InstagramCrawler crawler;
	private Iterator<InstagramPhoto> iterator;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		log.info("Instagram 다운로드 시작!");
		iterator = crawler.getResult().iterator();
		log.info("Instagram 다운로드 완료!");
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public InstagramPhoto read() throws Exception {
		return iterator.hasNext() ? iterator.next() : null;
	}
}
