package com.dianping.swallow.web.model.stats;


import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 
 * @author qiyin
 *
 *         2015年9月6日 上午9:46:03
 */
@Document(collection = "CONSUMER_IP_STATS_DATA")
@CompoundIndexes({ @CompoundIndex(name = "IX_TOPICNAME_CONSUMERID_IP_TIMEKEY", def = "{'topicName':-1, 'consumerId': -1, 'ip': -1, 'timeKey': 1}") })
public class ConsumerIpStatsData extends ConsumerStatsData {

	private String topicName;

	private String consumerId;

	private String ip;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}
	
	public boolean checkStatsData() {
		if (this.getSendQps() == 0L && this.getAckQps() == 0L) {
			return false;
		}
		return true;
	}
	
	public boolean hasStatsData(){
		if (this.getSendQps() == 0L && this.getAckQps() == 0L) {
			return false;
		}
		return true;
	}

}
