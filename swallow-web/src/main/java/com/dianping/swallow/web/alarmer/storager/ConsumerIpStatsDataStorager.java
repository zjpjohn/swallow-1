package com.dianping.swallow.web.alarmer.storager;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.dianping.swallow.common.internal.util.EnvUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.swallow.web.model.stats.ConsumerIpStatsData;
import com.dianping.swallow.web.service.ConsumerIpStatsDataService;
import com.dianping.swallow.web.util.CountDownLatchUtil;

/**
 * @author qiyin
 *         <p/>
 *         2015年10月14日 下午8:12:29
 */
@Component
public class ConsumerIpStatsDataStorager extends AbstractConsumerStatsDataStorager {

    @Autowired
    private ConsumerIpStatsDataService ipStatsDataService;

    @Override
    protected void doStorage() {
        if (!EnvUtil.isQa()) {
            doStorageIpStats();
        }
    }

    private void doStorageIpStats() {
        logger.info("[doStorageIpStats].");
        Set<String> topicNames = consumerStatsDataWapper.getTopics(false);
        if (topicNames == null) {
            return;
        }
        for (String topicName : topicNames) {
            Set<String> consumerIds = consumerStatsDataWapper.getConsumerIds(topicName, false);
            if (consumerIds == null) {
                continue;
            }
            final CountDownLatch downLatch = CountDownLatchUtil.createCountDownLatch(consumerIds.size());
            for (String consumerId : consumerIds) {
                final List<ConsumerIpStatsData> ipStatsDatas = consumerStatsDataWapper.getIpStatsDatas(topicName,
                        consumerId, getLastTimeKey(), false);
                if (ipStatsDatas == null) {
                    downLatch.countDown();
                    continue;
                }
                try {
                    taskManager.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ipStatsDataService.insert(ipStatsDatas);
                            } catch (Throwable t) {
                                logger.error("[doStorageIpStats] insert ipstatsDatas.", t);
                            } finally {
                                downLatch.countDown();
                            }
                        }
                    });
                } catch (Throwable t) {
                    logger.error("[doStorageIpStats] executor submit error.", t);
                } finally {
                    downLatch.countDown();
                }
            }
            CountDownLatchUtil.await(downLatch);
        }

    }

}
