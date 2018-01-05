package com.qbt.web.support.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qbt.bussiness.pojo.vip.VipOrderConfirmRequest;
import com.qbt.bussiness.pojo.vip.VipOrderConfirmResponse;
import com.qbt.bussiness.support.EmailMsgSupport;
import com.qbt.bussiness.support.OrderCommonSupport;
import com.qbt.bussiness.support.SmsMsgSupport;
import com.qbt.bussiness.support.VipCommonSupport;
import com.qbt.bussiness.support.WechatCommonSupport;
import com.qbt.bussiness.support.WechatMessageSupport;
import com.qbt.common.entity.BaseDiffEnum;
import com.qbt.common.entity.PageEntity;
import com.qbt.common.enums.AddrTypeEnum;
import com.qbt.common.enums.BigOrderPayStatus;
import com.qbt.common.enums.BigOrderStatus;
import com.qbt.common.enums.DeliveryStatus;
import com.qbt.common.enums.ErrorCodeEnum;
import com.qbt.common.enums.ExceptionTypeEnum;
import com.qbt.common.enums.ExpressTypeEnum;
import com.qbt.common.enums.LanguagesEnum;
import com.qbt.common.enums.MqTypeEnum;
import com.qbt.common.enums.OrderOperateTypeEnum;
import com.qbt.common.enums.OrderSource;
import com.qbt.common.enums.OrderStatus;
import com.qbt.common.enums.PayStatus;
import com.qbt.common.enums.PaymentStatus;
import com.qbt.common.enums.PaymethodEnum;
import com.qbt.common.enums.QuestionStatus;
import com.qbt.common.enums.RefundStatus;
import com.qbt.common.enums.SettlementStatusEnum;
import com.qbt.common.enums.SfBpsStatus;
import com.qbt.common.exception.BusinessException;
import com.qbt.common.exception.WechatException;
import com.qbt.common.logback.LogCvt;
import com.qbt.common.util.Arith;
import com.qbt.common.util.BeanUtil;
import com.qbt.common.util.Checker;
import com.qbt.common.util.DateUtil;
import com.qbt.common.util.Md5;
import com.qbt.common.util.RedisKeyUtil;
import com.qbt.common.util.ReflectHelper;
import com.qbt.common.util.StringUtil;
import com.qbt.common.util.ToolsUtil;
import com.qbt.email.EmailService;
import com.qbt.persistent.dto.BossOrderInfo;
import com.qbt.persistent.entity.BaseCourse;
import com.qbt.persistent.entity.BaseInsuredTemplate;
import com.qbt.persistent.entity.BaseSfArea;
import com.qbt.persistent.entity.MailChannel;
import com.qbt.persistent.entity.OrderActivityBook;
import com.qbt.persistent.entity.OrderChangeBook;
import com.qbt.persistent.entity.OrderDelayTask;
import com.qbt.persistent.entity.OrderExceptionBook;
import com.qbt.persistent.entity.OrderInfo;
import com.qbt.persistent.entity.OrderOperateLog;
import com.qbt.persistent.entity.OrderPayInfo;
import com.qbt.persistent.entity.OrderRefund;
import com.qbt.persistent.entity.OrderSettlement;
import com.qbt.persistent.entity.OrderWaybill;
import com.qbt.persistent.entity.Orders;
import com.qbt.persistent.entity.OutRangeAddress;
import com.qbt.persistent.entity.SaleUser;
import com.qbt.persistent.entity.SysUser;
import com.qbt.persistent.entity.Urgent;
import com.qbt.persistent.entity.UserContact;
import com.qbt.persistent.entity.UserOrderContact;
import com.qbt.persistent.entity.UserWeixin;
import com.qbt.persistent.entity.VipInfo;
import com.qbt.redis.RedisService;
import com.qbt.service.ActivityService;
import com.qbt.service.BaseAirportService;
import com.qbt.service.BaseBankBranchService;
import com.qbt.service.BaseCourseService;
import com.qbt.service.BaseSfAreaService;
import com.qbt.service.BaseSfSiteService;
import com.qbt.service.CommonService;
import com.qbt.service.CouponService;
import com.qbt.service.MailChannelService;
import com.qbt.service.OrderActivityBookService;
import com.qbt.service.OrderChangeBookService;
import com.qbt.service.OrderCouponService;
import com.qbt.service.OrderDelayTaskService;
import com.qbt.service.OrderExceptionBookService;
import com.qbt.service.OrderInfoService;
import com.qbt.service.OrderOperateLogService;
import com.qbt.service.OrderPayInfoService;
import com.qbt.service.OrderRefundService;
import com.qbt.service.OrderSettlementService;
import com.qbt.service.OrderWaybillService;
import com.qbt.service.OrdersService;
import com.qbt.service.OutRangeAddressService;
import com.qbt.service.SaleOrderInfoService;
import com.qbt.service.SaleUserService;
import com.qbt.service.SysUserService;
import com.qbt.service.UrgentService;
import com.qbt.service.UserAssistantService;
import com.qbt.service.UserContactService;
import com.qbt.service.UserOrderContactService;
import com.qbt.service.UserWeixinService;
import com.qbt.service.VipUserService;
import com.qbt.sms.api.service.SmsSendService;
import com.qbt.web.pojo.order.OrderActivityRequestVo;
import com.qbt.web.pojo.order.OrderActivityResponseVo;
import com.qbt.web.pojo.order.OrderConfirm;
import com.qbt.web.pojo.order.OrderDetailVo;
import com.qbt.web.pojo.order.OrderOperateLogVo;
import com.qbt.web.pojo.order.OrderPageReqVo;
import com.qbt.web.pojo.order.OrderVo;
import com.qbt.web.pojo.order.OrderWaybillVo;
import com.qbt.web.pojo.refund.OrderCancelRefund;
import com.qbt.web.pojo.urgent.UrgentPageReqVo;
import com.qbt.web.support.ActivityOffSupport;
import com.qbt.web.support.BaseInsTempSupport;
import com.qbt.web.support.OrderSupport;
import com.qbt.web.support.VipInfoSupport;
import com.qbt.wechat.api.WechatApi;

@Service
public class OrderSupportImpl implements OrderSupport {
	
	@Resource
	private OrderInfoService orderInfoService;
	
	@Resource
	private UserWeixinService userWeixinService;
	
	@Resource
	private OrderPayInfoService orderPayInfoService;
	
	@Resource
	private OrderOperateLogService orderOperateLogService;
	
	@Resource
	private OrderWaybillService orderWaybillService;
	
	@Resource
	private CommonService commonService;
	
	@Resource
	private OrderRefundService orderRefundService;
	
	@Resource
	private OrderCouponService orderCouponService;
	
	@Resource
	private ActivityService activityService;
	
	@Resource
	private CouponService couponService;
	
	@Resource
	private OrderExceptionBookService orderExceptionBookService;
	
	@Resource
	private WechatApi wechatApi;
	
	@Resource
	private SmsSendService smsSendService;
	
	@Resource
	private UserAssistantService userAssistantService;
	
	@Resource
	private OrderChangeBookService orderChangeBookService;
	
	@Resource
	private RedisService redisService;
	
	@Resource
	private EmailService emailService;
	
	@Resource
	private WechatCommonSupport commonSupport;
	
	@Resource
	private OrderActivityBookService orderActivityBookService;
	
	@Resource
	private OrderSettlementService orderSettlementService;
	
	@Resource
	private BaseCourseService baseCourseService;
	
	@Resource
	private BaseSfSiteService baseSfSiteService;
	
	@Resource
	private BaseAirportService baseAirportService;
	
	@Resource
	private BaseBankBranchService baseBankBranchService;
	
	@Resource
	private BaseSfAreaService baseSfAreaService;
	
	@Resource
	private OrdersService ordersService;
	
	@Resource
	private BaseInsTempSupport baseInsTempSupport;
	
	@Resource
	private OrderDelayTaskService orderDelayTaskService;
	
	@Resource
	private UserContactService userContactService;
	
	@Resource
	private ActivityOffSupport activityOffSupport;
	
	@Resource
	private VipUserService vipUserService;
	
	@Resource
	private VipInfoSupport vipInfoSupport;
	
	@Resource
	private SaleOrderInfoService saleOrderInfoService;
	
	@Resource
	private SaleUserService saleUserService;
	
	@Resource
	private UserOrderContactService userOrderContactService;
	
	@Resource
	private OutRangeAddressService outRangeAddressService;
	
	@Resource
	private SmsMsgSupport smsMessageSupport;
	
	@Resource
	private MailChannelService mailChannelService;
	
	@Resource
	private WechatMessageSupport wechatMessageSupport;
	
	@Resource
	private EmailMsgSupport emailMsgSupport;
	
	@Resource
	private OrderCommonSupport orderCommonSupport;
	
	@Resource
	private VipCommonSupport vipCommonSupport;
	
	@Resource
	private SysUserService sysUserService;
	
	@Resource
	UrgentService urgentService;	
	
	@Override
	public List<OrderVo> findByPage(OrderPageReqVo reqVo) {
		PageEntity<BossOrderInfo> pageEntity = BeanUtil.pageConvert(reqVo, BossOrderInfo.class);
		BossOrderInfo condition = pageEntity.getCondition();
		condition.setNumber(reqVo.getOrderNo());
		if(Checker.isEmpty(reqVo.getMailChannel())){
			condition.setMailChannel(null);
		}
		
		List<BossOrderInfo> list = orderInfoService.findByPageBoss(pageEntity);
		List<OrderVo> voList = BeanUtil.list2list(list, OrderVo.class);
		if(Checker.isNotEmpty(voList)){
			BaseCourse jCourse = null;
			BaseCourse dCourse = null;
			for(OrderVo vo : voList){
				vo.setUserName(Checker.isNotEmpty(vo.getUserName()) ? vo.getUserName() : vo.getNickname());
				if(Checker.isNotEmpty(vo.getjAddrType()) && AddrTypeEnum.course.getType() == vo.getjAddrType()){
					jCourse = baseCourseService.findById(vo.getjAddrId());
					if(Checker.isNotEmpty(jCourse)){
						vo.setjCourseStatus(jCourse.getStatus());
					}
				}
				if(Checker.isNotEmpty(vo.getdAddrType()) && AddrTypeEnum.course.getType() == vo.getdAddrType()){
					dCourse = baseCourseService.findById(vo.getdAddrId());
					if(Checker.isNotEmpty(dCourse)){
						vo.setdCourseStatus(dCourse.getStatus());
					}
				}
			}
		}
		reqVo.setTotalCount(pageEntity.getTotalCount());
		return voList;
	}

	@Override
	public OrderDetailVo findById(int id) {
		OrderInfo orderInfo = orderInfoService.findById(id);
		UserWeixin userWeixin = null;
		OrderPayInfo orderPayInfo = null;
		OrderDetailVo detail = null;
		if(Checker.isNotEmpty(orderInfo)){
			detail = BeanUtil.a2b(orderInfo, OrderDetailVo.class);
			//微信用户信息
			userWeixin = userWeixinService.findByOpenId(orderInfo.getOpenid());
			String jTreeId= baseSfAreaService.findById(orderInfo.getjAreaId()).getTreeId();
			String dTreeId= baseSfAreaService.findById(orderInfo.getdAreaId()).getTreeId();
			detail.setjTreeId(jTreeId);
			detail.setdTreeId(dTreeId);
			if(Checker.isNotEmpty(userWeixin)){
				detail.setUserName(Checker.isNotEmpty(userWeixin.getName()) ? userWeixin.getName() : userWeixin.getNickname());
				detail.setNickname(userWeixin.getNickname());
			}
			//支付信息
			if(Checker.isNotEmpty(orderInfo.getPayId())&&orderInfo.getPayId()>0){
				orderPayInfo = orderPayInfoService.findById(orderInfo.getPayId());
				if(Checker.isNotEmpty(orderPayInfo)){
					detail.setUuid(orderPayInfo.getUuid());
					detail.setPayMethod(orderPayInfo.getPayMethod());
					detail.setPayStatus(orderPayInfo.getStatus());
					//若是代下单订单,支付信息加,订单支付人：代下单人支付／用户支付
					if(orderInfo.getProxyUserId()>0){
						if(orderInfo.getProxyUserId().equals(orderPayInfo.getUserId())){
							detail.setIsProxyPay(true);
						}else{
							detail.setIsProxyPay(false);
						}
					}
				}
			}
			if(orderInfo.getProxyUserId()>0){
				//支付信息加,代下单人：销售人员名字
				SaleUser saleUser = saleUserService.findByUserId(orderInfo.getProxyUserId());
				if(saleUser!=null)
					detail.setSaleUserName(saleUser.getName());
			}
			//结算信息
			OrderSettlement orderSettlement = orderSettlementService.findByOrderId(id);
			if(Checker.isNotEmpty(orderSettlement)){
				detail.setSettleState(orderSettlement.getSettleState());
			}
			//球场信息
			if(Checker.isNotEmpty(detail.getjAddrType()) && AddrTypeEnum.course.getType() == detail.getjAddrType()){
				BaseCourse jCourse = baseCourseService.findById(detail.getjAddrId());
				if(Checker.isNotEmpty(jCourse)){
					detail.setjCourseStatus(jCourse.getStatus());
				}
			}
			if(Checker.isNotEmpty(detail.getdAddrType()) && AddrTypeEnum.course.getType() == detail.getdAddrType()){
				BaseCourse dCourse = baseCourseService.findById(detail.getdAddrId());
				if(Checker.isNotEmpty(dCourse)){
					detail.setdCourseStatus(dCourse.getStatus());
				}
			}
			//vip会员信息
			if(detail.getIsVip()){
				VipInfo vipInfo = vipUserService.findById(detail.getVipId());
				if(Checker.isNotEmpty(vipInfo)){
					detail.setVipNo(vipInfo.getVipNo());
				}
			}
			if(Checker.isNotEmpty(detail.getWeixinPayCodeOperater())){
				SysUser sysuser	=sysUserService.selectById(Integer.parseInt(detail.getWeixinPayCodeOperater()));
				if(sysuser!=null){
					detail.setWeixinPayCodeOperater(sysuser.getName());
				}
			}
			
			OrderRefund orderRefund = orderRefundService.findByOrderId(id);
			detail.setCancelFee(Checker.isEmpty(orderRefund) ? 0 : orderRefund.getFee());
		}
		return detail;
	}

	@Override
	public List<OrderOperateLogVo> findOperateLogByOrderId(Integer orderId) {
		List<OrderOperateLog> logList = orderOperateLogService.findByOrderId(orderId);
		List<OrderOperateLogVo> voList = BeanUtil.list2list(logList, OrderOperateLogVo.class);
		return voList;
	}

	@Override
	public List<OrderWaybillVo> findWaybillByOrderId(Integer orderId) {
		List<OrderWaybill> waybillList = orderWaybillService.findByOrderId(orderId);
		List<OrderWaybillVo> voList = BeanUtil.list2list(waybillList, OrderWaybillVo.class);
		return voList;
	}

	/**
	 * 订单修改检查
	  * @Title: update
	  * @Description: TODO
	  * @author: share 2016年8月27日
	  * @modify: share 2016年8月27日
	  * @param orderDetail
	  * @see com.qbt.web.support.OrderSupport#update(com.qbt.web.pojo.order.OrderDetailVo)
	 */
	@Override
	public void update(OrderDetailVo orderDetail, SysUser sysUser) {
		// 替换地区ID的市名称
		BaseSfArea jArea =  baseSfAreaService.findById(orderDetail.getjAreaId());
		jArea = baseSfAreaService.findById(jArea.getParentId());
		if(Checker.isEmpty(jArea)) {
			throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"寄件地区不存在");
		}
		String[] jTreePaths = orderDetail.getjTreePath().split(",");
		orderDetail.setjTreePath(jTreePaths[0]+","+jArea.getFullName()+","+jTreePaths[2]);
		
		BaseSfArea dArea =  baseSfAreaService.findById(orderDetail.getdAreaId());
		dArea = baseSfAreaService.findById(dArea.getParentId());
		if(Checker.isEmpty(dArea)) {
			throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"收件地区不存在");
		}
		String[] dTreePaths = orderDetail.getdTreePath().split(",");
		orderDetail.setdTreePath(dTreePaths[0]+","+dArea.getFullName()+","+dTreePaths[2]);
		//偏远地区信息
		int jOutRangeType = 0;
		double jOutRangeTime = 0;
		double jOutRangePrice = 0;
		int dOutRangeType = 0;
		double dOutRangeTime = 0;
		double dOutRangePrice = 0;
		BaseCourse jCourse = null;
		BaseCourse dCourse = null;
		OutRangeAddress jOutRangeAddress = null;
		OutRangeAddress dOutRangeAddress = null;
		if(AddrTypeEnum.course.getType() == orderDetail.getjAddrType()) {
			jCourse = baseCourseService.findById(orderDetail.getjAddrId());
			if(Checker.isEmpty(jCourse)) {
				throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"寄件球场不存在");
			}
			jOutRangeType = jCourse.getIsOutRange() ? AddrTypeEnum.course.getType() : jOutRangeType;
			jOutRangeTime = jCourse.getIsOutRange() ? jCourse.getOutRangeTime() : jOutRangeTime;
			jOutRangePrice = jCourse.getIsOutRange() ? jCourse.getOutRangePrice() : jOutRangePrice;
		}else if(AddrTypeEnum.address.getType() == orderDetail.getjAddrType()) {
			jOutRangeAddress = outRangeAddressService.findByAreaId(orderDetail.getjAreaId(), orderDetail.getjAddress());
			jOutRangeType = Checker.isNotEmpty(jOutRangeAddress) ? AddrTypeEnum.address.getType() : jOutRangeType;
			jOutRangeTime = Checker.isNotEmpty(jOutRangeAddress) ? jOutRangeAddress.getOutRangeTime() : jOutRangeTime;
			jOutRangePrice = Checker.isNotEmpty(jOutRangeAddress) ? jOutRangeAddress.getOutRangePrice() : jOutRangePrice;
		}
		if(AddrTypeEnum.course.getType() == orderDetail.getdAddrType()) {
			dCourse = baseCourseService.findById(orderDetail.getdAddrId());
			if(Checker.isEmpty(dCourse)) {
				throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"收件球场不存在");
			}
			dOutRangeType = dCourse.getIsOutRange() ? AddrTypeEnum.course.getType() : dOutRangeType;
			dOutRangeTime = dCourse.getIsOutRange() ? dCourse.getOutRangeTime() : dOutRangeTime;
			dOutRangePrice = dCourse.getIsOutRange() ? dCourse.getOutRangePrice() : dOutRangePrice;
		}else if(AddrTypeEnum.address.getType() == orderDetail.getdAddrType()) {
			dOutRangeAddress = outRangeAddressService.findByAreaId(orderDetail.getdAreaId(), orderDetail.getdAddress());
			dOutRangeType = Checker.isNotEmpty(dOutRangeAddress) ? AddrTypeEnum.address.getType() : dOutRangeType;
			dOutRangeTime = Checker.isNotEmpty(dOutRangeAddress) ? dOutRangeAddress.getOutRangeTime() : dOutRangeTime;
			dOutRangePrice = Checker.isNotEmpty(dOutRangeAddress) ? dOutRangeAddress.getOutRangePrice() : dOutRangePrice;
		}
		orderDetail.setjOutRangePrice(jOutRangePrice);
		orderDetail.setdOutRangePrice(dOutRangePrice);
		
		// 检查修改类型 1-正常下单 2-BPS重新下单 3-纸质下单
		int type = orderDetail.getType();
		OrderInfo before = orderInfoService.findById(orderDetail.getId());
		OrderDetailVo orderInfo = this.findById(orderDetail.getId());
		orderInfo.setType(type);
		OrderInfo newOrderInfo 	= BeanUtil.a2bDiff(orderDetail, orderInfo, OrderInfo.class);
		newOrderInfo.setId(orderDetail.getId());
		newOrderInfo.setNumber(orderDetail.getNumber());
		if(type == 2){
			// BPS重新下单检查
			if(newOrderInfo.getDiffCount() == 0){
				throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"无任何信息变更，不能重新下单");
			}
			if(!StringUtil.isEmpty(newOrderInfo.getMailNo())){
				throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"运单号发生变更，非重新下单，请纸质下单");
			}
			newOrderInfo.setHandleType(2);
			newOrderInfo.setNumber(commonService.createOrderId());
		}else if(type == 3){
			// 纸质下单检查
			if(StringUtil.isEmpty(newOrderInfo.getMailNo())){
				throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"运单号未变更不能进行纸质下单");
			}
			newOrderInfo.setHandleType(3);
			newOrderInfo.setNumber(commonService.createOrderId());
		}
		if(type == 2){
			Orders orders = ordersService.findById(orderInfo.getOrderId());
			if(orders.getSubNumber().equals(orderDetail.getNumber())){
				orders.setSubNumber(newOrderInfo.getNumber());
				ordersService.updateSubNumber(newOrderInfo.getNumber(),orderInfo.getOrderId());
			}
			
		}
		// 保存订单信息
		newOrderInfo.setjOutRangeType(jOutRangeType);
		newOrderInfo.setjOutRangeTime(jOutRangeTime);
		newOrderInfo.setdOutRangeType(dOutRangeType);
		newOrderInfo.setdOutRangeTime(dOutRangeTime);
		int result = orderInfoService.updateByIdForBoss(newOrderInfo);
		if(result == 0){
			throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"修改保存订单失败");
		}
		String newMailNo = newOrderInfo.getMailNo() == null ? orderInfo.getMailNo() : newOrderInfo.getMailNo();
		String oldMailNo = orderInfo.getMailNo() == null ? "" : orderInfo.getMailNo();
		// 登记订单变更记录
		if(!orderInfo.getNumber().equals(newOrderInfo.getNumber())){
			OrderChangeBook book = new OrderChangeBook();
			book.setOrderId(orderInfo.getId());
			book.setNewOrderNumber(newOrderInfo.getNumber());
			book.setOldOrderNumber(orderInfo.getNumber());
			book.setNewMailNo(newMailNo);
			book.setOldMailNo(oldMailNo);
			orderChangeBookService.insert(book);
		}
		
		// 登记订单日志
//		String diffConent = orderCompareDiffStr(orderDetail, orderInfo, ReflectHelper.getFiledValueMap(orderDetail));
		String diffConent = BeanUtil.a2bDiffStr(orderDetail, orderInfo, ReflectHelper.getFiledValueMap(orderDetail), getFiledDiffEnumMap(orderDetail, orderInfo));
		saveOrderOperateLog(type, newOrderInfo, sysUser, diffConent);
		// 是否发送短信
		if(orderDetail.getSendMsg() != null && orderDetail.getSendMsg()){
			OrderInfo after = orderInfoService.findById(orderDetail.getId());
			smsMessageSupport.sendOrderModifyMsgToJContact(before, after);
			if(!after.getjMobile().equalsIgnoreCase(after.getdMobile())){
				smsMessageSupport.sendOrderModifyMsgToDContact(before, after);
			}
		}
		
		if(type == 1)return;
		/**
		 *  纸质下单或顺丰下单情况，检查是否超过顺丰预约三天，超过三天不要下推等定时任务
		 *  纸质下单修改运单好情况，订单记录下下单情况，队列推送进行区分
		 */
		/**
		 * 检查订单预约取包时间是否在3天内
		 */
		Date date = newOrderInfo.getSendSfUserTime() == null?orderInfo.getSendSfUserTime():newOrderInfo.getSendSfUserTime();
		long diff = DateUtil.getDiffDaysForDay(new Date(), date);
		if(diff > 3){
			LogCvt.info("Boss修改订单，3天外的订单还在任务列中，暂时不发值顺丰，订单ID:"+orderInfo.getId());
			return;
		}
		
		/**
		 * redis 队列处理发送顺丰和微信通知功能，走异步方式
		 * 发订单信息到顺丰接口
		 */
		String mqKey = RedisKeyUtil.mq_order_pay_sucess_redis_key(MqTypeEnum.order_bps_uppdate.getType());
		redisService.lpush(mqKey, String.valueOf(orderInfo.getId()));
	}

	private void saveOrderOperateLog(int type, OrderInfo newOrderInfo, SysUser sysUser, String diffConent) {
		OrderOperateLog log = new OrderOperateLog();
		String action = "订单修改";
		if(type == 2){
			action = "BPS重新下单";
		}else if(type == 3){
			action = "纸质重新下单";
		}
		log.setAction(action);
		log.setDescription("客服修改订单：\n"+diffConent);
		log.setOperatorId(sysUser.getId());
		log.setOperatorName(sysUser.getName());
		log.setOperatorType(OrderOperateTypeEnum.sys_kf.getCode());
		log.setOrderId(newOrderInfo.getId());
		orderOperateLogService.insert(log);
	}
	
	/**
	 *  订单信息推送
	  * @Title: orderPush
	  * @Description: TODO
	  * @author: share 2016年8月27日
	  * @modify: share 2016年8月27日
	  * @param id
	  * @see com.qbt.web.support.OrderSupport#orderPush(int)
	 */
	@Override
	public void orderPush(int id) {
		OrderInfo orderInfo = orderInfoService.findById(id);
		if(orderInfo.getBpsStatus().intValue() == SfBpsStatus.sucess.getCode().intValue()){
			throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"已下发成功的订单不能重发");
		}
		/**
		 * redis 队列处理发送顺丰和微信通知功能，走异步方式
		 * 发订单信息到顺丰接口
		 */
		String mqKey = RedisKeyUtil.mq_order_pay_sucess_redis_key(MqTypeEnum.order_bps_uppdate.getType());
		// 1-正常下单 2-BPS重新下单 3-纸质下单
		if(orderInfo.getHandleType() == 1){
			mqKey = RedisKeyUtil.mq_order_pay_sucess_redis_key(MqTypeEnum.order_pay_sucess.getType());
		}
		redisService.lpush(mqKey, String.valueOf(orderInfo.getId()));
	}
	
	/**
	 *  订单取消
	  * @Title: orderCancel
	  * @Description: TODO
	  * @author: share 2016年8月27日
	  * @modify: share 2016年8月27日
	  * @param orderCancelRefund
	  * @param sysUser
	  * @see com.qbt.web.support.OrderSupport#orderCancel(com.qbt.web.pojo.order.OrderDetailVo, com.qbt.persistent.entity.SysUser)
	 */
	@Override
	public void orderCancel(OrderCancelRefund orderCancelRefund, SysUser sysUser) {
		OrderInfo orderInfo = orderInfoService.findById(orderCancelRefund.getId());
		if(orderInfo == null){
			throw new BusinessException(ErrorCodeEnum.ERROR, "无效订单记录");
		}
		
		if(orderInfo.getOrderStatus() == OrderStatus.kf_close.getCode().intValue()
		  ||orderInfo.getOrderStatus() == OrderStatus.sys_close.getCode().intValue()
		  ||orderInfo.getOrderStatus() == OrderStatus.user_close.getCode().intValue()){
			throw new BusinessException(ErrorCodeEnum.ERROR, "已关闭订单不能取消");
		}
		if(orderInfo.getDeliveryStatus() >= DeliveryStatus.delivery_ing.getCode().intValue()){
			throw new BusinessException(ErrorCodeEnum.ERROR, "快递派送中或已签收不能取消");
		}
		if(orderInfo.getRefundStatus() > RefundStatus.refund_normal.getCode().intValue()){
			throw new WechatException(ErrorCodeEnum.ERROR, "退款订单不能重复取消");
		}
		
		/**
		 * 支付成功退款
		 */
		if(orderInfo.getOrderStatus() == OrderStatus.pay_succ.getCode().intValue()
				||orderInfo.getOrderStatus() == OrderStatus.mail_change.getCode().intValue()){
			
			/**
			 * 订单信息登记退款
			 */
			UserWeixin userWeixin = userWeixinService.findByOpenId(orderInfo.getOpenid());
			int orderPayType = orderPayInfoService.findById(orderInfo.getPayId()).getPayMethod();
			OrderRefund refund = new OrderRefund();
			refund.setAmount(orderInfo.getAmount());
			refund.setRealAmount(orderInfo.getAmount());
			refund.setFee(0d);
			refund.setIsNotice(false);
			refund.setMobile(userWeixin.getMobile());
			refund.setOrderId(orderInfo.getId());
			refund.setStatus(1);
			refund.setUserId(orderInfo.getUserId());
			refund.setUserName(userWeixin.getName());
			refund.setUuid(commonService.createRefundId());
			refund.setOrderPayType(orderPayType);
			refund.setRemark("正常退款");
			// 检查是否当前的订单取消
			if(DateUtil.formatDate(orderInfo.getSendSfUserTime(),DateUtil.DATE_FORMAT_01).equals(DateUtil.formatDate(new Date(),DateUtil.DATE_FORMAT_01))){
				refund.setRealAmount(orderCancelRefund.getRefundAmount());
				refund.setFee(orderCancelRefund.getFeeAmount());
				refund.setRemark(orderCancelRefund.getTipMsg());
			}
			orderRefundService.insert(refund);
			orderInfo.setRefundStatus(RefundStatus.refund_ing.getCode());
			
			/**
			 * 使用优惠券支付的，退回优惠券
			 */
			
//			if(Checker.isNotEmpty(orderInfo.getCouponId()) && orderInfo.getCouponId() > 0 && orderInfo.getCouponCode().length() == 8){
//				orderCouponService.updateByOrderCancel(orderInfo.getCouponId(), orderInfo.getId(),orderInfo.getDiscount());
//				// 如果优惠券使用订单都取消
//				if(orderInfoService.countCouponOrder(orderInfo.getOrderId(),orderInfo.getCouponId()) == 1){
//					couponService.updateStatusById(orderInfo.getCouponId(), 1);
//				}
//			}
			
			/**
			 * 若是合作方下单，取消订单，结算记录至成无效
			 */
			// 如果是合作方免支付下单，添加结算记录
			int thirdId = orderInfo.getThirdId()==null?0:orderInfo.getThirdId();
			if(thirdId > 0){
				OrderSettlement orderSettlement = new OrderSettlement();
				orderSettlement.setOrderId(orderInfo.getId());
				orderSettlement.setSettleState(SettlementStatusEnum.invalid_settlement.getCode());
				orderSettlement.setRemark("订单取消");
				orderSettlementService.updateByOrderId(orderSettlement);
			}
			
			/**
			 * 将优惠活动记录至成无效退款
			 */
			orderActivityBookService.updateStatus(orderInfo.getId());
			
			/**
			 * 将销售二维码人员的订单至成无效
			 */
			saleOrderInfoService.updateStatus(orderInfo.getId(),3);
			
			/**
			 * 登记仪表盘
			 */
			OrderExceptionBook orderExceptionBook = new OrderExceptionBook();
			orderExceptionBook.setOrderId(orderInfo.getId()); 
			orderExceptionBook.setOrderNumber(orderInfo.getNumber());
			orderExceptionBook.setIsDealed(false);
			orderExceptionBook.setOrderStatus(orderInfo.getOrderStatus());
			orderExceptionBook.setDescription("订单支付成功，客服发起取消订单，请确认退款信息和顺丰沟通取消订单");
			orderExceptionBook.setType(ExceptionTypeEnum.check_status.getCode());
			orderExceptionBookService.insert(orderExceptionBook);
			
			// 消息通知
			String accessToken = commonSupport.getAccessToken();
			sendMsg(orderInfo, refund,accessToken);
			// 发送邮件
			emailMsgSupport.orderCancleNotify(orderInfo);
		}
		orderInfo.setOrderStatus(OrderStatus.kf_close.getCode());
		orderInfoService.updateById(orderInfo);
		
		// 记录大订单id
		List<Integer> supOrderId = new ArrayList<Integer>();
		if (orderInfo.getOrderId() != 0) {
			supOrderId.add(orderInfo.getOrderId());
		}
		//检查是否关闭大订单
		orderCommonSupport.closeBigOrder(supOrderId);
		/**
		 * 登记订单日志
		 */
		OrderOperateLog log = new OrderOperateLog();
		log.setAction("取消订单");
		log.setDescription("客服取消订单，已发邮件通知给客服及顺丰工作人员");
		log.setOperatorId(sysUser.getId());
		log.setOperatorName(sysUser.getName());
		log.setOperatorType(OrderOperateTypeEnum.sys_kf.getCode());
		log.setOrderId(orderInfo.getId());
		orderOperateLogService.insert(log);
	}

	/**
	 *  订单客服已签收
	  * @Title: orderConfirmSfRecive
	  * @Description: TODO
	  * @author: share 2016年8月27日
	  * @modify: share 2016年8月27日
	  * @param orderDetail
	  * @param sysUser
	  * @see com.qbt.web.support.OrderSupport#orderConfirmSfRecive(com.qbt.web.pojo.order.OrderDetailVo, com.qbt.persistent.entity.SysUser)
	 */
	@Override
	public void orderConfirmSfRecive(OrderDetailVo orderDetail, SysUser sysUser) {
		OrderInfo orderInfo = orderInfoService.findById(orderDetail.getId());
		if(orderInfo == null){
			throw new WechatException(ErrorCodeEnum.ERROR, "无效订单记录");
		}
		
		if(orderInfo.getDeliveryStatus().intValue() == DeliveryStatus.wait_sf_recive.getCode().intValue()){
			orderInfo.setDeliveryStatus(DeliveryStatus.kf_confirm_recived.getCode());
			orderInfoService.updateById(orderInfo);
			// 消息发送
			sendMsgByRecive(orderInfo);
		}else{
			throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"状态："+DeliveryStatus.getDesc(orderInfo.getDeliveryStatus())+",不能进行修改");
		}
	}
	

	private void sendMsgByRecive(OrderInfo orderInfo) {
		String acceptTime = DateUtil.formatDate(new Date(), DateUtil.DATE_TIME_FORMAT_01);
		// 1、通知寄包人已取件
//		String[] jmobiles = new String[]{orderInfo.getjMobile()};
		if(LanguagesEnum.checkIsJapanese(orderInfo.getLangEname())){
//			smsSendService.sendSms(jmobiles, SmsTemplate.M_120206.getCode(), JapaneseSmsTemplate.getSmsTemplate(SmsTemplate.M_112395.getCode(), SmsMessageTemplate.createM_112395(orderInfo,acceptTime)));
		}else{
			//smsSendService.sendSms(jmobiles, SmsTemplate.M_162520.getCode(), SmsMessageTemplate.createM_162520J(orderInfo,acceptTime));
		}
		// 2、通知收包人已取件
		if(!orderInfo.getjMobile().equals(orderInfo.getdMobile())){
//			jmobiles = new String[]{orderInfo.getdMobile()};
			if(LanguagesEnum.checkIsJapanese(orderInfo.getLangEname())){
//				smsSendService.sendSms(jmobiles, SmsTemplate.M_120206.getCode(), JapaneseSmsTemplate.getSmsTemplate(SmsTemplate.M_112395.getCode(), SmsMessageTemplate.createM_112395(orderInfo,acceptTime)));
			}else{
				//smsSendService.sendSms(jmobiles, SmsTemplate.M_162520.getCode(), SmsMessageTemplate.createM_162520D(orderInfo,acceptTime));
			}
		}
		// 3、球场人员
//		// 寄方
//		if(orderInfo.getjAddrType() != 4){
//			List<UserAssistant> assistantList = userAssistantService.findAssistantBySiteId(orderInfo.getjAddrId(), orderInfo.getjAddrType());
//			if(assistantList != null && !assistantList.isEmpty()){
//				String[] siteMobile = new String[assistantList.size()];
//				int i = 0;
//				for(UserAssistant assistant : assistantList){
//					siteMobile[i] = assistant.getMobile();
//					i++;
//				}
//				smsSendService.sendSms(siteMobile, SmsTemplate.M_124157.getCode(), SmsMessageTemplate.createM_124157(orderInfo,acceptTime));
//			} 
//		}
		// 收方
//		if(orderInfo.getdAddrType() != 4){
//			List<UserAssistant> assistantList = userAssistantService.findAssistantBySiteId(orderInfo.getdAddrId(), orderInfo.getdAddrType());
//			if(assistantList != null && !assistantList.isEmpty()){
//				String[] siteMobile = new String[assistantList.size()];
//				int i = 0;
//				for(UserAssistant assistant : assistantList){
//					siteMobile[i] = assistant.getMobile();
//					i++;
//				}
//				smsSendService.sendSms(siteMobile, SmsTemplate.M_124157.getCode(), SmsMessageTemplate.createM_124157(orderInfo,acceptTime));
//			}
//		}
		
		/**
		 * 微信发送
		 */
		// 1、下单人微信推送
		wechatMessageSupport.orderReciveNotify(orderInfo, acceptTime);
		
	}
	
	private void sendMsg(OrderInfo orderInfo,OrderRefund refund, String accessToken) {
		// 发微信通知
		String openid = orderInfo.getOpenid();
		// 1、下单人微信推送
		wechatMessageSupport.orderCancelNotify(orderInfo, refund);
		// 2、财务人员微信推送
		List<UserWeixin> kfList = userWeixinService.findByKfType(1);
		for (UserWeixin userKf : kfList) {
			openid = userKf.getOpenid();
			wechatMessageSupport.orderCancelCwNotify(orderInfo, openid, refund);
		}
		
		// 短信通知
		// 1、通知寄包人
//		String[] jmobiles = new String[] { orderInfo.getjMobile() };
		if(LanguagesEnum.checkIsJapanese(orderInfo.getLangEname())){
			smsMessageSupport.sendOrderCancelJpMsgToJContact(orderInfo);
		}else{
			smsMessageSupport.sendOrderCancelMsgToJContact(orderInfo);
		}
		if (!orderInfo.getjMobile().equals(orderInfo.getdMobile())) {
			// 2、通知收包人
//			String[] dmobiles = new String[] { orderInfo.getdMobile() };
			if(LanguagesEnum.checkIsJapanese(orderInfo.getLangEname())){
				smsMessageSupport.sendOrderCancelJpMsgToDContact(orderInfo);
			}else{
				smsMessageSupport.sendOrderCancelMsgToDContact(orderInfo);
			}
		}

		// 球包助理
		// 3、球场人员
		// 寄方
//		if (orderInfo.getjAddrType() != 4) {
//			List<UserAssistant> assistantList = userAssistantService.findAssistantBySiteId(orderInfo.getjAddrId(), orderInfo.getjAddrType());
//			if (assistantList != null && !assistantList.isEmpty()) {
//				String[] siteMobile = new String[assistantList.size()];
//				int i = 0;
//				for (UserAssistant assistant : assistantList) {
//					siteMobile[i] = assistant.getMobile();
//					i++;
//				}
//				smsSendService.sendSms(siteMobile, SmsTemplate.M_112645.getCode(), SmsMessageTemplate.createM_112645(orderInfo));
//			}
//		}
		// 收方
//		if (orderInfo.getdAddrType() != 4) {
//			List<UserAssistant> assistantList = userAssistantService.findAssistantBySiteId(orderInfo.getdAddrId(), orderInfo.getdAddrType());
//			if (assistantList != null && !assistantList.isEmpty()) {
//				String[] siteMobile = new String[assistantList.size()];
//				int i = 0;
//				for (UserAssistant assistant : assistantList) {
//					siteMobile[i] = assistant.getMobile();
//					i++;
//				}
//				smsSendService.sendSms(siteMobile, SmsTemplate.M_112645.getCode(), SmsMessageTemplate.createM_112645(orderInfo));
//			}
//		}
		
	}
	
	/**
	 *  Boss下单
	  * @Title: createOrder
	  * @Description: TODO
	  * @author: share 2016年10月25日
	  * @modify: share 2016年10月25日
	  * @param orderConfirm
	  * @see com.qbt.web.support.OrderSupport#createOrder(com.qbt.web.pojo.order.OrderConfirm)
	 */
	@Override
	public void createOrder(OrderConfirm orderConfirm) {
		/**
		 * 业务订单校验
		 */
		// 预约取包时间校验
		String timeSection = orderConfirm.getConsigned_time();
		Date consignedTime = DateUtil.str2Date(timeSection.substring(0,16)+":00", DateUtil.DATE_TIME_FORMAT_01);
		if(consignedTime.getTime() < System.currentTimeMillis()){
			throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"取包时间已失效");
		}
		
		if(orderConfirm.getSrc_area_id() == orderConfirm.getDest_area_id() && orderConfirm.getSrc_address().equals(orderConfirm.getDest_address())){
			throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"取包和送包地址不能相同");
		}
		
		// 替换地区ID的市名称
		BaseSfArea jArea =  baseSfAreaService.findById(orderConfirm.getSrc_area_id());
		jArea = baseSfAreaService.findById(jArea.getParentId());
		orderConfirm.setSrc_city(jArea.getFullName());
		BaseSfArea dArea =  baseSfAreaService.findById(orderConfirm.getDest_area_id());
		dArea = baseSfAreaService.findById(dArea.getParentId());
		orderConfirm.setDest_city(dArea.getFullName());
		
		/**
		 *  订单价格校验
		 */
		// 顺丰速运价格计算
		if(orderConfirm.getSrc_type() == 1){
			orderConfirm.setSrc_course_id(orderConfirm.getSrc_id());
		}
		if(orderConfirm.getDest_type() == 1){
			orderConfirm.setDest_course_id(orderConfirm.getDest_id());
		}
		double sfPrice = orderConfirm.getFreight();
		
		
		// 加急费
		Integer urgentFee = 0;
		
		// 加急价格
		if("3".equals(orderConfirm.getBusiness_type())) {
			int src_area_id = orderConfirm.getSrc_area_id();
			int dest_area_id = orderConfirm.getDest_area_id();
			
			
			BaseSfArea fromArea = baseSfAreaService.findById(src_area_id);
			BaseSfArea toArea = baseSfAreaService.findById(dest_area_id);
			
			UrgentPageReqVo reqCityVo = new UrgentPageReqVo();
			reqCityVo.setFromCity(fromArea.getParentId()+"");
			reqCityVo.setToCity(toArea.getParentId()+"");		
			
			PageEntity<Urgent> pageEntity = BeanUtil.pageConvert(reqCityVo, Urgent.class);
			
			List<Urgent> urgents = urgentService.findByPage(pageEntity);			
			if(urgents != null && urgents.size()>0) {
				urgentFee = urgents.get(0).getPrice();
			}
		
			// 加急类型其实是一个快件
			orderConfirm.setBusiness_type("1");
		}
		
		int insureValue = orderConfirm.getInsure_price();
		// 保价金额
		BaseInsuredTemplate template = baseInsTempSupport.insureValidPrice(orderConfirm.getInsure_price());
		Double insureRate = template.getInsuredRates().doubleValue();
		double free = template.getFreeInsured().doubleValue();
		double insurePrice = Arith.mul(Arith.mul(insureValue, 10000),insureRate) - Arith.mul(Arith.mul(free, 10000),insureRate);
		if(insurePrice < 0)insurePrice=0d;
		// 1-course球场，2-airport机场,3-bank银行,5-顺丰网店
		int srcType = orderConfirm.getSrc_type();
		int srcId = orderConfirm.getSrc_id();
		String jAddrName = "",dAddrName = "";
		jAddrName = getAddrName(srcType, srcId);
		int destType = orderConfirm.getDest_type();
		int destId = orderConfirm.getDest_id();
		dAddrName = getAddrName(destType, destId);
		
		/**
		 * 满减
		 */
		OrderActivityRequestVo orderActivityVo = new OrderActivityRequestVo();
		orderActivityVo.setdAddrId(destId);
		orderActivityVo.setdAddrName(dAddrName);
		orderActivityVo.setdAddrType(destType);
		orderActivityVo.setdAreaId(orderConfirm.getDest_area_id());
		orderActivityVo.setFreight(orderConfirm.getFreight());
		orderActivityVo.setjAddrId(srcId);
		orderActivityVo.setjAddrName(jAddrName);
		orderActivityVo.setjAddrType(srcType);
		orderActivityVo.setjAreaId(orderConfirm.getSrc_area_id());
		OrderActivityResponseVo orderActivity = activityOffSupport.checkActivity(orderActivityVo);
		double  discount = 0;
		if(orderActivity.getjActivity()){
			discount = Arith.add(discount, orderActivity.getjAmount());
		}else if(orderActivity.getdActivity()){
			discount = Arith.add(discount, orderActivity.getdAmount());
		}
		double inputDiscount = orderConfirm.getInputDiscount();
		
		double totalPrice = Arith.add(sfPrice, insurePrice);
		double realPrice = Arith.sub(totalPrice, discount);
		LogCvt.info("保价金额："+insurePrice+",实付金额:"+realPrice+",手动优惠:"+inputDiscount);
		if(realPrice != orderConfirm.getPrice() || realPrice <= 0){
			throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"订单金额无效");
		}
		
		/**
		 * VIP 订单
		 */
		VipOrderConfirmRequest vipOrderConfirmReqVo = new VipOrderConfirmRequest();
		vipOrderConfirmReqVo.setConsignedTime(orderConfirm.getConsigned_time());
		vipOrderConfirmReqVo.setjMobile(orderConfirm.getSrc_moibile());
		vipOrderConfirmReqVo.setjName(orderConfirm.getSrc_name());
		vipOrderConfirmReqVo.setdMobile(orderConfirm.getDest_moibile());
		vipOrderConfirmReqVo.setdName(orderConfirm.getDest_name());
		vipOrderConfirmReqVo.setSrcCourseId(orderConfirm.getSrc_course_id());
		vipOrderConfirmReqVo.setDestCourseId(orderConfirm.getDest_course_id());
		vipOrderConfirmReqVo.setNum(orderConfirm.getQuantity());
		vipOrderConfirmReqVo.setSrc_area_id(orderConfirm.getSrc_area_id());
		vipOrderConfirmReqVo.setSrc_address(orderConfirm.getSrc_address());
		vipOrderConfirmReqVo.setDest_area_id(orderConfirm.getDest_area_id());
		vipOrderConfirmReqVo.setDest_address(orderConfirm.getDest_address());
		VipOrderConfirmResponse vipOrderConfirm = vipCommonSupport.checkVipOrder(vipOrderConfirmReqVo);
		int num = vipOrderConfirm.getNum();
		boolean isVipOrder = vipOrderConfirm.isVipOrder();
		int[] vipIds = vipOrderConfirm.getVipId();
		double[] vipOrderAmounts = vipOrderConfirm.getVipOrderAmount();
		double outRangeAmount = vipOrderConfirm.getOutRangeAmount();
		
		double orderTotalPrice = Arith.mul(orderConfirm.getQuantity()-num,realPrice);
		// 去除手动优惠金额
		orderTotalPrice = Arith.sub(orderTotalPrice, Arith.mul(orderConfirm.getQuantity()-num,inputDiscount));
		if(orderTotalPrice < 0)orderTotalPrice=0;
		if(isVipOrder){
			for(double vipOrderAmount : vipOrderAmounts){
				double vipTotalPrice = Arith.add(vipOrderAmount, insurePrice);
				vipTotalPrice = Arith.add(vipTotalPrice, outRangeAmount);
				vipTotalPrice = Arith.sub(vipTotalPrice, inputDiscount);
				if(vipTotalPrice < 0)vipTotalPrice=0;
				orderTotalPrice = Arith.add(orderTotalPrice,vipTotalPrice);
			}
		}
		// 去除手动优惠金额
		LogCvt.info("保价金额："+insurePrice+",实付金额:"+realPrice+",计算总金额:"+orderTotalPrice);
		if(orderTotalPrice != orderConfirm.getTotalPrice()){
			throw new BusinessException(ErrorCodeEnum.ERROR_VALID_FAIL,"订单金额无效");
		}
		
		List<OrderInfo> subOrderList = new ArrayList<OrderInfo>();
		
		//时效校验
		String deliverDate = orderConfirm.getDeliver_time();
		if(consignedTime.getTime() > DateUtil.str2Date(deliverDate, DateUtil.DATE_TIME_FORMAT_01).getTime()){
			throw new WechatException(ErrorCodeEnum.ERROR_VALID_FAIL, "取件时间不能大于到达时间");
		}
		
		/**
		 * 订单信息组装
		 */
		// 订单信息
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setProxySelfUserId(orderConfirm.getUserId());
		orderInfo.setUserId(orderConfirm.getUserId());
		orderInfo.setAmount(orderConfirm.getPrice());
		orderInfo.setTotalAmount(totalPrice);
		orderInfo.setInputDiscount(inputDiscount);
		orderInfo.setReason(orderConfirm.getReason());
		if(isVipOrder){
			double vipDiscount =  sfPrice < vipOrderAmounts[0]?0:Arith.sub(Arith.sub(sfPrice, vipOrderAmounts[0]), outRangeAmount);
			double vipTotalPrice = Arith.add(vipOrderAmounts[0], insurePrice);
			vipTotalPrice = Arith.add(vipTotalPrice, outRangeAmount);
			orderInfo.setAmount(vipTotalPrice);
			
			orderInfo.setDiscount(vipDiscount);
			if(vipDiscount==0){
				orderInfo.setFavorableDesc("");
				orderInfo.setVipId(0);
				orderInfo.setIsVip(false);

			}else{
				orderInfo.setFavorableDesc("会员卡订单");
				orderInfo.setVipId(vipIds[0]);
				orderInfo.setIsVip(true);
			}
		}else{
			orderInfo.setAmount(realPrice);
			orderInfo.setDiscount(discount);
			orderInfo.setFavorableDesc(orderActivity.getActivitName());
			orderInfo.setIsVip(false);
		}
		double orderInfoRealPrice = Arith.sub(orderInfo.getAmount(),inputDiscount);
		if(orderInfoRealPrice <0)orderInfoRealPrice=0;
		orderInfo.setAmount(orderInfoRealPrice);
		
				
		orderInfo.setOrderStatus(OrderStatus.pay_succ.getCode());
		orderInfo.setRefundStatus(RefundStatus.refund_normal.getCode());
		orderInfo.setDeliveryStatus(DeliveryStatus.un_send_sf.getCode());
		orderInfo.setBpsStatus(SfBpsStatus.un_send.getCode());
		orderInfo.setQuestionStatus(QuestionStatus.normal.getCode());
		orderInfo.setOrderSource(OrderSource.boss.getCode());
		orderInfo.setSendSfUserTime(consignedTime);
		orderInfo.setFreight(sfPrice);
		orderInfo.setUrgentFee(urgentFee);
		orderInfo.setInsuredFee(insurePrice);
		orderInfo.setInsuredRates(insureRate);
		orderInfo.setInsuredValue((double)orderConfirm.getInsure_price());
		orderInfo.setExpressType(orderConfirm.getBusiness_type());
		orderInfo.setIsHandleQuestion(true);
		orderInfo.setDeliverDate(deliverDate);
		orderInfo.setSendTimeSection(orderConfirm.getConsigned_time());
		// 寄件信息
		orderInfo.setjAddrType(orderConfirm.getSrc_type());
		orderInfo.setjAddress(orderConfirm.getSrc_address());
		orderInfo.setjAddrId(orderConfirm.getSrc_type() != 4?orderConfirm.getSrc_id():0);
		orderInfo.setjAddrName(jAddrName);
		orderInfo.setjAreaId(orderConfirm.getSrc_area_id());
		orderInfo.setjContact(orderConfirm.getSrc_name());
		orderInfo.setjMobile(orderConfirm.getSrc_moibile());
		orderInfo.setjTreePath(orderConfirm.getSrc_province()+","+orderConfirm.getSrc_city()+","+orderConfirm.getSrc_district());
		// 收件信息
		orderInfo.setdAddrType(orderConfirm.getDest_type());
		orderInfo.setdAddress(orderConfirm.getDest_address());
		orderInfo.setdAddrId(orderConfirm.getDest_type() != 4?orderConfirm.getDest_id():0);
		orderInfo.setdAddrName(dAddrName);
		orderInfo.setdAreaId(orderConfirm.getDest_area_id());
		orderInfo.setdContact(orderConfirm.getDest_name());
		orderInfo.setdMobile(orderConfirm.getDest_moibile());
		orderInfo.setdTreePath(orderConfirm.getDest_province()+","+orderConfirm.getDest_city()+","+orderConfirm.getDest_district());
		// other
		orderInfo.setIsHelped(false);
		orderInfo.setIsDelete(false);
		orderInfo.setIsOrg(false);
		orderInfo.setIsBagCode(false);
		orderInfo.setRemark(orderConfirm.getRemark()==null?"":orderConfirm.getRemark());
		orderInfo.setOpenid(orderConfirm.getOpenid());
		orderInfo.setLangEname(orderConfirm.getLangName());
		orderInfo.setPlayTime(orderConfirm.getPlayTime());
		// thrid info
		orderInfo.setThirdType(1);
		orderInfo.setThirdId(0);
		// boss代下单：客服收款：否，财务收款：否。
		orderInfo.setKfReceivableStatus(2);
		orderInfo.setCwReceivableStatus(2);
		
		// 偏远寄件信息
		BaseCourse jCourse = baseCourseService.findById(orderInfo.getjAddrId());
		BaseCourse dCourse = baseCourseService.findById(orderInfo.getdAddrId());
		OutRangeAddress jAddress = outRangeAddressService.findByAreaId(orderInfo.getjAreaId(), orderInfo.getjAddress());
		OutRangeAddress dAddress = outRangeAddressService.findByAreaId(orderInfo.getdAreaId(), orderInfo.getdAddress());;
		if(orderInfo.getjAddrType() == AddrTypeEnum.address.getType() && Checker.isNotEmpty(jAddress)) {
			orderInfo.setjOutRangeType(4);
			orderInfo.setjOutRangeTime(jAddress.getOutRangeTime());
			orderInfo.setjOutRangePrice(jAddress.getOutRangePrice());
		}else if(orderInfo.getjAddrType() == AddrTypeEnum.course.getType() && Checker.isNotEmpty(jCourse)){
			orderInfo.setjOutRangeType(jCourse.getIsOutRange() ? 1 : 0);
			orderInfo.setjOutRangeTime(jCourse.getOutRangeTime());
			orderInfo.setjOutRangePrice(jCourse.getOutRangePrice());
		}else{
			orderInfo.setjOutRangeType(0);
			orderInfo.setjOutRangeTime(0d);
			orderInfo.setjOutRangePrice(0d);
		}
		// 偏远收件信息
		if(orderInfo.getdAddrType() == AddrTypeEnum.address.getType() && Checker.isNotEmpty(dAddress)) {
			orderInfo.setdOutRangeType(4);
			orderInfo.setdOutRangeTime(dAddress.getOutRangeTime());
			orderInfo.setdOutRangePrice(dAddress.getOutRangePrice());
		}else if(orderInfo.getdAddrType() == AddrTypeEnum.course.getType() && Checker.isNotEmpty(dCourse)){
			orderInfo.setdOutRangeType(dCourse.getIsOutRange() ? 1 : 0);
			orderInfo.setdOutRangeTime(dCourse.getOutRangeTime());
			orderInfo.setdOutRangePrice(dCourse.getOutRangePrice());
		}else{
			orderInfo.setdOutRangeType(0);
			orderInfo.setdOutRangeTime(0d);
			orderInfo.setdOutRangePrice(0d);
		}
		//检查是否首单
		long count = orderInfoService.orderCount(orderInfo);
		if(count == 1){
			orderInfo.setFirstOrder(true);
		}
		subOrderList.add(orderInfo);
		
		/**
		 * 添加大订单
		 */
		String subNumber = commonService.createOrderId();
		Orders order = new Orders();
		if(orderInfo.getdAddrType() == 4){
			order.setdAddrName(orderInfo.getdAddress());
		}else{
			order.setdAddrName(orderInfo.getdAddrName());
		}
		order.setdContact(orderInfo.getdContact());
		order.setdMobile(orderInfo.getdMobile());
		order.setdTreePath(orderInfo.getdTreePath());
		order.setIsDelete(false);
		order.setIsHelped(orderInfo.getIsHelped());
		if(orderInfo.getjAddrType() == 4){
			order.setjAddrName(orderInfo.getjAddress());
		}else{
			order.setjAddrName(orderInfo.getjAddrName());
		}
		order.setjContact(orderInfo.getjContact());
		order.setjMobile(orderInfo.getjMobile());
		order.setjTreePath(orderInfo.getjTreePath());
		order.setNumber(commonService.createBigOrderId());
		order.setOpenId(orderInfo.getOpenid());
		order.setOrderSource(orderInfo.getOrderSource());
		order.setOrderStatus(BigOrderStatus.normal.getCode());
		order.setPayMethod(PaymethodEnum.boss_pay.getCode());
		order.setPayStatus(BigOrderPayStatus.paid.getCode());
		order.setPayTime(new Date());
		order.setQuantity(orderConfirm.getQuantity());
		order.setThirdId(orderInfo.getThirdId());
		order.setThirdName(orderInfo.getThirdName());
		order.setThirdType(orderInfo.getThirdType());
		order.setAssistantName(orderInfo.getAssistantName());
		order.setUserId(orderConfirm.getUserId());
		order.setSubNumber(subNumber);
		int orderId = ordersService.insert(order);
		// 添加支付流水
		OrderPayInfo payment = new OrderPayInfo();
		payment.setNonceStr("");
		payment.setOrderId(orderId);
		payment.setUuid(commonService.createPaymentId());
		payment.setPayMethod(PaymethodEnum.boss_pay.getCode());
		payment.setPaySign("");
		payment.setStatus(PaymentStatus.success.getCode());
		payment.setPrepayId("");
		payment.setPayStatus(PayStatus.init.getCode());
		payment.setPayAmount(orderConfirm.getTotalPrice());
		payment.setUserId(orderConfirm.getUserId());
		payment.setCouponCode("");
		payment.setCouponId(0);
		payment.setActivityId(0);
		payment.setActivityType(0);
		orderPayInfoService.insert(payment);
		// 添加第一个子订单，冗余在大订单上
		String uuid = ToolsUtil.getUUID();
		orderInfo.setNumber(subNumber);
		orderInfo.setOrderId(orderId);
		orderInfo.setOrderNumber(order.getNumber());
		orderInfo.setMd5(uuid);
		orderInfo.setPayId(payment.getId());
		orderInfo.setPayTime(payment.getCreateTime());
		orderInfoService.inser(orderInfo);
		checkVipOrder(orderInfo);
		saveActivityOffBook(orderInfo, orderActivity);
		// 下发BSP
		sendSfBsp(orderInfo);
		saveUserContact(orderInfo);
		 //添加历史联系人
		addUserOrderContact(orderInfo);
		
		for(int i = 1 ; i < order.getQuantity(); i++){
			OrderInfo subOrderInfo = BeanUtil.a2b(orderInfo, OrderInfo.class);
			
			if(i < num){
				double vipDiscount =  sfPrice < vipOrderAmounts[i]?0:Arith.sub(Arith.sub(sfPrice, vipOrderAmounts[i]), outRangeAmount);
				double vipTotalPrice = Arith.add(vipOrderAmounts[i], insurePrice);
				vipTotalPrice = Arith.add(vipTotalPrice, outRangeAmount);
				subOrderInfo.setAmount(vipTotalPrice);
				subOrderInfo.setDiscount(vipDiscount);
				if(vipDiscount==0){
					subOrderInfo.setFavorableDesc("");
					subOrderInfo.setVipId(0);
					subOrderInfo.setIsVip(false);
				}else{
					subOrderInfo.setFavorableDesc("会员卡订单");
					subOrderInfo.setVipId(vipIds[i]);
					subOrderInfo.setIsVip(true);
				}
			}else{
				subOrderInfo.setMd5(uuid);
				subOrderInfo.setAmount(realPrice);
				subOrderInfo.setTotalAmount(totalPrice);
				subOrderInfo.setDiscount(discount);
				subOrderInfo.setFavorableDesc(orderActivity.getActivitName());
				subOrderInfo.setIsVip(false);
			}
			
			orderInfoRealPrice = Arith.sub(subOrderInfo.getAmount(),inputDiscount);
			if(orderInfoRealPrice <0)orderInfoRealPrice=0;
			subOrderInfo.setAmount(orderInfoRealPrice);
			
			subOrderInfo.setNumber(commonService.createOrderId());
			subOrderInfo.setOrderId(orderId);
			subOrderInfo.setOrderNumber(order.getNumber());
			subOrderInfo.setMd5(uuid);
			subOrderInfo.setFirstOrder(false);
			subOrderInfo.setPayId(payment.getId());
			subOrderInfo.setPayTime(payment.getCreateTime());
			orderInfoService.inser(subOrderInfo);
			saveActivityOffBook(orderInfo, orderActivity);
			subOrderList.add(subOrderInfo);
			// 下发BSP
			sendSfBsp(subOrderInfo);
			saveUserContact(subOrderInfo);
			/**
			 * 记录订单日志
			 */
			OrderOperateLog log = new OrderOperateLog();
			log.setAction("下单");
			log.setDescription("boss代下单");
			log.setOperatorId(orderInfo.getUserId());
			log.setOperatorName(orderConfirm.getUserName());
			log.setOperatorType(OrderOperateTypeEnum.weixin_user.getCode());
			log.setOrderId(orderInfo.getId());
			orderOperateLogService.insert(log);
		}

		// 短信消息发送
		for(OrderInfo suborder : subOrderList){
			messageAndWeixinSend(suborder);
		}
		
	}
	
	/**
	 * 添加历史联系人
	 * @param orderInfo
	 * * @author: wuwang 2017年6月27日
	  * @modify: wuwang 2017年6月27日
	 */
	private void addUserOrderContact(OrderInfo orderInfo) {
		String juuid = Md5.MD5Encode(""+orderInfo.getUserId()+orderInfo.getjContact()+orderInfo.getjMobile());
		//寄
		UserOrderContact j_uoc = userOrderContactService.findByUuid(juuid);
		if(j_uoc == null){
			j_uoc = new UserOrderContact();
			j_uoc.setUserId(orderInfo.getUserId());
			j_uoc.setContact(orderInfo.getjContact());
			j_uoc.setMobile(orderInfo.getjMobile());
			j_uoc.setUuid(juuid);
			userOrderContactService.insert(j_uoc);
		}
		
		String duuid = Md5.MD5Encode(""+orderInfo.getUserId()+orderInfo.getdContact()+orderInfo.getdMobile());
		//收
		UserOrderContact d_uoc = userOrderContactService.findByUuid(duuid);
		if(d_uoc == null){
			d_uoc = new UserOrderContact();
			d_uoc.setUserId(orderInfo.getUserId());
			d_uoc.setContact(orderInfo.getdContact());
			d_uoc.setMobile(orderInfo.getdMobile());
			d_uoc.setUuid(duuid);
			userOrderContactService.insert(d_uoc);
		}
	}

	/**
	 *  更新VIP卡的使用状态
	  * @Title: checkVipOrder
	  * @Description: TODO
	  * @author: share 2017年1月4日
	  * @modify: share 2017年1月4日
	  * @param orderInfo
	 */
	private void checkVipOrder(OrderInfo orderInfo) {
		if(orderInfo.getIsVip()){
			VipInfo vipInfo = vipUserService.findById(orderInfo.getVipId());
			if(vipInfo == null){
				LogCvt.error("支付成功，获取VIP用户信息失败...");
				return;
			}
			
			if(!vipInfo.getUsedOrder()){
				vipInfo.setUsedOrder(true);
				vipUserService.updateUsedOrder(vipInfo);
			}
		}
		
	}
	
	private void saveActivityOffBook(OrderInfo orderInfo,OrderActivityResponseVo orderActivity) {
			// 优惠订单登记簿
		if(orderActivity.getActivityId() > 0){
			OrderActivityBook orderActivityBook = new OrderActivityBook();
			orderActivityBook.setActivityId(orderActivity.getActivityId());
			orderActivityBook.setActivityName(orderActivity.getActivitName());
			orderActivityBook.setAmount(orderInfo.getAmount());
			orderActivityBook.setDiscount(orderInfo.getDiscount());
			orderActivityBook.setOrderId(orderInfo.getId());
			orderActivityBook.setStatus(1);
			orderActivityBook.setTotalAmount(orderInfo.getTotalAmount());
			orderActivityBook.setType(orderActivity.getActivityType());
			orderActivityBookService.insert(orderActivityBook);
		}
		
	}
	
	/**
	 *   异步消息推送
	  * @Title: messageAndWeixinSend
	  * @Description: TODO
	  * @author: share 2016年10月19日
	  * @modify: share 2016年10月19日
	  * @param orderInfo
	 */
	private void messageAndWeixinSend(OrderInfo orderInfo){
		String mqKey = RedisKeyUtil.mq_order_pay_sucess_redis_key(MqTypeEnum.order_pay_message.getType());
		redisService.lpush(mqKey, String.valueOf(orderInfo.getId()));
	}
	
	/**
	 *  发送顺丰BSP
	  * @Title: sendSfBsp
	  * @Description: TODO
	  * @author: share 2016年10月19日
	  * @modify: share 2016年10月19日
	  * @param orderInfo
	 */
	public void sendSfBsp(OrderInfo orderInfo){
		Date date = orderInfo.getSendSfUserTime();
		long diff = DateUtil.getDiffDaysForDay(orderInfo.getCreateTime(), date);
		if(diff > 3){
			LogCvt.info("3天外的订单添加到任务列中，暂时不发值顺丰，订单ID:"+orderInfo.getId());
		   Date dd = DateUtil.calDateOfDay(orderInfo.getSendSfUserTime(),-3);
		   Date delayTime = DateUtil.str2Date(DateUtil.formatDate(dd, DateUtil.DATE_FORMAT_01)+" 08:00:00",DateUtil.DATE_TIME_FORMAT_01);
			// 记录到任务列中
			OrderDelayTask task = new OrderDelayTask();
			task.setOrderId(orderInfo.getId());
			task.setReason("超约3天，延迟下单");
			task.setStatus(1);
			task.setDelayTime(delayTime);
			orderDelayTaskService.insert(task);
			return;
		}
		
		/**
		 * redis 队列处理发送顺丰和微信通知功能，走异步方式
		 * 发订单信息到顺丰接口
		 */
		LogCvt.info("订单发送至顺丰，订单Number:"+orderInfo.getNumber()+"订单Id:"+orderInfo.getId());
		String mqKey = RedisKeyUtil.mq_order_pay_sucess_redis_key(MqTypeEnum.order_pay_sucess.getType());
		redisService.lpush(mqKey, String.valueOf(orderInfo.getId()));
	}
	
	/**
	 *  保存常用联系人
	  * @Title: saveUserContact
	  * @Description: TODO
	  * @author: share 2016年10月19日
	  * @modify: share 2016年10月19日
	  * @param orderInfo
	 */
	private void saveUserContact(OrderInfo orderInfo) {
		try{
			// 寄件人
			UserContact jContact = userContactService.findByMobile(orderInfo.getjMobile());
			if(Checker.isEmpty(jContact)){
				jContact = new UserContact();
				jContact.setMobile(orderInfo.getjMobile());
				jContact.setName(orderInfo.getjContact());
				jContact.setIsDelete(false);
				jContact.setType(1);
				userContactService.insert(jContact);
			}else if(jContact.getType() == 0){
				jContact.setType(1);
				userContactService.updateById(jContact);
			}
			// 收件人
			UserContact dContact = userContactService.findByMobile(orderInfo.getdMobile());
			if (Checker.isEmpty(dContact)) {
				dContact = new UserContact();
				dContact.setMobile(orderInfo.getdMobile());
				dContact.setName(orderInfo.getdContact());
				dContact.setIsDelete(false);
				dContact.setType(1);
				userContactService.insert(dContact);
			}else if(dContact.getType() == 0){
				dContact.setType(1);
				userContactService.updateById(dContact);
			}
		}catch(Exception e){
			LogCvt.error(e.getMessage(),e);
		}
	}
	
	/**
	 *  获取站点名称
	  * @Title: getAddrName
	  * @Description: TODO
	  * @author: share 2016年8月13日
	  * @modify: share 2016年8月13日
	  * @param srcType
	  * @param srcId
	  * @return
	 */
	private String getAddrName(int srcType, int srcId) {
		 String jAddrName = "";
		switch (AddrTypeEnum.getByType(srcType)) {
			case course:
				jAddrName = baseCourseService.findById(srcId).getName();
				break;
			case airport:
				jAddrName = baseAirportService.findById(srcId).getName();
				break;
			case bank:
				jAddrName = baseBankBranchService.findById(srcId).getName();
				break;
			case sfSite:
				jAddrName = baseSfSiteService.findById(srcId).getName();
				break;
			default:
				break;
		}
		return jAddrName;
	}

	
	/**
	 *  订单取消计算退款金额信息
	  * @Title: orderCancelCalcRefund
	  * @Description: TODO
	  * @author: share 2016年11月3日
	  * @modify: share 2016年11月3日
	  * @param orderDetail
	  * @return
	  * @see com.qbt.web.support.OrderSupport#orderCancelCalcRefund(com.qbt.web.pojo.order.OrderDetailVo)
	 */
	@Override
	public OrderCancelRefund orderCancelCalcRefund(OrderDetailVo orderDetail) {
		OrderCancelRefund refund = new OrderCancelRefund();
		OrderInfo orderInfo = orderInfoService.findById(orderDetail.getId());
		if(orderInfo == null){
			throw new BusinessException(ErrorCodeEnum.ERROR, "无效订单记录");
		}
		
		if(orderInfo.getOrderStatus() == OrderStatus.kf_close.getCode().intValue()
		  ||orderInfo.getOrderStatus() == OrderStatus.sys_close.getCode().intValue()
		  ||orderInfo.getOrderStatus() == OrderStatus.user_close.getCode().intValue()){
			throw new BusinessException(ErrorCodeEnum.ERROR, "已关闭订单不能取消");
		}
		if(orderInfo.getDeliveryStatus() >= DeliveryStatus.delivery_ing.getCode().intValue()){
			throw new BusinessException(ErrorCodeEnum.ERROR, "快递派送中或已签收不能取消");
		}
		if(orderInfo.getRefundStatus() > RefundStatus.refund_normal.getCode().intValue()){
			throw new WechatException(ErrorCodeEnum.ERROR, "退款订单不能重复取消");
		}
		
		/**
		 * 支付成功退款
		 */
		refund.setId(orderDetail.getId());
		if(orderInfo.getOrderStatus() == OrderStatus.pay_succ.getCode().intValue()
				||orderInfo.getOrderStatus() == OrderStatus.mail_change.getCode().intValue()){
			refund.setTotalAmount(orderInfo.getAmount());
			refund.setRefundAmount(orderInfo.getAmount());
			refund.setFeeAmount(0d);
			refund.setTip(false);
			refund.setTipMsg("非当天取件单，全额退款。");
			// 检查是否当前的订单取消
			if(DateUtil.formatDate(orderInfo.getSendSfUserTime(),DateUtil.DATE_FORMAT_01).equals(DateUtil.formatDate(new Date(),DateUtil.DATE_FORMAT_01))){
				refund.setTip(true);
				if(orderInfo.getDeliveryStatus().intValue() <= DeliveryStatus.wait_sf_recive.getCode().intValue()
					&&
					orderInfo.getSendSfUserTime().getTime() >= System.currentTimeMillis()
					){
					double fee = 20;
					 double realAmout = Arith.sub(orderInfo.getAmount(), fee);
					if(realAmout < 0){
						realAmout = 0;
						fee = orderInfo.getAmount();
					}
					refund.setRefundAmount(realAmout);
					refund.setFeeAmount(fee);
					refund.setTipMsg("球包未收取，未超过预约时间取消，退部分金额");
				}else{
					refund.setRefundAmount(0);
					refund.setFeeAmount(orderInfo.getAmount());
					if(orderInfo.getSendSfUserTime().getTime() < System.currentTimeMillis()){
						refund.setTipMsg("球包已过预约取件时间，全额不退。若是问题件退款金额请调整。");
					}else{
						refund.setTipMsg("球包已取包，全额不退。若是问题件退款金额请调整。");
					}
					
				}
			}
		}
		return refund;
	}

	@Override
	public boolean receivable(Integer id, Integer receivableType, SysUser sysUser) {
		OrderInfo orderInfo = orderInfoService.findById(id);
		if(orderInfo.getOrderStatus() != OrderStatus.pay_succ.getCode()){
			throw new BusinessException("非有效订单不能收款");
		}
		String receivableTypeStr = "";
		switch (receivableType) {
		case 1://1-客服
			receivableTypeStr = "客服";
			orderInfo.setKfReceivableStatus(1);
			break;
		case 2://2-财务
			receivableTypeStr = "财务";
			orderInfo.setCwReceivableStatus(1);
			if(orderInfo.getKfReceivableStatus() == 2){
				//如果客服为未收款状态,更新为已收款状态
				orderInfo.setKfReceivableStatus(1);
			}
			break;
		default:
			break;
		}
		int result = orderInfoService.updateByIdForBoss(orderInfo);
		//记录订单操作记录
		OrderOperateLog log = new OrderOperateLog();
		log.setAction("订单收款");
		log.setDescription(receivableTypeStr + "已收款");
		log.setOperatorId(sysUser.getId());
		log.setOperatorName(sysUser.getName());
		log.setOperatorType(OrderOperateTypeEnum.sys_kf.getCode());
		log.setOrderId(orderInfo.getId());
		orderOperateLogService.insert(log);
		return result > 0;
	}
	
	
	public void updateMailChannelById(SysUser sysUser,Integer id,Integer mailChannel,String mailNumber) {
		OrderInfo oldOrder= orderInfoService.findById(id);
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setId(id);
		orderInfo.setMailChannel(mailChannel);
		orderInfo.setMailNumber(mailNumber);;
		
		// 如果 从顺丰 修改成 非顺丰渠道  发送邮件，取消订单，并修改 订单 状态 为8
		if(oldOrder.getMailChannel()==1 && mailChannel!=1){
			emailMsgSupport.orderCancleNotify(oldOrder);
			orderInfo.setOrderStatus(OrderStatus.mail_change.getCode());
		}
		/**
		 * 登记订单日志
		 */
		
		MailChannel oldMailChannel=mailChannelService.findById(oldOrder.getMailChannel());
		MailChannel newMailChannel=mailChannelService.findById(mailChannel);

		
		OrderOperateLog log = new OrderOperateLog();
		log.setAction("修改渠道");
		log.setDescription("修改前："+oldMailChannel.getChannelName()+","+getMailNo(oldOrder)+",修改后："+newMailChannel.getChannelName()+","+mailNumber);
		log.setOperatorId(sysUser.getId());
		log.setOperatorName(sysUser.getName());
		log.setOperatorType(OrderOperateTypeEnum.sys_kf.getCode());
		log.setOrderId(orderInfo.getId());
		orderOperateLogService.insert(log);
		
		
		orderInfoService.updateMailChannelById(orderInfo);
	}

	private String getMailNo(OrderInfo orderInfo){
		String result = null;
		if(orderInfo.getMailChannel()==1){
			result= orderInfo.getMailNo();
		}else{
			result= orderInfo.getMailNumber();
		}
		if(result==null){
			return "";
		}else{
			return result;
		}
	}
	
	/**
	 * 
	 * getFiledDiffEnumMap:(获取字段修改前后枚举描述).
	 *
	 * @author huangyihao
	 * 2017年8月25日 下午6:21:44
	 * @param newDetail
	 * @param oldDetail
	 * @return
	 *
	 */
	private Map<String, BaseDiffEnum> getFiledDiffEnumMap(OrderDetailVo newDetail, OrderDetailVo oldDetail) {
		Map<String, BaseDiffEnum> filedEnumMap = new HashMap<String, BaseDiffEnum>();
		BaseDiffEnum expressDiff = new BaseDiffEnum();
		expressDiff.setBefor(ExpressTypeEnum.getByType(oldDetail.getExpressType()));
		expressDiff.setAfter(ExpressTypeEnum.getByType(newDetail.getExpressType()));
		filedEnumMap.put("expressType", expressDiff);
		
		return filedEnumMap;
	}
	
	
	
	
	@Override
	public void updateWeixinPayCodeById(Integer id, String weixinPayCode, String kfId) {
		orderInfoService.updateWeixinPayCodeById(id, weixinPayCode, kfId);
	}

	public static void main(String[] args) {
		System.out.println(ReflectHelper.getFiledValueMap(new OrderDetailVo()));
	}
}