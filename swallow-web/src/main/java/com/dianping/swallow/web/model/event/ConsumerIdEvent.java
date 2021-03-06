package com.dianping.swallow.web.model.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.util.StringUtils;

import com.dianping.swallow.web.manager.AlarmReceiverManager.AlarmReceiver;
import com.dianping.swallow.web.model.alarm.AlarmMeta;
import com.dianping.swallow.web.model.alarm.AlarmType;
import com.dianping.swallow.web.model.alarm.RelatedType;
import com.dianping.swallow.web.util.DateUtil;

/**
 * 
 * @author qiyin
 *
 *         2015年8月3日 上午11:10:45
 */
public class ConsumerIdEvent extends TopicEvent {

	private static final Map<String, AlarmRecord> lastAlarms = new ConcurrentHashMap<String, AlarmRecord>();

	private String consumerId;

	public String getConsumerId() {
		return consumerId;
	}

	public ConsumerIdEvent setConsumerId(String consumerId) {
		this.consumerId = consumerId;
		return this;
	}

	@Override
	public String toString() {
		return "ConsumerIdEvent [consumerId=" + consumerId + super.toString() + "]";
	}

	@Override
	public void alarm() {
		switch (getEventType()) {
		case CONSUMER:
			switch (getStatisType()) {
			case SENDQPS_PEAK:
				sendMessage(AlarmType.CONSUMER_CONSUMERID_SENDQPS_PEAK);
				break;
			case SENDQPS_VALLEY:
				sendMessage(AlarmType.CONSUMER_CONSUMERID_SENDQPS_VALLEY);
				break;
			case SENDQPS_FLU:
				sendMessage(AlarmType.CONSUMER_CONSUMERID_SENDQPS_FLUCTUATION);
				break;
			case SENDDELAY:
				sendMessage(AlarmType.CONSUMER_CONSUMERID_SENDMESSAGE_DELAY);
				break;
			case SENDACCU:
				sendMessage(AlarmType.CONSUMER_CONSUMERID_SENDMESSAGE_ACCUMULATION);
				break;
			case ACKQPS_PEAK:
				sendMessage(AlarmType.CONSUMER_CONSUMERID_ACKQPS_PEAK);
				break;
			case ACKQPS_VALLEY:
				sendMessage(AlarmType.CONSUMER_CONSUMERID_ACKQPS_VALLEY);
				break;
			case ACKQPS_FLU:
				sendMessage(AlarmType.CONSUMER_CONSUMERID_ACKQPS_FLUCTUATION);
				break;
			case ACKDELAY:
				sendMessage(AlarmType.CONSUMER_CONSUMERID_ACKMESSAGE_DELAY);
			default:
				break;
			}
		default:
			break;
		}
	}

	@Override
	public String getMessage(String template) {
		String message = template;
		if (StringUtils.isNotBlank(message)) {
			message = StringUtils.replace(message, AlarmMeta.TOPIC_TEMPLATE, getTopicName());
			message = StringUtils.replace(message, AlarmMeta.CONSUMERID_TEMPLATE, getConsumerId());
			message = StringUtils.replace(message, AlarmMeta.CURRENTVALUE_TEMPLATE, Long.toString(getCurrentValue()));
			message = StringUtils.replace(message, AlarmMeta.EXPECTEDVALUE_TEMPLATE, Long.toString(getExpectedValue()));
			message = StringUtils.replace(message, AlarmMeta.DATE_TEMPLATE, DateUtil.getDefaulFormat());
		}
		return message;
	}

	@Override
	public String getRelated() {
		return getTopicName();
	}

	@Override
	protected String getSubRelated() {
		return consumerId;
	}

	@Override
	public boolean isSendAlarm(AlarmType alarmType, AlarmMeta alarmMeta) {
		String key = getTopicName() + KEY_SPLIT + getConsumerId() + KEY_SPLIT + alarmType.getNumber();
		return isAlarm(lastAlarms, key, alarmMeta);
	}

	@Override
	public AlarmReceiver getRelatedReceiver() {
		return this.receiverManager.getAlarmReceiverByConsumerId(getTopicName(), consumerId);
	}

	@Override
	public RelatedType getRelatedType() {
		switch (getEventType()) {
		case CONSUMER:
			return RelatedType.C_CONSUMERID;
		default:
			break;
		}
		return null;
	}
}
