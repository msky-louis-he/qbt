package com.qbt.web.support.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qbt.bussiness.support.WechatMessageSupport;
import com.qbt.common.entity.PageEntity;
import com.qbt.common.enums.ErrorCodeEnum;
import com.qbt.common.exception.BusinessException;
import com.qbt.common.util.BeanUtil;
import com.qbt.common.util.Checker;
import com.qbt.persistent.entity.TicketPackageActivity;
import com.qbt.persistent.entity.TicketPackageRule;
import com.qbt.persistent.entity.TicketPackageRuleConfig;
import com.qbt.persistent.entity.UserActivityOrder;
import com.qbt.persistent.entity.UserActivityOrderDetail;
import com.qbt.persistent.entity.UserActivityPackage;
import com.qbt.persistent.entity.UserPackageTicket;
import com.qbt.persistent.entity.UserWeixin;
import com.qbt.service.TicketPackageActivityService;
import com.qbt.service.TicketPackageRuleConfigService;
import com.qbt.service.TicketPackageRuleService;
import com.qbt.service.UserActivityOrderDetailService;
import com.qbt.service.UserActivityOrderService;
import com.qbt.service.UserActivityPackageService;
import com.qbt.service.UserPackageTicketService;
import com.qbt.service.UserWeixinService;
import com.qbt.util.CodeUtil;
import com.qbt.web.pojo.ticket.UserActivityOrderAddReqVo;
import com.qbt.web.pojo.ticket.UserActivityOrderPageReqVo;
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
	UserActivityPackageService userActivityPackageService;
	
	@Resource
	UserPackageTicketService userPackageTicketService;
	
	@Resource
	TicketPackageActivityService ticketPackageActivityService;
	
	@Resource 
	TicketPackageRuleConfigService ticketPackageRuleConfigService;
	
	@Resource
	private UserWeixinService userWeixinService;
	
	@Resource
	private WechatMessageSupport wechatMessageSupport;

	
	@Override
	public List<UserActivityOrderVo> findByPage(UserActivityOrderPageReqVo reqVo) {
		PageEntity<UserActivityOrder> pageEntity = BeanUtil.pageConvert(reqVo, UserActivityOrder.class);
		
		List<UserActivityOrder> list = userActivityOrderService.findByPage(pageEntity);
		List<UserActivityOrderVo> voList = new ArrayList<UserActivityOrderVo>();
		for(UserActivityOrder act : list){
			UserActivityOrderVo vo = BeanUtil.a2b(act, UserActivityOrderVo.class);
			voList.add(vo);
		}
		reqVo.setTotalCount(pageEntity.getTotalCount());
		return voList;
	}

	@Override
	public UserActivityOrderVo findById(Integer id) {
		UserActivityOrder ticketPackageOrder = userActivityOrderService.findById(id);
		UserActivityOrderVo vo = BeanUtil.a2b(ticketPackageOrder, UserActivityOrderVo.class);
		return vo;
	}
	
	private String getOrderNumber() {
		Date now = new Date(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String currentTime = dateFormat.format( now );
		int rand = new Random().nextInt(998) + 1000;		
		return currentTime + rand;
	}

	@Override
	public int add(UserActivityOrderAddReqVo vo) {
		UserActivityOrder order = BeanUtil.a2b(vo, UserActivityOrder.class);
		
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
	
	
	/**
	 * 
	 * @param orderId
	 */
	@Override
	public void createPackageAfterPaid(int orderId) {
		UserActivityOrder order = userActivityOrderService.findById(orderId);
		
		if(order == null || (order.getPayStatus() != null && order.getPayStatus() == 1)) {
			throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL, "订单不存在或者已经支付成功");
		}
		
		// 更改订单支付状态
		order.setPayStatus(1);
		userActivityOrderService.update(order);
		
		// 根据券包的活动，获取礼包和礼券的信息
		int activityId = order.getActivityId();
		
		//获取券包活动
		TicketPackageActivity ticketPackageActivity = ticketPackageActivityService.findById(activityId);
		int ruleId = ticketPackageActivity.getRuleId();
		
		int effectiveDay = ticketPackageActivity.getEffectiveDay();
		
		//获取券包活动的配置
		List<TicketPackageRuleConfig> ticketPackageRuleConfigs = ticketPackageRuleConfigService.findByRuleId(ruleId);
		
		PageEntity<UserActivityOrderDetail> pageEntity = new PageEntity<UserActivityOrderDetail>();
		UserActivityOrderDetail condition = new UserActivityOrderDetail();
		condition.setOrderId(orderId);		
		pageEntity.setCondition(condition);
		List<UserActivityOrderDetail> details = userActivityOrderDetailService.findByPage(pageEntity);
		
		UserWeixin userWeixin = userWeixinService.findById(order.getUserId());
		
		if(details != null) {
			for(UserActivityOrderDetail detail: details) {
				int orderDetailId = detail.getId();
				
				// 自动激活
				if(order.getBuyActive() == 1) {
					
					detail.setActivateUser(userWeixin.getName());
					detail.setActivatePhoneNumber(userWeixin.getMobile());
					detail.setActivateTime(new Date());
					detail.setActiveStatus(1);
					detail.setPackageType(0);
					
					Calendar cal = Calendar.getInstance();   
					cal.add(Calendar.DAY_OF_MONTH, effectiveDay);  
					detail.setExpirationTime(cal.getTime());
					
					userActivityOrderDetailService.update(detail);
				}
				
				// 创建用户券包 into user_activity_package
				UserActivityPackage userActivityPackage = new UserActivityPackage();
				userActivityPackage.setUserId(order.getUserId());
				userActivityPackage.setOrderId(orderId);
				userActivityPackage.setOrderDetailId(orderDetailId);

				userActivityPackage.setStatus(1);
				userActivityPackage.setCreateTime(new Date());
				userActivityPackage.setUpdateTime(new Date());

				userActivityPackage.setOperatorId(order.getOperatorId());
				userActivityPackage.setOperatorName(order.getOperatorName());

				int userActivityPackageId = userActivityPackageService.insert(userActivityPackage);

				// 根据活动规则配置创建用户券包
				if (ticketPackageRuleConfigs != null) {
					for (TicketPackageRuleConfig config : ticketPackageRuleConfigs) {
						UserPackageTicket userPackageTicket = new UserPackageTicket();
						userPackageTicket.setOrderDetailId(orderDetailId);
						userPackageTicket.setPackageId(userActivityPackageId);

						userPackageTicket.setTicketName(config.getTicketName());
						userPackageTicket.setTicketPrice(config.getTicketPrice());
						userPackageTicket.setQuantity(config.getQuantity());
						userPackageTicket.setUsedQuantity(0);
						userPackageTicket.setUseLimit(config.getUseLimit());
						userPackageTicket.setUseStatus(0);

						userPackageTicket.setCreateTime(new Date());
						userPackageTicket.setUpdateTime(new Date());

						userPackageTicketService.insert(userPackageTicket);
					}
				}
			}
		}
		
	}

	@Override
	public boolean update(UserActivityOrderVo vo) {
		UserActivityOrder order = userActivityOrderService.findById(vo.getId());
		if(Checker.isEmpty(order)) {
			throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL, "无效的数据");
		}
		
		order.setOrderNumber(vo.getOrderNumber());
		order.setActivityName(vo.getActivityName());
		order.setOperatorId(vo.getOperatorId());
		order.setOperatorName(vo.getOperatorName());
		order.setPayStatus(vo.getPayStatus());
		order.setProxyUser(vo.getProxyUser());
		order.setPurchaser(vo.getPurchaser());
		order.setPurchaserAddress(vo.getPurchaserAddress());
		order.setPurchaserNumber(vo.getPurchaserNumber());
		order.setType(vo.getType());
		order.setStatus(vo.getStatus());
		
		return userActivityOrderService.update(order) > 0;
	}

	@Override
	public Map<String, String> pushTicketPackagePaymentNotify(UserActivityOrder userActivityOrder){
		return wechatMessageSupport.pushTicketPackagePaymentNotify(userActivityOrder);
	}
}
