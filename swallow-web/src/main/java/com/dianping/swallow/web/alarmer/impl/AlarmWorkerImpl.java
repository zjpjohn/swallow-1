package com.dianping.swallow.web.alarmer.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.swallow.common.internal.lifecycle.impl.AbstractLifecycle;
import com.dianping.swallow.common.internal.util.CatUtil;
import com.dianping.swallow.common.internal.util.CommonUtils;
import com.dianping.swallow.web.alarmer.AlarmWorker;
import com.dianping.swallow.web.alarmer.AlarmerLifecycle;
import com.dianping.swallow.web.alarmer.EventChannel;
import com.dianping.swallow.web.model.event.Event;
import com.dianping.swallow.web.util.ThreadFactoryUtils;
import com.dianping.swallow.web.util.ThreadUtils;

/**
 * 
 * @author qiyin
 *
 *         2015年8月3日 下午6:06:26
 */
@Component
public class AlarmWorkerImpl extends AbstractLifecycle implements AlarmerLifecycle, AlarmWorker {

	private static final Logger logger = LoggerFactory.getLogger(AlarmWorkerImpl.class);

	@Autowired
	private EventChannel eventChannel;

	private static final String FACTORY_NAME = "AlarmWorker-Worker";

	private static final int poolSize = CommonUtils.DEFAULT_CPU_COUNT * 2;

	private volatile boolean isStopped = false;

	private ExecutorService executorService = null;

	private Thread alarmTaskThread;

	public AlarmWorkerImpl() {
		this(poolSize);
	}

	public AlarmWorkerImpl(int poolSize) {
		executorService = Executors.newFixedThreadPool(poolSize, ThreadFactoryUtils.getThreadFactory(FACTORY_NAME));
	}

	@Override
	protected void doInitialize() throws Exception {
		super.doInitialize();
		isStopped = false;
	}
	
	@Override
	protected void doStart() throws Exception {
		super.doStart();
		alarmTaskThread = ThreadUtils.createThread(new Runnable() {
			@Override
			public void run() {
				startAlarmTask();
			}

		}, "AlarmWorker-Boss", true);
		alarmTaskThread.start();
	}
	

	@Override
	public void startAlarmTask() {
		while (!checkStop()) {
			Event event = null;
			try {
				event = eventChannel.next();
				logger.info("[start] {}. ", event.getClass().getSimpleName());
				executorService.submit(new AlarmTask(event));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				CatUtil.logException(e);
				try {
					TimeUnit.SECONDS.sleep(200);
				} catch (InterruptedException ex) {
					// ignore
				}
				logger.error("[start] lost event {}. ", event.toString());
			}
		}
	}

	@Override
	protected void doStop() throws Exception {
		stopAlarmTask();
	}
	
	protected void doDispose() throws Exception {
		super.doDispose();
		executorService.shutdown();
	}
	
	@Override
	public void stopAlarmTask() {
		isStopped = true;
		alarmTaskThread.interrupt();
	}

	private boolean checkStop() {
		return isStopped || Thread.currentThread().isInterrupted();
	}

	private class AlarmTask implements Runnable {

		private Event event;

		public AlarmTask(Event event) {
			this.event = event;
		}

		@Override
		public void run() {
			try {
				event.alarm();
				logger.info("[run] {}.", event.getClass().getSimpleName());
			} catch (Exception e) {
				logger.error("[run] alarm event failed . ", e);
			}
		}

	}

}
