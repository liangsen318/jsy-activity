package com.jsy.activity.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.jsy.activity.pojo.ActTaskPojo;
import com.jsy.activity.pojo.ActivityImplPojo;
import com.jsy.activity.pojo.ProcessDefinitionPojo;
import com.jsy.activity.pojo.TaskIdMapPojo;
  
@Service("activityService")  
public class ActivityService {  
	private static Logger log = LoggerFactory.getLogger(ActivityService.class); 
	
	@Autowired
	private HistoryService historyService;
    @Autowired  
    private RuntimeService runtimeService;   
    @Autowired  
    private TaskService taskService;  
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;
      
    /** 
     * 启动/部署 流程
     * @return 
     */  
    @Transactional(propagation=Propagation.REQUIRED) 
	public void start(String key) {
		//流程启动  
//        List<Deployment> deployments = repositoryService  
//                .createDeploymentQuery().deploymentName("bohui")  
//                .orderByDeploymenTime().desc().list(); 
//        if(deployments==null||deployments.size()==0){
//        if("1".equals(flag)){
//        	InputStream in = this.getClass().getResourceAsStream("/processes/test.bpmn");  
//    		Deployment dm = repositoryService.createDeployment()  
//                                              .name("test")  
//                                              .addInputStream("test.bpmn",in)
//                                              .deploy();  
//        }
        //根据key启动最新版本的流程定义的实例
        ExecutionEntity pi1 = (ExecutionEntity) runtimeService.startProcessInstanceByKey(key);
        log.info("流程实例[key="+key+"]启动，实例id="+pi1.getProcessInstanceId());
	}

	/** 
     * 根据任务id完成流程节点
     * @return 
     */  
	public void completeTaskByTaskId(String taskId) {
        taskService.complete(taskId);
	}
	
	/** 
     * 根据任务id和map完成流程节点，并批注
     * @return 
     */  
	@Transactional(propagation=Propagation.REQUIRED) 
	public void completeTaskByTaskIdAndMap(@RequestBody TaskIdMapPojo pojo) {
		taskService.setAssignee(pojo.getTaskId(), pojo.getUserId());
		//批注
	    if(!StringUtils.isEmpty(pojo.getComment())){
	    	comment(pojo.getTaskId(),pojo.getComment());
	    }
		taskService.complete(pojo.getTaskId(),pojo.getMap());
        
	}
	
	/** 
     * 驳回流程 
     * @param userId 用户/办理人 
     * @param taskId 当前任务ID 
     * @param activityId 驳回节点ID 
     * @param variables 流程存储参数 
     * @throws Exception 
     */  
	@Transactional(propagation=Propagation.REQUIRED) 
    public void backProcess(@RequestBody TaskIdMapPojo pojo) {  
		String activityId = "";
		Task curTask = findTaskById(pojo.getTaskId());
		if(StringUtils.isEmpty(curTask.getCategory())){
			Task parentTask = findTaskById(curTask.getParentTaskId());
			activityId = parentTask.getTaskDefinitionKey();
		}else{
			activityId = curTask.getCategory();
//			ActivityImpl activityImpl = findActivitiImpl(pojo.getTaskId(),activityId);
//			if(activityImpl==null){
//				throw new Exception("驳回节点不存在");
//			}
		}
  
        // 查找所有并行任务节点，删除  
        List<Task> taskList = taskService.createTaskQuery()
        		.taskDefinitionKey(curTask.getTaskDefinitionKey()) 
                .list();  
        for (Task task : taskList) {  
        	if(Objects.equal(task.getId(), pojo.getTaskId())){
        		continue;
        	}
        	if(StringUtils.isEmpty(task.getExecutionId())){//任务还未完成
        		taskService.complete(task.getId());
            	comment(task.getId(),"该任务被用户["+pojo.getUserId()+"]驳回");
        	}
        	
//        	taskService.deleteTask(task.getId(),true);//true表示连带删除历史表(ACT_HI_TASKINST用户历史任务),ACT_HI_ACTINST(所有历史任务表中无法删除)
//        	historyService.deleteHistoricTaskInstance(task.getId());
        }  
        //当前节点转向到指定流程
        turnTransition(pojo.getUserId(),pojo.getTaskId(),pojo.getActivityId(),pojo.getMap());
        log.info("taskId["+pojo.getTaskId()+"]办理人["+pojo.getUserId()+"]驳回，流程转至节点["+activityId+"]");
    }  
	
	/** 
     * 中止流程 
     *  
     * @param taskId 
     */  
    public void endProcess(@RequestBody TaskIdMapPojo pojo) throws Exception {  
        ActivityImpl endActivity = findActivitiImpl(pojo.getTaskId(), "end"); 
        turnTransition(pojo.getUserId(),pojo.getTaskId(), endActivity.getId(), pojo.getMap());  
    }  
	

	/** 
	 * 查看流程定义 
	 * 查询act_re_procdef表 流程定义表 
	 */  
	public List<ProcessDefinitionPojo> queryProcdef(String key) {
		List<ProcessDefinitionPojo> resList = Lists.newArrayList();
		
	    //创建查询对象  
	    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();  
	    //添加查询条件  
	    query.processDefinitionKey(key);//通过key获取  
	        // .processDefinitionName("My process")//通过name获取  
	        // .orderByProcessDefinitionId()//根据ID排序  
	           //.processDefinitionKeyLike(processDefinitionKeyLike)//支持模糊查询  
	            //.listPage(firstResult, maxResults)//分页  
	    //执行查询获取流程定义明细  
	    List<ProcessDefinition> pds = query.list();//获取批量的明细  
	                    //.singleResult()//获取单个的明细  
	    for (ProcessDefinition pd : pds) { 
	    	ProcessDefinitionPojo pdPojo = new ProcessDefinitionPojo();
	    	pdPojo.setId(pd.getId());
	    	pdPojo.setKey(pd.getKey());
	    	pdPojo.setDescription(pd.getDescription());
	    	pdPojo.setCategory(pd.getCategory());
	    	pdPojo.setDeploymentId(pd.getDeploymentId());
	    	pdPojo.setDiagramResourceName(pd.getDiagramResourceName());
	    	pdPojo.setName(pd.getName());
	    	pdPojo.setResourceName(pd.getResourceName());
	    	pdPojo.setSuspended(pd.isSuspended());
	    	pdPojo.setTenantId(pd.getTenantId());
	    	pdPojo.setVersion(pd.getVersion());
	    	resList.add(pdPojo);
	    }  
		return resList;
	}

	/** 
     * 查看运行流程实例
     */  
	public ProcessInstance queryProcessInstance(String processInstanceId) {
        ProcessInstance pi = runtimeService  
                    //创建执行对象查询,查询正在执行的执行对象  
                    //.createExecutionQuery()  
                    .createProcessInstanceQuery()//创建流程实例查询,查询正在执行的流程实例  
                    .processInstanceId(processInstanceId)//通过流程实例ID查询  
                    //返回批量结果  
                    //.list()  
                    .singleResult();//返回唯一的结果  
        if (pi != null) {  
            System.out.println("当前流程:"+pi.getActivityId());  
        }else {  
            System.out.println("流程已经结束");  
        }  
        return pi;
	}  
	
	/** 
     * 根据接受人查询该用户的任务 
     */  
    public List<ActTaskPojo> queryTaskByUser(String user){  
    	List<ActTaskPojo> acTaskList = Lists.newArrayList();
    	
        //获取任务服务对象  
        List<Task> tasks = taskService.createTaskQuery()  
                                    //模糊查询  
                                    //.taskAssigneeLike(assigneeLike)  
                                    //通过执行对象ID查询任务  
                                    //.executionId(executionId)  
                                    .taskInvolvedUser(user)//通过接受人来查询个人任务  
                                    .list();  
        for (Task task : tasks) {  
            System.out.println("ID:"+task.getId()+",姓名:"+task.getName()+",接收人:"+task.getAssignee()+",开始时间:"+task.getCreateTime());  
            ActTaskPojo actTask = new ActTaskPojo();
            actTask.setAssignee(task.getAssignee());
            actTask.setUser(user);
            actTask.setId(task.getId());
            actTask.setName(task.getName());
            actTask.setCreateTime(task.getCreateTime());
            actTask.setDelegationState(task.getDelegationState());
            actTask.setTaskDefinitionKey(task.getTaskDefinitionKey());
            actTask.setCategory(task.getCategory());
            actTask.setFormKey(task.getFormKey());
            actTask.setOwner(task.getOwner());
            actTask.setParentTaskId(task.getParentTaskId());
            acTaskList.add(actTask);
        }  
        
        return acTaskList;
    }  
    
    /**
     * 使用processInstanceId查询历史流程中各节点的信息
     */
    public List<ActivityImplPojo> findHistoryActivitiImpl(String processInstanceId) { 
    	List<ActivityImplPojo> resList = Lists.newArrayList();
    	
    	List<HistoricActivityInstance> list = historyService // 历史相关Service
                .createHistoricActivityInstanceQuery() // 创建历史活动实例查询
                .processInstanceId(processInstanceId) // 执行流程实例id
//                .finished()
                .list();
    	for (HistoricActivityInstance historicActivityInstance : list) {
    		ActivityImplPojo pojo = new ActivityImplPojo();
    		pojo.setTaskId(historicActivityInstance.getTaskId());
    		pojo.setAssignee(historicActivityInstance.getAssignee());
    		pojo.setActivityId(historicActivityInstance.getActivityId());
    		pojo.setName(historicActivityInstance.getActivityName());
    		pojo.setType(historicActivityInstance.getActivityType());
    		pojo.setStartTime(historicActivityInstance.getStartTime());
    		pojo.setEndTime(historicActivityInstance.getEndTime());
    		pojo.setCommentList(findCommentByTaskId(historicActivityInstance.getTaskId()));
    		resList.add(pojo);
		}
    	return resList;
	}  
    
    /**
     * 使用processInstanceId查询流程定义中各节点的信息
     */
    public List<ActivityImplPojo> findActivitiImpl(String processInstanceId){ 
    	List<ActivityImplPojo> resList = Lists.newArrayList();
    	
    	ProcessInstance pi = runtimeService  
                //创建执行对象查询,查询正在执行的执行对象  
                //.createExecutionQuery()  
                .createProcessInstanceQuery()//创建流程实例查询,查询正在执行的流程实例  
                .processInstanceId(processInstanceId)//通过流程实例ID查询  
                //返回批量结果  
                //.list()  
                .singleResult();//返回唯一的结果  
    	
    	ReadOnlyProcessDefinition entity = ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(pi.getProcessDefinitionId());
    	ProcessDefinitionEntity proEntity = (ProcessDefinitionEntity)entity;
    	List<ActivityImpl> list = proEntity.getActivities();
    	for (ActivityImpl activityImpl : list) {
    		ActivityImplPojo pojo = new ActivityImplPojo();
    		pojo.setActivityId(activityImpl.getId());
    		pojo.setName((String)activityImpl.getProperties().get("name"));
    		pojo.setType((String)activityImpl.getProperties().get("type"));
    		resList.add(pojo);
		}
//	    ActivityImpl activityImpl =  proEntity.findActivity("jlCheck");    
	    return resList;    
	}  
    
    /**
     * 
     * 查询流程批注信息
     */
    public List<Comment> findCommentByTaskId(String taskId) {  
        List<Comment> list = taskService.getTaskComments(taskId);  
        return list;  
    }  
    
      
    /**
     * 获取流程的图片
     * @throws IOException 
     */
    public void testGetProcDefImg(String deploymentId) throws IOException{
        //从act_ge_bytearray表中获取数据，该表存放的是我们的流程定义文件和图片文件的数据
        List<String> resources=repositoryService.getDeploymentResourceNames(deploymentId);
        
        //获取图片的名称
        String imgName="";
        if(resources!=null&&resources.size()>0){
            for (String string : resources) {
                if(string.indexOf("png")>=0){
                    imgName=string;
                }
            }
        }
        System.out.println(imgName);
        if(imgName!=null){
            File f=new File("d:/tt.png");
            
            InputStream in=repositoryService.getResourceAsStream(deploymentId, imgName);
            
            FileUtils.copyInputStreamToFile(in, f);
            
        }
    }
    
    /**
     * 获取变量值
     * @param taskId
     * @param variableName
     * @return
     */
    public Object getVariablesByTask(String taskId,String variableName){  
        Object variable = taskService.getVariable(taskId, variableName);  
        return variable;  
    }  
    
    /**
     * 添加流程批注信息
     */
    @Transactional(propagation=Propagation.REQUIRED) 
    private void comment(String taskId, String message){
    	Task task =  findTaskById(taskId);  
    	taskService.addComment(taskId, task.getProcessInstanceId(), message);
    }
    
    /** 
     * 根据任务ID获得任务实例 
     *  
     * @param taskId 
     *            任务ID 
     * @return 
     * @throws Exception 
     */  
    private TaskEntity findTaskById(String taskId){  
        TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(taskId).singleResult();  
//        if (task == null) {  
//            throw new Exception("任务实例未找到!");  
//        }  
        return task;  
    }  
    
    /** 
     * 根据任务ID获取流程定义 
     *  
     * @param taskId 
     *            任务ID 
     * @return 
     * @throws Exception 
     */  
    private ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(String taskId){
    	ReadOnlyProcessDefinition entity = ((RepositoryServiceImpl)repositoryService)
    			.getDeployedProcessDefinition(findTaskById(taskId).getProcessDefinitionId());
    	ProcessDefinitionEntity proEntity = (ProcessDefinitionEntity)entity;
    	return proEntity;
    }
    
    /** 
     * 根据任务ID和节点ID获取活动节点 <br> 
     *  
     * @param taskId 
     *            任务ID 
     * @param activityId 
     *            活动节点ID <br> 
     *            如果为null或""，则默认查询taskId对应活动节点 <br> 
     *            如果为"end"，则查询结束节点 <br> 
     *  
     * @return 
     * @throws Exception 
     */  
    private ActivityImpl findActivitiImpl(String taskId, String activityId){  
        // 取得流程定义  
        ProcessDefinitionEntity processDefinition = findProcessDefinitionEntityByTaskId(taskId);  
  
        // 获取当前活动节点ID  
        if (StringUtils.isEmpty(activityId)) {  
            activityId = findTaskById(taskId).getTaskDefinitionKey();  
        }  
  
        // 根据流程定义，获取该流程实例的结束节点  
        if (activityId.toUpperCase().equals("END")) {  
            for (ActivityImpl activityImpl : processDefinition.getActivities()) {  
                List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();  
                if (pvmTransitionList.isEmpty()) {  
                    return activityImpl;  
                }  
            }  
        }  
  
        // 根据节点ID，获取对应的活动节点  
        ActivityImpl activityImpl = ((ProcessDefinitionImpl) processDefinition).findActivity(activityId);  
  
        return activityImpl;  
    }  
    
    /** 
     * 流程转向操作 
     *  
     * @param taskId 
     *            当前任务ID 
     * @param activityId 
     *            目标节点任务ID 
     * @param variables 
     *            流程变量 
     * @throws Exception 
     */  
    @Transactional(propagation=Propagation.REQUIRED) 
    private void turnTransition(String userId, String taskId, String activityId,Map<String, Object> variables){  
        // 当前节点  
        ActivityImpl currActivity = findActivitiImpl(taskId, null);  
        // 清空当前流向  
        List<PvmTransition> oriPvmTransitionList = clearTransition(currActivity);  
  
        // 创建新流向  
        TransitionImpl newTransition = currActivity.createOutgoingTransition();  
        // 目标节点  
        ActivityImpl pointActivity = findActivitiImpl(taskId, activityId);  
        // 设置新流向的目标节点  
        newTransition.setDestination(pointActivity);  
  
        // 执行转向任务  
        taskService.setAssignee(taskId, userId);
        taskService.complete(taskId, variables);  
        // 删除目标节点新流入  
        pointActivity.getIncomingTransitions().remove(newTransition);  
  
        // 还原以前流向  
        restoreTransition(currActivity, oriPvmTransitionList);  
    }  
    
    /** 
     * 清空指定活动节点流向 
     *  
     * @param activityImpl 
     *            活动节点 
     * @return 节点流向集合 
     */  
    @Transactional(propagation=Propagation.REQUIRED) 
    private List<PvmTransition> clearTransition(ActivityImpl activityImpl) {  
        // 存储当前节点所有流向临时变量  
        List<PvmTransition> oriPvmTransitionList = Lists.newArrayList();  
        // 获取当前节点所有流向，存储到临时变量，然后清空  
        List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();  
        for (PvmTransition pvmTransition : pvmTransitionList) {  
            oriPvmTransitionList.add(pvmTransition);  
        }  
        pvmTransitionList.clear();  
  
        return oriPvmTransitionList;  
    }  
    
    /** 
     * 还原指定活动节点流向 
     *  
     * @param activityImpl 
     *            活动节点 
     * @param oriPvmTransitionList 
     *            原有节点流向集合 
     */  
    @Transactional(propagation=Propagation.REQUIRED) 
    private void restoreTransition(ActivityImpl activityImpl,List<PvmTransition> oriPvmTransitionList) {  
        // 清空现有流向  
        List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();  
        pvmTransitionList.clear();  
        // 还原以前流向  
        for (PvmTransition pvmTransition : oriPvmTransitionList) {  
            pvmTransitionList.add(pvmTransition);  
        }  
    }  
}  