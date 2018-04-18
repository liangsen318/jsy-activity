package com.jsy.activity.pojo;

import java.io.Serializable;
import java.util.Date;

import org.activiti.engine.task.DelegationState;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ActTaskPojo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;
	
	private int revision;

	private String owner;
	
	private String assignee;//指定人(区别候选人)
	
	private String user;//任务所有人(指定人或候选人)
	
	private String initialAssignee;
	
	private DelegationState delegationState;

	private String parentTaskId;

	private String name;
	
	private String localizedName;
	
	private String description;
	
	private Date createTime; // The time when the task has been created
	
	private Date dueDate;
	
	private String category;

	private String executionId;

	private String processInstanceId;

	private String processDefinitionId;

	private String taskDefinitionKey;
	
	private String formKey;

	private String eventName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public String getInitialAssignee() {
		return initialAssignee;
	}

	public void setInitialAssignee(String initialAssignee) {
		this.initialAssignee = initialAssignee;
	}

	public DelegationState getDelegationState() {
		return delegationState;
	}

	public void setDelegationState(DelegationState delegationState) {
		this.delegationState = delegationState;
	}

	public String getParentTaskId() {
		return parentTaskId;
	}

	public void setParentTaskId(String parentTaskId) {
		this.parentTaskId = parentTaskId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocalizedName() {
		return localizedName;
	}

	public void setLocalizedName(String localizedName) {
		this.localizedName = localizedName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getTaskDefinitionKey() {
		return taskDefinitionKey;
	}

	public void setTaskDefinitionKey(String taskDefinitionKey) {
		this.taskDefinitionKey = taskDefinitionKey;
	}

	public String getFormKey() {
		return formKey;
	}

	public void setFormKey(String formKey) {
		this.formKey = formKey;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
}
