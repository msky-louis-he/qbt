package com.qbt.sf.api.bean.orderconfirm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)  
@XmlType(propOrder = {}) 
public class Route {
	
	@XmlAttribute(name="accept_time")
//	@XmlJavaTypeAdapter(value=JaxbDateAdapter.class)
	private String accept_time;
	
	@XmlAttribute(name="accept_address")
	private String accept_address;
	
	@XmlAttribute(name="remark")
	private String remark;
	
	@XmlAttribute(name="opcode")
	private String opcode;
	


	public String getAccept_time() {
		return accept_time;
	}


	public void setAccept_time(String accept_time) {
		this.accept_time = accept_time;
	}


	public String getAccept_address() {
		return accept_address;
	}


	public void setAccept_address(String accept_address) {
		this.accept_address = accept_address;
	}


	public String getRemark() {
		return remark;
	}


	public void setRemark(String remark) {
		this.remark = remark;
	}


	public String getOpcode() {
		return opcode;
	}


	public void setOpcode(String opcode) {
		this.opcode = opcode;
	}


	@Override
	public String toString() {
		return "Route [accept_time=" + accept_time + ", accept_address="
				+ accept_address + ", remark=" + remark + ", opcode=" + opcode
				+ "]";
	}
}

	