package com.dianping.swallow.web.controller;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.swallow.web.common.Pair;
import com.dianping.swallow.web.controller.dto.IpQueryDto;
import com.dianping.swallow.web.controller.dto.IpResourceDto;
import com.dianping.swallow.web.controller.mapper.IpResourceMapper;
import com.dianping.swallow.web.dao.impl.DefaultConsumerIdResourceDao;
import com.dianping.swallow.web.model.cmdb.IPDesc;
import com.dianping.swallow.web.model.resource.ConsumerIdResource;
import com.dianping.swallow.web.model.resource.IpResource;
import com.dianping.swallow.web.model.resource.TopicResource;
import com.dianping.swallow.web.service.ConsumerIdResourceService;
import com.dianping.swallow.web.service.IpResourceService;
import com.dianping.swallow.web.service.TopicResourceService;
import com.dianping.swallow.web.util.ResponseStatus;

/**
 * @author mingdongli
 *
 *         2015年8月27日下午3:04:37
 */
@Controller
public class IPController extends AbstractMenuController {

	private static final String IP = "ip";

	private static final String APPLICATION = "iPDesc.name";

	@Resource(name = "ipResourceService")
	private IpResourceService ipResourceService;
	
	@Resource(name = "topicResourceService")
	private TopicResourceService topicResourceService;
	
	@Resource(name = "consumerIdResourceService")
	private ConsumerIdResourceService consumerIdResourceService;

	@RequestMapping(value = "/console/ip")
	public ModelAndView topicView(HttpServletRequest request, HttpServletResponse response) {

		return new ModelAndView("ip/index", createViewMap());
	}

	@RequestMapping(value = "/console/ip/list", method = RequestMethod.POST)
	@ResponseBody
	public Object producerserverSettingList(@RequestBody IpQueryDto ipQueryDto) {

		Pair<Long, List<IpResource>> pair = null;
		List<IpResourceDto> ipResourceDto = new ArrayList<IpResourceDto>();
		int offset = ipQueryDto.getOffset();
		int limit = ipQueryDto.getLimit();
		String ip = ipQueryDto.getIp();
		String application = ipQueryDto.getApplication();
		String type = ipQueryDto.getType();
		if(StringUtils.isNotBlank(type)){
			List<String> ips = null;
			
			if("PRODUCER".equals(type)){
				ips = loadProducerIps();
			}else{
				ips = loadConsumerIps();
			}
			
			if(StringUtils.isNotBlank(ip)){
				String[] queryIps = StringUtils.split(ip);
				
				Collection<String> intersection = CollectionUtils.intersection(ips, Arrays.asList(queryIps));
				if(intersection.isEmpty()){
					return new Pair<Long, List<IpResourceDto>>(0L, ipResourceDto);
				}else{
					ip = StringUtils.join(intersection, ",");
				}
			}else{
				ip = StringUtils.join(ips, ",");
			}
		}
		
		Boolean isAllBlank = StringUtils.isBlank(ip) && StringUtils.isBlank(application);

		if (isAllBlank) {
			pair = ipResourceService.findIpResourcePage(offset, limit);
		} else {

			if (StringUtils.isBlank(application)) {
				String[] ips = ip.split(",");
				pair = ipResourceService.findByIp(offset, limit, ips);
			} else if (StringUtils.isBlank(ip)) {
				pair = ipResourceService.findByApplication(offset, limit, application);
			} else {
				String[] ips = ip.split(",");
				pair = ipResourceService.find(offset, limit, application, ips);
			}

		}

		for (IpResource ipResource : pair.getSecond()) {
			ipResourceDto.add(IpResourceMapper.toIpResourceDto(ipResource));
		}

		return new Pair<Long, List<IpResourceDto>>(pair.getFirst(), ipResourceDto);

	}

	@RequestMapping(value = "/console/ip/update", method = RequestMethod.POST)
	@ResponseBody
	public Object updateIp(@RequestBody IpResourceDto IpResourceDto) throws UnknownHostException {

		IpResource ipResource = IpResourceMapper.toIpResource(IpResourceDto);
		boolean result = ipResourceService.update(ipResource);

		if (result) {
			return ResponseStatus.SUCCESS.getStatus();
		} else {
			return ResponseStatus.MONGOWRITE.getStatus();
		}
	}

	@RequestMapping(value = "/console/ip/allip", method = RequestMethod.GET)
	@ResponseBody
	public List<String> loadCmsumerid() {

		Set<String> ips = new HashSet<String>();
		List<IpResource> ipResources = ipResourceService.findAll(IP);

		for (IpResource ipResource : ipResources) {
			String ip = ipResource.getIp();
			if (!ips.contains(ip)) {
				ips.add(ip);
			}
		}

		return new ArrayList<String>(ips);
	}

	@RequestMapping(value = "/console/ip/application", method = RequestMethod.GET)
	@ResponseBody
	public List<String> loadApplication() {

		Set<String> apps = new HashSet<String>();
		List<IpResource> ipResources = ipResourceService.findAll(APPLICATION);

		for (IpResource ipResource : ipResources) {
			IPDesc iPDesc = ipResource.getiPDesc();
			if (iPDesc != null) {
				String app = iPDesc.getName();
				if (StringUtils.isNotBlank(app) && !apps.contains(app)) {
					apps.add(app);
				}
			}
		}

		return new ArrayList<String>(apps);
	}

	@RequestMapping(value = "/console/ip/alarm", method = RequestMethod.GET)
	@ResponseBody
	public boolean editIpAlarm(@RequestParam String ip, @RequestParam boolean alarm, HttpServletRequest request,
			HttpServletResponse response) {

		Pair<Long, List<IpResource>> pair = ipResourceService.findByIp(0, 1, ip);
		if(pair.getFirst() == 0){
			throw new RuntimeException(String.format("Record of %s not found.", ip));
		}
		List<IpResource> ipResources = pair.getSecond();
		IpResource ipResource = ipResources.get(0);
		ipResource.setAlarm(alarm);
		boolean result = ipResourceService.update(ipResource);

		if (result) {
			if (logger.isInfoEnabled()) {
				logger.info(String.format("Update alarm of %s to %b successfully", ip, alarm));
			}
		} else {
			if (logger.isInfoEnabled()) {
				logger.info(String.format("Update alarm of %s to %b fail", ip, alarm));
			}
		}
		
		return result;
	}
	
	private List<String> loadProducerIps(){
		
		List<TopicResource> topicResources = topicResourceService.findAll();
		Set<String> ips = new LinkedHashSet<String>();
		
		List<String> ipList = null;
		for(TopicResource topicResource : topicResources){
			ipList = topicResource.getProducerIps();
			if(ipList != null){
				ips.addAll(ipList);
			}
		}
		return new ArrayList<String>(ips);
	}
	
	private List<String> loadConsumerIps(){
		
		List<ConsumerIdResource> consumerIdResources = consumerIdResourceService.findAll(DefaultConsumerIdResourceDao.CONSUMERIPS);
		Set<String> ips = new LinkedHashSet<String>();
		
		List<String> ipList = null;
		for(ConsumerIdResource consumerIdResource : consumerIdResources){
			ipList = consumerIdResource.getConsumerIps();
			if(ipList != null){
				ips.addAll(ipList);
			}
		}
		return new ArrayList<String>(ips);
	}
	
	@Override
	protected String getMenu() {
		return "ip";
	}

}
