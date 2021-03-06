package com.dianping.swallow.common.internal.dao.impl.mongodb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.swallow.common.internal.config.SwallowServerConfig;
import com.dianping.swallow.common.internal.config.impl.AbstractSwallowServerConfig;
import com.dianping.swallow.common.internal.config.impl.SwallowConfigDistributed;
import com.dianping.swallow.common.internal.dao.impl.AbstractDbTest;
import com.dianping.swallow.common.internal.util.EnvUtil;
import com.mongodb.CommandResult;
import com.mongodb.DBCollection;

/**
 * @author mengwenchao
 *
 * 2015年2月4日 下午4:38:10
 */
public class MongoClusterTest extends AbstractDbTest{
	
	
	private MongoCluster mongoCluster;

	private String []topics = new String[]{"topic1", "topic2", "topic3"};
	
	
	/**
		swallow.topiccfg.default={"mongoUrl":"mongodb://192.168.213.143:27018","size":100,"max":100}
		swallow.topiccfg.topic1={"size":200,"max":200}
		swallow.topiccfg.topic2={}
		swallow.topiccfg.topic3={"mongoUrl":"mongodb://192.168.213.143:27118","size":101,"max":102}	 
		* @throws Exception
	 */
	@Before
	public void beforeDefaultMongoManagerTest() throws Exception{
		
		System.setProperty("SWALLOW.STORE.LION.CONFFILE", "swallow-mongo-createmongo.properties");
		
		mongoCluster = new MongoCluster(new MongoConfig("swallow-mongo.properties").buildMongoOptions(), getMongoAddress());
		
		mongoCluster.setSwallowServerConfig(createSwallowConfig());
		mongoCluster.initialize();
		
		for(String topic : topics){
			mongoCluster.cleanMessageCollection(topic, null);
		}
	}
	
	private SwallowServerConfig createSwallowConfig() throws Exception {
		
		SwallowServerConfig config = new SwallowConfigDistributed();
		config.initialize();
		
		return config;
	}

	@Test
	public void testCreateMongo(){
		if(!EnvUtil.isDev()){
			return;
		}
		
		DBCollection collection1 = mongoCluster.getMessageCollection("topic1");
		checkOk(collection1, 200, 200);
		
		DBCollection collection2 = mongoCluster.getMessageCollection("topic2");
		checkOk(collection2, 100, 100);
		
		
		
	}

	private void checkOk(DBCollection col, int size, int max) {
		
		Assert.assertTrue(col.isCapped());
		
		CommandResult result = col.getStats();
		long realSize = result.getLong("storageSize");
		long realMax = result.getLong("max");

		Assert.assertTrue((realSize / (size * AbstractSwallowServerConfig.MILLION)) == 1 );
		Assert.assertTrue((realMax / (max * AbstractSwallowServerConfig.MILLION)) == 1 );
		
		Assert.assertEquals(ajustExpectedSize(size * AbstractSwallowServerConfig.MILLION), realSize);
		Assert.assertEquals(max * AbstractSwallowServerConfig.MILLION, realMax);
	}

	private Object ajustExpectedSize(long size) {
		
		return ((size / 4096) + 1) *4096;
	}

}
