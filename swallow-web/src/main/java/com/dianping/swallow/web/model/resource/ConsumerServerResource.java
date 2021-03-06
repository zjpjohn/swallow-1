package com.dianping.swallow.web.model.resource;

import org.springframework.data.mongodb.core.mapping.Document;

import com.dianping.swallow.web.model.alarm.QPSAlarmSetting;

/**
 * @author mingdongli
 *
 *         2015年8月10日下午2:44:55
 */
@Document(collection = "CONSUMER_SERVER_RESOURCE")
public class ConsumerServerResource extends ServerResource {

	private int port;

	private ServerType type;

	private int groupId;

	private long qps;

	private boolean isQpsAlarm;
	
	private QPSAlarmSetting sendAlarmSetting;

	private QPSAlarmSetting ackAlarmSetting;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ServerType getType() {
		return type;
	}

	public void setType(ServerType type) {
		this.type = type;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public QPSAlarmSetting getSendAlarmSetting() {
		return sendAlarmSetting;
	}

	public void setSendAlarmSetting(QPSAlarmSetting sendAlarmSetting) {
		this.sendAlarmSetting = sendAlarmSetting;
	}

	public QPSAlarmSetting getAckAlarmSetting() {
		return ackAlarmSetting;
	}

	public void setAckAlarmSetting(QPSAlarmSetting ackAlarmSetting) {
		this.ackAlarmSetting = ackAlarmSetting;
	}

	public long getQps() {
		return qps;
	}

	public void setQps(long qps) {
		this.qps = qps;
	}

	public boolean isQpsAlarm() {
		return isQpsAlarm;
	}

	public void setIsQpsAlarm(boolean isQpsAlarm) {
		this.isQpsAlarm = isQpsAlarm;
	}

	@Override
	public String toString() {
		return "ConsumerServerResource{" +
				"port=" + port +
				", type=" + type +
				", groupId=" + groupId +
				", qps=" + qps +
				", isQpsAlarm=" + isQpsAlarm +
				", sendAlarmSetting=" + sendAlarmSetting +
				", ackAlarmSetting=" + ackAlarmSetting +
				'}';
	}
}
