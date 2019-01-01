package org.occidere.instagramdump.batch;

import org.occidere.instagramdump.domain.InstagramPhoto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author occidere
 * @since 2019-01-01
 * Blog: https://blog.naver.com/occidere
 * Github: https://github.com/occidere
 */
@Repository
public interface InstagramRepository extends MongoRepository<InstagramPhoto, String> {
	InstagramPhoto findByPageUrl(String pageUrl);
	List<InstagramPhoto> findAllByDateBetween(String fromInclude, String toExclude);
}
