package com.jsy.activity.pojo;

import java.io.Serializable;

import org.activiti.engine.RepositoryService;

public class ProcessDefinitionPojo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** unique identifier */
	private String id;

	/**
	 * category name which is derived from the targetNamespace attribute in the
	 * definitions element
	 */
	private String category;

	/** label used for display purposes */
	private String name;

	/** unique name for all versions this process definitions */
	private String key;

	/** description of this process **/
	private String description;

	/** version of this process definition */
	private int version;

	/**
	 * name of {@link RepositoryService#getResourceAsStream(String, String) the
	 * resource} of this process definition.
	 */
	private String resourceName;

	/** The deployment in which this process definition is contained. */
	private String deploymentId;

	/** The resource name in the deployment of the diagram image (if any). */
	private String diagramResourceName;

	/** Returns true if the process definition is in suspended state. */
	private boolean isSuspended;

	/** The tenant identifier of this process definition */
	private String tenantId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public String getDiagramResourceName() {
		return diagramResourceName;
	}

	public void setDiagramResourceName(String diagramResourceName) {
		this.diagramResourceName = diagramResourceName;
	}

	public boolean isSuspended() {
		return isSuspended;
	}

	public void setSuspended(boolean isSuspended) {
		this.isSuspended = isSuspended;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	  
}
