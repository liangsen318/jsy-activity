package com.jsy.activity.controller;

import java.io.IOException;
import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jsy.activity.pojo.ActTaskPojo;
import com.jsy.activity.pojo.ActivityImplPojo;
import com.jsy.activity.pojo.ProcessDefinitionPojo;
import com.jsy.activity.pojo.TaskIdMapPojo;
import com.jsy.activity.service.ActivityService;
  
@RestController  
@RequestMapping("/activityService")  
public class ActivityController {  
	@Autowired
	private ActivityService activityService;
    
    /** 
     * 启动/部署 流程
     * @return 
     */  
//    @RequestMapping(value="/deploy",method=RequestMethod.GET)  
    @RequestMapping(value="/start",method=RequestMethod.POST)  
    public void start(String key){
    	activityService.start(key);
    }  
    
    /** 
     * 根据任务id完成流程节点
     * @return 
     */  
    @RequestMapping(value="/completeTaskByTaskId",method=RequestMethod.POST)  
    public void completeTaskByTaskId(String taskId){
    	activityService.completeTaskByTaskId(taskId);
    }  
    
    /** 
     * 根据任务id和map完成流程节点，并批注
     * @return 
     */  
    @RequestMapping(value="/completeTaskByTaskIdAndMap",method=RequestMethod.POST)  
    public void completeTaskByTaskIdAndMap(@RequestBody TaskIdMapPojo pojo){
    	activityService.completeTaskByTaskIdAndMap(pojo);
    }  
    
    /** 
     * 驳回流程 
     * @param userId 用户/办理人 
     * @param taskId 当前任务ID 
     * @param activityId 驳回节点ID 
     * @param map 流程存储参数 
     * @throws Exception 
     */  
    @RequestMapping(value="/backProcess",method=RequestMethod.POST)  
    public void backProcess(@RequestBody TaskIdMapPojo pojo){
    	activityService.backProcess(pojo);
    }
    
    /** 
     * 查看流程定义 
     * 查询act_re_procdef表 流程定义表 
     */  
    @RequestMapping(value="/queryProcdef",method=RequestMethod.POST)
    @ResponseBody
    public List<ProcessDefinitionPojo> queryProcdef(String key){
    	return activityService.queryProcdef(key);
    }
    
    /** 
     * 查看流程
     */  
    @RequestMapping(value="/queryProcessInstance",method=RequestMethod.POST)  
    public ProcessInstance queryProcessInstance(String processInstanceId){
    	return activityService.queryProcessInstance(processInstanceId);
    }
    
    /** 
     * 根据接受人查询该用户的任务 
     */  
    @RequestMapping(value="/queryTaskByUser",method=RequestMethod.POST)  
    @ResponseBody
    public List<ActTaskPojo> queryTaskByUser(String user){
    	return activityService.queryTaskByUser(user);
    }
    
    /**
     * 使用processInstanceId查询流程定义中各节点的信息
     */
    @RequestMapping(value="/findActivitiImpl",method=RequestMethod.POST)  
    @ResponseBody
    public List<ActivityImplPojo> findActivitiImpl(String processInstanceId){
    	return activityService.findActivitiImpl(processInstanceId);
    }
    
    /**
     * 使用processInstanceId查询历史流程中各节点的信息
     */
    @RequestMapping(value="/findHistoryActivitiImpl",method=RequestMethod.POST)  
    @ResponseBody
    public List<ActivityImplPojo> findHistoryActivitiImpl(String processInstanceId){
    	return activityService.findHistoryActivitiImpl(processInstanceId);
    }
    
    /**
     * 
     * 查询流程批注信息
     */
    @RequestMapping(value="/findCommentByTaskId",method=RequestMethod.POST)
    @ResponseBody
    public List<Comment> findCommentByTaskId(String taskId){
    	return activityService.findCommentByTaskId(taskId);
    }
    
    
    /**
     * 获取流程的图片
     * @throws IOException 
     */
    @RequestMapping(value="/testGetProcDefImg",method=RequestMethod.POST)  
    public void testGetProcDefImg(String deploymentId) throws IOException{
    	activityService.testGetProcDefImg(deploymentId);
    }
    
    /**
     * 获取变量值
     * @param taskId
     * @param variableName
     * @return
     */
    @RequestMapping(value="/getVariablesByTask",method=RequestMethod.POST) 
    @ResponseBody
    public Object getVariablesByTask(String taskId,String variableName){
    	return activityService.getVariablesByTask(taskId, variableName);
    }
}  