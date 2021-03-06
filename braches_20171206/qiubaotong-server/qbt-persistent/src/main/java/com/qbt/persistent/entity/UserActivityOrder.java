package com.qbt.persistent.entity;

import com.qbt.common.entity.BaseEntity;

/**
 * 券包订单
 * 
 * @author feng.xuan
 *
 */
public class UserActivityOrder extends BaseEntity {
	String orderNumber;
	String wechatTransactionNumber;
	int userId;
	String openid;
	Integer type; //(0 - e-ticket;1- paper ticket)
	int activityId;
	int activityAmount;
	String activityName;
	String purchaser;
	String purchaserAddress;
	String purchaserNumber;
	Integer status;// (0 - inactive; 1 - active)
	double payAmount;
	Integer payStatus; //(0  - not paid ; 1 - paid)
	String proxyUserId;
	String proxyUser;
	Integer operatorId;
	String operatorName;
	int buyActive; //(0:购买后暂不激活，1:购买后就激活)
	String description;
	String paymentUrl; // 用于微信支付，不保存数据库
	
	public String getPaymentUrl() {
		return paymentUrl;
	}

	public void setPaymentUrl(String paymentUrl) {
		this.paymentUrl = paymentUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getBuyActive() {
		return buyActive;
	}

	public void setBuyActive(int buyActive) {
		this.buyActive = buyActive;
	}

	public int getActivityAmount() {
		return activityAmount;
	}

	public void setActivityAmount(int activityAmount) {
		this.activityAmount = activityAmount;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getWechatTransactionNumber() {
		return wechatTransactionNumber;
	}

	public void setWechatTransactionNumber(String wechatTransactionNumber) {
		this.wechatTransactionNumber = wechatTransactionNumber;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getProxyUserId() {
		return proxyUserId;
	}

	public void setProxyUserId(String proxyUserId) {
		this.proxyUserId = proxyUserId;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getPurchaser() {
		return purchaser;
	}

	public void setPurchaser(String purchaser) {
		this.purchaser = purchaser;
	}

	public String getPurchaserAddress() {
		return purchaserAddress;
	}

	public void setPurchaserAddress(String purchaserAddress) {
		this.purchaserAddress = purchaserAddress;
	}

	public String getPurchaserNumber() {
		return purchaserNumber;
	}

	public void setPurchaserNumber(String purchaserNumber) {
		this.purchaserNumber = purchaserNumber;
	}

	public Integer getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(Integer payStatus) {
		this.payStatus = payStatus;
	}
	
	public double getPayAmount() {
		return payAmount;
	}

	public void setPayAmount(double payAmount) {
		this.payAmount = payAmount;
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public Integer getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(Integer operatorId) {
		this.operatorId = operatorId;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

}
