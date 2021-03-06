package com.dianping.swallow.web.model.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.swallow.web.manager.AlarmReceiverManager.AlarmReceiver;
import com.dianping.swallow.web.model.alarm.AlarmType;
import com.dianping.swallow.web.model.alarm.RelatedType;

public abstract class ClientEvent extends Event {

	protected static final Map<String, AlarmRecord> lastAlarms = new ConcurrentHashMap<String, AlarmRecord>();

	private String topicName;

	private String ip;

	private ClientType clientType;

	public String getTopicName() {
		return topicName;
	}

	public ClientEvent setTopicName(String topicName) {
		this.topicName = topicName;
		return this;
	}

	public String getIp() {
		return ip;
	}

	public ClientEvent setIp(String ip) {
		this.ip = ip;
		return this;
	}

	@Override
	public void alarm() {
		switch (getEventType()) {
		case CONSUMER:
			switch (getClientType()) {
			case CLIENT_RECEIVER:
				sendMessage(AlarmType.CONSUMER_CLIENT_RECEIVER);
				break;
			default:
				break;
			}
			break;
		case PRODUCER:
			switch (getClientType()) {
			case CLIENT_SENDER:
				sendMessage(AlarmType.PRODUCER_CLIENT_SENDER);
				break;
			default:
				break;
			}
			break;
		}
	}

	@Override
	public String getRelated() {
		return ip;
	}

	@Override
	public RelatedType getRelatedType() {
		switch (getEventType()) {
		case PRODUCER:
			return RelatedType.P_IP;
		case CONSUMER:
			return RelatedType.C_IP;
		}
		return null;
	}

	@Override
	public AlarmReceiver getRelatedReceiver() {
		return this.receiverManager.getAlarmReceiverByIp(ip);
	}

	public ClientType getClientType() {
		return clientType;
	}

	public ClientEvent setClientType(ClientType clientType) {
		this.clientType = clientType;
		return this;
	}

	@Override
	public String toString() {
		return "ClientEvent [topicName=" + topicName + ", ip=" + ip + ", clientType=" + clientType + super.toString()
				+ "]";
	}

}
