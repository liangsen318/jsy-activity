package com.jsy.activity.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.jsy.config.SpringUtils;

//@Component
public class MyTaksListener implements TaskListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private RestTemplate restTemplate = (RestTemplate)SpringUtils.getBean("restTemplate");

	public void notify(DelegateTask delegateTask) {
		System.out.println("restTemplate===="+restTemplate);
		System.out.println("delegateTask.getEventName() = " + delegateTask.getEventName());
		//添加审批的人员，以下任何一人通过即可进入下一环节
        String[] empLoyees = {"ls1","ls2"};
        delegateTask.addCandidateUsers(Arrays.asList(empLoyees));
		
		//post json数据
//		JSONObject postData = new JSONObject();
//		postData.put("data", "request for post");
//		List<String> json = restTemplate.postForEntity("http://localhost:8080/dhys/sys/quartz/test", postData, List.class).getBody();
//		System.out.println("json===="+json);

        //添加会签的人员，所有的都审批通过才可进入下一环节
//		List<String> assigneeList = new ArrayList<String>();
//		assigneeList.add("wangba");
//		assigneeList.add("wangjiu");
//		delegateTask.setVariable("publicityList",assigneeList); 
	}

}
