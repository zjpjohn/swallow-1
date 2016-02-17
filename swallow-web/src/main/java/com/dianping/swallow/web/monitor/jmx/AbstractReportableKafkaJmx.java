package com.dianping.swallow.web.monitor.jmx;

import com.dianping.swallow.web.alarmer.EventReporter;
import com.dianping.swallow.web.model.event.Event;
import com.dianping.swallow.web.model.event.EventFactory;
import com.dianping.swallow.web.monitor.jmx.event.KafkaEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author   mingdongli
 * 16/2/2  下午3:42.
 */
public abstract class AbstractReportableKafkaJmx implements ReportableKafkaJmx {

    @Autowired
    private EventReporter eventReporter;

    @Autowired
    protected EventFactory eventFactory;

    @Override
    public void report(Event event) {
        eventReporter.report(event);
    }
//
//    protected BrokerKafkaEvent createEvent() {
//
//        return eventFactory.createBrokerKafkaEvent();
//    }

    abstract protected int getInterval();

    abstract protected int getDelay();

    abstract protected KafkaEvent createEvent();

}
