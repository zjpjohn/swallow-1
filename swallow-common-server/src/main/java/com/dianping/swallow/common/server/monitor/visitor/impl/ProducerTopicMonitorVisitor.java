package com.dianping.swallow.common.server.monitor.visitor.impl;


import com.dianping.swallow.common.server.monitor.data.MonitorData.MessageInfo;
import com.dianping.swallow.common.server.monitor.data.ProducerData;
import com.dianping.swallow.common.server.monitor.data.structure.TotalMap;

/**
 * @author mengwenchao
 *
 * 2015年4月22日 下午5:18:40
 */
public class ProducerTopicMonitorVisitor extends AbstractProducerMonitorVisitor {

	public ProducerTopicMonitorVisitor(String topic) {
		super(topic);
	}

	@Override
	public void visitTopic(@SuppressWarnings("rawtypes") TotalMap visitorData) {
		
		ProducerData producerData = (ProducerData)visitorData;

		MessageInfo info = new MessageInfo();
		if(producerData != null){
			info = producerData.getTotal();
		}
		allRawData.add(info);
	}

}