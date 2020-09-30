package com;

public class CodedElement implements java.io.Serializable {
	
	static final long serialVersionUID = 1L;
	
	public String code = "";
	public String displayName = "";

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public CodedElement(String code, String displayName) {
		super();
		this.code = code;
		this.displayName = displayName;
	}

	public CodedElement() {
		super();
	}

}
