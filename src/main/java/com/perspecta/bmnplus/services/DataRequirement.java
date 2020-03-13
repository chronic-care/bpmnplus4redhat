package com.perspecta.bmnplus.services;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.util.StringUtils;

 
public class DataRequirement {
	public DataRequirement() {
		super();
		 
	}

	private String clinicalStatement;
	private String id;
	private String clinicalStatementFilter;
	private String clinicalStatementDisplay;
	private String path;
	private String pathDisplay;
	public String getPathDisplay() {
		return pathDisplay;
	}

	public void setPathDisplay(String pathDisplay) {
		this.pathDisplay = pathDisplay;
	}

	private String type;
	private ArrayList<String> values = new ArrayList<String>();
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public DataRequirement(String clinicalStatement, String id, String clinicalStatementFilter,
			String clinicalStatementDisplay, String path, String type, List<String> values) {
		super();
		this.clinicalStatement = clinicalStatement;
		this.id = id;
		this.clinicalStatementFilter = clinicalStatementFilter;
		this.clinicalStatementDisplay = clinicalStatementDisplay;
		this.path = path;
		this.pathDisplay = path;
		
		
		
		if (!StringUtils.isEmpty(this.path) ) {
			
			String[] camelCaseWords = this.path.split("(?=[A-Z])");
			if (camelCaseWords.length >0) {
				camelCaseWords[0]=camelCaseWords[0].substring(0, 1).toUpperCase() + camelCaseWords[0].substring(1);
				this.pathDisplay = String.join(" ",camelCaseWords);		
			}
		
		}
		
		
		
		this.type = type;
		if (values != null) {
			this.values.addAll(values);
		} else {
			this.values.add("NONE");
		}

	}

	public String getClinicalStatement() {
		return clinicalStatement;
	}

	public String getClinicalStatementDisplay() {
		return clinicalStatementDisplay;
	}

	public String getClinicalStatementFilter() {
		return clinicalStatementFilter;
	}

	public String getId() {
		
		
		return id;
	}

	public String getPath() {
		return path;
	}

	public String getType() {
		return type;
	}

	public ArrayList<String> getValues() {
		return values;
	}

	public void setClinicalStatement(String clinicalStatement) {
		this.clinicalStatement = clinicalStatement;
	}

	public void setClinicalStatementDisplay(String clinicalStatementDisplay) {
		this.clinicalStatementDisplay = clinicalStatementDisplay;
	}

	public void setClinicalStatementFilter(String clinicalStatementFilter) {
		this.clinicalStatementFilter = clinicalStatementFilter;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setValues(ArrayList<String> values) {
		this.values = values;
	}

}