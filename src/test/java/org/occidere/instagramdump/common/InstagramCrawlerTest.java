package org.occidere.instagramdump.common;

import org.junit.runner.RunWith;
import org.occidere.instagramdump.domain.InstagramPhoto;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author occidere
 * @since 2019-01-01
 * Blog: https://blog.naver.com/occidere
 * Github: https://github.com/occidere
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/application-test.xml" })
public class InstagramCrawlerTest {

	public static void main (String[] args) throws Exception {
		InstagramCrawler crawler = new InstagramCrawler();
//		crawler.setUrl("https://www.instagram.com/twicetagram/?hl=ko");
//		crawler.setRange(1);
//		crawler.setChromedriverPath("/Users/occidere/Downloads/chromedriver");
		List<InstagramPhoto> instagramPhotos = crawler.getResult();
		for (InstagramPhoto photo : instagramPhotos) {
			System.out.println(photo);
		}
	}
}
