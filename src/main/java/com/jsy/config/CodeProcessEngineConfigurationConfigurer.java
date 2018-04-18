package com.jsy.config;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.stereotype.Component;

/** 解决流程图乱码问题
 */
@Component
public class CodeProcessEngineConfigurationConfigurer implements ProcessEngineConfigurationConfigurer  {
	@Override
	public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
		processEngineConfiguration.setActivityFontName("宋体");
		processEngineConfiguration.setLabelFontName("宋体");
		processEngineConfiguration.setAnnotationFontName("宋体");
		System.out.println("CodeProcessEngineConfigurationConfigurer#############");
	}
}
