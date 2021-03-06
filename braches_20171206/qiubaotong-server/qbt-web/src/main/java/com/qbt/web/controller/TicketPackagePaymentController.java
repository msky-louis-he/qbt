package com.qbt.web.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.ValidationException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.qbt.common.enums.ErrorCodeEnum;
import com.qbt.common.exception.WechatException;
import com.qbt.common.logback.LogCvt;
import com.qbt.common.result.Result;
import com.qbt.common.validate.VlidationUtil;
import com.qbt.persistent.entity.UserWeixin;
import com.qbt.web.pojo.ticketPackagePayment.TicketPackagePaymentRequest;
import com.qbt.web.support.LoginSupport;
import com.qbt.web.support.TicketPackagePyamentSupport;

/**
 * 
 * @author feng.xuan
 *
 */
@Controller
@RequestMapping("/ticketPackagePay")
public class TicketPackagePaymentController {

	@Resource
	private TicketPackagePyamentSupport ticketPackagePyamentSupport;

	@Resource
	private LoginSupport loginSupport;

	@RequestMapping("/pay")
	public Result<Map<String, String>> ticketPackagePay(HttpServletRequest request,
			@RequestBody TicketPackagePaymentRequest packageRequest) {
		Result<Map<String, String>> result = new Result<Map<String, String>>();
		/**
		 * 订单信息基本校验
		 */
		try {
			VlidationUtil.validate(packageRequest);
		} catch (ValidationException e) {
			result.setError(ErrorCodeEnum.ERROR_VALID_FAIL);
			result.setMsg(e.getMessage());
			return result;
		}
		try {
			String openid = String.valueOf(request.getAttribute("openid"));
			UserWeixin userWeixin = loginSupport.findByOpenId(openid);
			packageRequest.setUserId(userWeixin.getId());
			packageRequest.setOpenid(openid);
			result.setDatas(ticketPackagePyamentSupport.pay(packageRequest));
		} catch (WechatException e) {
			result.setCode(e.getCode());
			result.setMsg(e.getMsg());
		} catch (Exception e) {
			result.setCode(ErrorCodeEnum.ERROR.getCode());
			result.setMsg(ErrorCodeEnum.ERROR.getMsg());
			LogCvt.error(e.getMessage(), e);
		}
		return result;
	}
}
