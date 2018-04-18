package com.jsy.activity.listener;

import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import com.google.common.collect.Lists;

public class UserListListener implements TaskListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
//	@Autowired
//	private RestTemplate restTemplate;

	public void notify(DelegateTask delegateTask) {
//		restTemplate.postForObject("", "", String.class);
		System.out.println("delegateTask.getEventName() = " + delegateTask.getEventName());

		List<String> userList = Lists.newArrayList();
		userList.add("ls3");
		userList.add("ls4");
		userList.add("ls5");
		delegateTask.setVariable("userList", userList);
	}
}
