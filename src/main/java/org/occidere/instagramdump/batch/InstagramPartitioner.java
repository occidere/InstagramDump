package org.occidere.instagramdump.batch;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author occidere
 * @since 2018-12-31
 * Blog: https://blog.naver.com/occidere
 * Github: https://github.com/occidere
 */
public class InstagramPartitioner implements Partitioner {
	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Map<String, ExecutionContext> map = new HashMap<>(gridSize);
		for (int i = 0; i < gridSize; i++) {
			ExecutionContext context = new ExecutionContext();
			context.put("partitionNum", String.valueOf(i));
			map.put(String.format("partition-%02d", i), context);
		}
		return map;
	}
}
