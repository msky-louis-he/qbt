package com.qbt.web.support.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qbt.common.util.BeanUtil;
import com.qbt.persistent.entity.TicketPackageActivity;
import com.qbt.persistent.entity.TicketPackageRule;
import com.qbt.persistent.entity.UserActivityOrder;
import com.qbt.persistent.entity.UserActivityOrderDetail;
import com.qbt.service.TicketPackageActivityService;
import com.qbt.service.TicketPackageRuleService;
import com.qbt.service.UserActivityOrderDetailService;
import com.qbt.service.UserActivityOrderService;
import com.qbt.util.CodeUtil;
import com.qbt.web.pojo.ticket.UserActivityOrderVo;
import com.qbt.web.support.UserActivityOrderSupport;

@Service
public class UserActivityOrderSupportImpl implements UserActivityOrderSupport {

	@Resource
	UserActivityOrderService userActivityOrderService;
	
	@Resource
	TicketPackageRuleService ticketPackageRuleService;
	
	@Resource
	UserActivityOrderDetailService userActivityOrderDetailService;
	
	@Resource
	TicketPackageActivityService ticketPackageActivityService;
	
	private String getOrderNumber() {
		Date now = new Date(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String currentTime = dateFormat.format( now );
		int rand = new Random().nextInt(998) + 1000;		
		return currentTime + rand;
	}

	@Override
	public int add(UserActivityOrder order) {
		
		order.setStatus(1);
		order.setPayStatus(0);
		order.setCreateTime(new Date());
		order.setUpdateTime(new Date());
		order.setOrderNumber(getOrderNumber());
		
		//获取券包活动
		TicketPackageActivity ticketPackageActivity = ticketPackageActivityService.findById(order.getActivityId());
		
		int ruleId = ticketPackageActivity.getRuleId();
		TicketPackageRule packageRule = ticketPackageRuleService.findById(ruleId);
		
		// 根据券包的活动，获取礼包和礼券的信息
		int activityAmount = order.getActivityAmount();
		
		double payAmount = activityAmount * packageRule.getPayPrice();
		order.setPayAmount(payAmount);
		order.setActivityName(ticketPackageActivity.getActivityName());		
		
		// add order to user_activity_order
		int orderId = userActivityOrderService.insert(order);
		
		// add order detail into user_activity_order_detail
		for(int i=0; i<activityAmount;i++) {
			UserActivityOrderDetail detail = new UserActivityOrderDetail();
			
			detail.setOrderId(orderId);
			detail.setUserId(order.getUserId());
			detail.setType(order.getType());
			detail.setPackageType(0);
			
			detail.setStatus(1);
			detail.setActiveStatus(0);
			
			detail.setCreateTime(new Date());
			detail.setUpdateTime(new Date());
			
			detail.setOperatorId(order.getOperatorId());
			detail.setOperatorName(order.getOperatorName());
			
			if(order.getType()==1){
				//paper-ticket,生成暗码
				detail.setQrCode(CodeUtil.getCode());
			}
			detail.setQrCode(CodeUtil.GetQRCode());
	
			userActivityOrderDetailService.insert(detail);
		}
		return orderId;
	}
	
	@Override
	public UserActivityOrderVo findById(Integer id) {
		UserActivityOrder ticketPackageOrder = userActivityOrderService.findById(id);
		UserActivityOrderVo vo = BeanUtil.a2b(ticketPackageOrder, UserActivityOrderVo.class);
		return vo;
	}

}
