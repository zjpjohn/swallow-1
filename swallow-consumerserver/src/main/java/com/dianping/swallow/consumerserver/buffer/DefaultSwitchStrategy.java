package com.dianping.swallow.consumerserver.buffer;

import com.dianping.swallow.consumerserver.buffer.impl.MessageRingBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 切换回topic缓存
 *
 * @author qi.yin
 *         2016/03/03  上午11:13.
 */
public class DefaultSwitchStrategy implements SwitchStrategy {

    private final Logger logger = LogManager.getLogger(getClass());

    private volatile long lastTrySwitchMillis = System.currentTimeMillis();

    private int minSwitchInterval;//ms

    private int maxSwitchInterval;//ms

    private volatile long retrySwitchCount;

    private int switchTimeUnit;//min

    private volatile boolean isOverBuffer = false;

    private volatile long lastOverBufferMillis;

    public DefaultSwitchStrategy() {

    }

    public DefaultSwitchStrategy(int minSwitchInterval, int maxSwitchInterval, int switchTimeUnit) {
        this.minSwitchInterval = minSwitchInterval;
        this.maxSwitchInterval = maxSwitchInterval;
        this.switchTimeUnit = switchTimeUnit;
    }


    public boolean isSwitch() {

        if (isOverBuffer) {
            if (System.currentTimeMillis() - lastOverBufferMillis > minSwitchInterval) {
                return true;
            }
            return false;
        }

        long tempMaxInterval = minSwitchInterval + switchTimeUnit * retrySwitchCount;

        if (tempMaxInterval > maxSwitchInterval) {
            tempMaxInterval = maxSwitchInterval;
        }
        if (System.currentTimeMillis() - lastTrySwitchMillis > tempMaxInterval) {
            return true;
        }
        return false;
    }

    public void switched(MessageRingBuffer.ReaderStatus status) {

        if (status.isOpen()) {
            retrySwitchCount = 0;
            isOverBuffer = false;
        } else if (status.isClosedOver()) {
            lastOverBufferMillis = System.currentTimeMillis();
            isOverBuffer = true;
            retrySwitchCount = 0;
        } else {
            lastTrySwitchMillis = System.currentTimeMillis();
            retrySwitchCount++;
            isOverBuffer = false;
        }
    }

}
