package com.dianping.swallow.test;



import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;

import com.dianping.swallow.common.consumer.ConsumerType;
import com.dianping.swallow.common.consumer.MessageFilter;
import com.dianping.swallow.common.internal.config.SwallowServerConfig;
import com.dianping.swallow.common.internal.config.impl.SwallowConfigDistributed;
import com.dianping.swallow.common.internal.dao.ClusterFactory;
import com.dianping.swallow.common.internal.dao.ClusterManager;
import com.dianping.swallow.common.internal.dao.MessageDAO;
import com.dianping.swallow.common.internal.dao.impl.DefaultClusterManager;
import com.dianping.swallow.common.internal.dao.impl.DefaultMessageDaoFactory;
import com.dianping.swallow.common.internal.dao.impl.kafka.KafkaClusterFactory;
import com.dianping.swallow.common.internal.dao.impl.mongodb.MongoClusterFactory;
import com.dianping.swallow.common.internal.lifecycle.Lifecycle;
import com.dianping.swallow.common.message.Destination;
import com.dianping.swallow.common.message.Message;
import com.dianping.swallow.common.producer.exceptions.RemoteServiceInitFailedException;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.dianping.swallow.consumer.Consumer;
import com.dianping.swallow.consumer.ConsumerConfig;
import com.dianping.swallow.consumer.MessageListener;
import com.dianping.swallow.consumer.impl.ConsumerFactoryImpl;
import com.dianping.swallow.producer.Producer;
import com.dianping.swallow.producer.ProducerConfig;
import com.dianping.swallow.producer.ProducerMode;
import com.dianping.swallow.producer.impl.ProducerFactoryImpl;
import com.dianping.swallow.test.AbstractUnitTest;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author mengwenchao
 *
 * 2015年3月23日 下午4:27:18
 */
public abstract class AbstractSwallowTest extends AbstractUnitTest{

	private String topic = "swallow-test-integrated";

	protected ConcurrentHashMap<String, AtomicInteger> sendMessageCount = new ConcurrentHashMap<String, AtomicInteger>();

	protected ConcurrentHashMap<Consumer, AtomicInteger> getMessageCount = new ConcurrentHashMap<Consumer, AtomicInteger>();
	
	@JsonIgnore
	protected List<Consumer> consumers = new LinkedList<Consumer>();

	@JsonIgnore
	protected MessageDAO<?> mdao;

	@JsonIgnore
	private List<Lifecycle> lifecycle = new LinkedList<Lifecycle>();
	
	@Before
	public void beforeSwallowAbstractTest() throws Exception{

		topic = getTestTopic();
		
		if(logger.isInfoEnabled()){
			logger.info("[beforeSwallowAbstractTest][topic]" + topic);
		}
		
		SwallowConfigDistributed swallowServerConfig = new SwallowConfigDistributed();
		swallowServerConfig.initialize();

		ClusterManager clusterManager = createClusterManager(swallowServerConfig);
		
		if(clusterManager instanceof Lifecycle){
			lifecycle.add((Lifecycle) clusterManager);
		}
		lifecycle.add(swallowServerConfig);
		
		
		DefaultMessageDaoFactory factory = new DefaultMessageDaoFactory();
		factory.setClusterManager(clusterManager);
		factory.setSwallowServerConfig(swallowServerConfig);
		factory.initialize();
		
		lifecycle.add(factory);
		
		mdao = factory.getObject();
	}

	private String getTestTopic() {
		
		String fileName = "swallow-test.properties";
		InputStream ins = getClass().getClassLoader().getResourceAsStream(fileName);
		if(ins == null){
			return topic;
		}
		Properties properties = new Properties();
		try {
			properties.load(ins);
		} catch (IOException e) {
			logger.error("load " + fileName, e);
		}
		
		return properties.getProperty("topic.test", topic);
	}

	public static ClusterManager createClusterManager(SwallowServerConfig swallowServerConfig) throws Exception {
		
		DefaultClusterManager clusterManager = new DefaultClusterManager();
		
		clusterManager.setSwallowServerConfig(swallowServerConfig);
		
		MongoClusterFactory mongoClusterFactory = new MongoClusterFactory();
		mongoClusterFactory.initialize();

		KafkaClusterFactory kafkaClusterFactory = new KafkaClusterFactory();
		kafkaClusterFactory.initialize();

		
		List<ClusterFactory> clusterFactories = new LinkedList<ClusterFactory>();
		
		clusterFactories.add(mongoClusterFactory);
		clusterFactories.add(kafkaClusterFactory);
		
		clusterManager.setClusterFactories(clusterFactories);


		return clusterManager;
	}

	@After
	public void afterAbstratTest() throws Exception{
		for(Consumer c : consumers){
			c.close();
		}
		
		for(int i = lifecycle.size() -1 ; i >= 0; i--){
			lifecycle.get(i).dispose();
		}
		sleep(100);
	}

	
	
	
	protected Long getMaxMessageId(String topicName){
		return mdao.getMaxMessageId(topicName);
	} 
	
	protected void sendMessage(String topic, Object message, boolean zipped) throws SendFailedException, RemoteServiceInitFailedException{
		
		sendMessage(1, topic, zipped,  0, -1, null, message);
	}
		
	protected void cleanSendMessageCount() {
		sendMessageCount.clear();
	}
	
	protected void cleanGetMessageCount(){
		getMessageCount.clear();
	}

	protected void sendMessage(String topic, Object message) throws SendFailedException, RemoteServiceInitFailedException{
		
		sendMessage(1, topic, false, 0, -1, null, message);
		
	}
	
	protected void sendMessage(String topic, int messageCount, int sleepTime) throws SendFailedException, RemoteServiceInitFailedException {
		
		sendMessage(messageCount, topic, false, sleepTime, 10, null, null);

	}
	
	protected void sendMessage(int messageCount, String topic, int size) throws SendFailedException, RemoteServiceInitFailedException {
		
		sendMessage(messageCount, topic, false, 0, size, null, null);
	}
	
	protected void sendMessage(int messageCount, String topic, String type) throws SendFailedException, RemoteServiceInitFailedException {
		
		sendMessage(messageCount, topic, false, 0, 10, type, null);
	}


	protected void sendMessage(int messageCount, String topic) throws SendFailedException, RemoteServiceInitFailedException {
		
		sendMessage(messageCount, topic, false,  0, 10, null, null);
		
	}
	
	private AtomicInteger totalSend = new AtomicInteger();

	protected void sendMessage(int messageCount, String topic, boolean zipped, int sleepInterval, int size, String type, Object message) throws SendFailedException, RemoteServiceInitFailedException {

		//等待consumer建立成功
		sleep(100);

		AtomicInteger count = sendMessageCount.get(topic);
		if(count == null){
			count = new AtomicInteger();
			AtomicInteger old = sendMessageCount.putIfAbsent(topic, count); 
			if(old != null){
				count = old;
			}
		}
		
		Producer p = createProducer(topic, zipped);
		if(logger.isInfoEnabled()){
			logger.info("[sendMessage][begin]" + count.get());
		}
        for (int i = 0; i < messageCount; i++) {
        	
    		if(message == null){
    			message = getMessage(size);
    		}
            p.sendMessage(message, type);
            sleep(sleepInterval);
            count.incrementAndGet();
            if((i+1) % 100 == 0){
            	if(logger.isInfoEnabled()){
            		logger.info("[sendMessage]" + (i+1));
            	}
            }
        }
		if(logger.isInfoEnabled()){
			logger.info("[sendMessage][end]" + count.get());
		}
		
		if(logger.isInfoEnabled()){
			logger.info("[sendMessage][db data count]" + mdao.count(topic));
			logger.info("[sendMessage][min message]" + mdao.getMessagesGreaterThan(topic, null, 0L, 1));
		}
	}


	protected Object getMessage(int size) {
		
        String msg = System.currentTimeMillis() + "," + totalSend.incrementAndGet() + ",";
        msg += createMessage(size);

        return msg;
	}

	protected Producer createProducer(String topic) throws RemoteServiceInitFailedException{
		
		return createProducer(topic, false);
	}
	
	protected Producer createProducer(String topic, boolean zipped) throws RemoteServiceInitFailedException{
		
        ProducerConfig config = new ProducerConfig();
        config.setMode(ProducerMode.SYNC_MODE);
        config.setZipped(zipped);
        
        setProducerConfig(config);
        
        Producer p = ProducerFactoryImpl.getInstance().createProducer(Destination.topic(topic), config);
        
        return p;

	}

	protected void setProducerConfig(ProducerConfig config) {
		
	}

	protected int getSendMessageCount(String topic){
		AtomicInteger count = sendMessageCount.get(topic);
		if(count == null){
			return 0;
		}
		return count.intValue();
	}

	protected Consumer addListener(final String topic) {
		
		return addListener(topic, false, null, 1, null, 0);
	}

	protected Consumer addListener(String topic, String consumerId, Set<String> filters) {
	
		return addListener(topic, true, consumerId, 10, filters, 0);
	}


	protected Consumer addListener(final String topic, int concurrentCount) {
		
		return addListener(topic, false, null, concurrentCount, null, 0);
	}
	
	protected Consumer addListener(final String topic, final String consumerId, int concurrentCount) {
		
		return addListener(topic, true, consumerId, concurrentCount, null, 0);
	}

	protected Consumer addListener(final String topic, final String consumerId, int concurrentCount, int sleepTime) {
		
		return addListener(topic, true, consumerId, concurrentCount, null, sleepTime);
	}


	protected Consumer createConsumer(String topic, String consumerId){
		
		return createConsumer(topic, true, consumerId, 1, 5, null);
	}

	protected Consumer createConsumer(String topic, String consumerId, int retryCount){

		return createConsumer(topic, true, consumerId, 1, retryCount, null);
	}

	protected Consumer createConsumer(String topic, boolean durable, String consumerId, int concurrentCount, int retryCount, Set<String> filters){

        ConsumerConfig config = new ConsumerConfig();
        config.setThreadPoolSize(concurrentCount);
        config.setMessageFilter(MessageFilter.createInSetMessageFilter(filters));
        config.setDelayBaseOnBackoutMessageException(1);
        
        setConsumerConfig(config);
        
        if(!durable){
        	config.setConsumerType(ConsumerType.NON_DURABLE);
        	if(consumerId != null){
        		throw new IllegalArgumentException("consumerId should be null, but " + consumerId);
        	}
        }
       
        Consumer c = ConsumerFactoryImpl.getInstance().createConsumer(Destination.topic(topic), consumerId, config);
        
        consumers.add(c);
        return c;
	}
	
	protected void setConsumerConfig(ConsumerConfig config) {
		
		
	}
	protected Consumer addListener(final String topic, boolean durable, final String consumerId, int concurrentCount, Set<String> filters, final int sleepTime) {

		final Consumer c = createConsumer(topic, durable, consumerId, concurrentCount, 5, filters);

		c.setListener(new MessageListener() {
        	
        	AtomicInteger count;
        	{
            	count = getMessageCount.get(c);
            	if(count == null){
            		count = new AtomicInteger();
            		AtomicInteger old = getMessageCount.putIfAbsent(c, count);
            		if(old != null){
            			count = old;
            		}
            	}
        	}
            @Override
            public void onMessage(Message msg) {
            	
            	if(logger.isDebugEnabled()){
            		logger.debug("[onMessage]" + msg);
            	}
            	int result = count.incrementAndGet();
            	if(result % consumerPrintCount() == 0 ){
            		if(logger.isInfoEnabled()){
            			logger.info("[onMessage]" + result);
            		}
            	}
            	doOnMessage(msg);
            	sleep(sleepTime);
            }
        });
        
        c.start();
        sleep(200);
        return c;
	}

	protected void closeConsumer(Consumer consumer){
		consumer.close();
		sleep(100);
	}

	protected void startConsumer(Consumer consumer){
		consumer.start();
	}
	
	protected void restartConsumer(Consumer consumer){
		closeConsumer(consumer);
		startConsumer(consumer);
	}
	
	protected int getConsumerMessageCount(Consumer consumer){
		AtomicInteger count = getMessageCount.get(consumer);
		if(count == null){
			return 0;
		}
		return count.intValue();
	}

	protected void waitForListernToComplete(int messageCount) {
		
		sleep((int) (Math.ceil((double)messageCount/1000) * 5000));
	}

	protected void doOnMessage(Message msg) {
		
	}
	
	public String getTopic() {
		return topic;
	}

	protected int consumerPrintCount() {
		return 100;
	}

}
