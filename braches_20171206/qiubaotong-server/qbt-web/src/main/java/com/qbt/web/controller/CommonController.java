package com.qbt.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.xml.bind.ValidationException;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.qbt.common.enums.ErrorCodeEnum;
import com.qbt.common.enums.ExpressTypeEnum;
import com.qbt.common.exception.WechatException;
import com.qbt.common.logback.LogCvt;
import com.qbt.common.result.Result;
import com.qbt.common.util.DateUtil;
import com.qbt.common.validate.VlidationUtil;
import com.qbt.web.pojo.base.Area;
import com.qbt.web.pojo.base.SiteModel;
import com.qbt.web.pojo.comm.BaseCourseResp;
import com.qbt.web.pojo.comm.OutRangeVo;
import com.qbt.web.pojo.sf.DeliverRequest;
import com.qbt.web.pojo.sf.DeliverResponse;
import com.qbt.web.pojo.sf.DeliverV2Response;
import com.qbt.web.pojo.urgent.UrgentPageReqVo;
import com.qbt.web.pojo.urgent.UrgentVo;
import com.qbt.web.support.AreaSupport;
import com.qbt.web.support.BaseCourseSupport;
import com.qbt.web.support.CommonSupport;
import com.qbt.web.support.SfExpressSupport;
import com.qbt.web.support.SiteSupport;
import com.qbt.web.support.UrgentSupport;
import com.qbt.wechat.api.WechatApi;

/**
 * 公共组件查询
 * @author share
 *
 */
@Controller
@RequestMapping("/comm")
public class CommonController {
	@Resource
	private WechatApi wechatApi;
	@Resource
	private CommonSupport commonSupport;
	@Resource
	private BaseCourseSupport baseCourseSupport;
	@Resource
	private SiteSupport siteSupport;
	@Resource
	private AreaSupport areaSupport;
	@Resource
	private SfExpressSupport sfExpressSupport;
	@Resource
	private UrgentSupport urgentSupport;
	
	/**
	 * 微信Js Sdk 获取Config
	 * @return
	 * @throws InterruptedException
	 */
	@RequestMapping("/weixin/jsConfig")
	public Result<Map<String,String>> weixinJsSdkConfig(String url) throws Exception{
		Result<Map<String,String>> result = new Result<Map<String,String>>();
		String jsapi_ticket = commonSupport.getJsapiTicket();
		result.setDatas(wechatApi.jsSdkSignConf(jsapi_ticket, url));
		return result;
	}
	
	/**
	 * 根据关键字或城市获取球场列表
	 * @param name() 球场名称
	 * @param area_id 地区ID
	 * @param lon 精度
	 * @param lat 纬度
	 * @return
	 */
	@RequestMapping("/query_course")
	public Result<List<SiteModel>> findBaseCourseList(
			@RequestParam(value="name", required=false,defaultValue="")String name,
			@RequestParam(value="city_name", required=false,defaultValue="") String cityName,
			@RequestParam(value="city_id", required=false,defaultValue="0") int cityId, 
			@RequestParam(value="lon", required=false,defaultValue="0") float lon, 
			@RequestParam(value="lat", required=false,defaultValue="0") float lat,
			@RequestParam(value="status", required=false,defaultValue="0") int status,
			@RequestParam(value="isPractice", required=false,defaultValue="0") int isPractice
			){
		Result<List<SiteModel>> result = new Result<List<SiteModel>>();
		int areaId = cityId;
		if(areaId ==0 && cityName != ""){
			Area area = areaSupport.findByName(cityName);
			areaId = area != null?area.getId():0;
		}
		List<SiteModel> weiXinBaseCourseList = baseCourseSupport.findList(name,areaId,lon,lat,status,isPractice);
		result.setDatas(weiXinBaseCourseList);
		
		return result;
	}
	/**
	 * 获取寄球包站点列表接口（type：1为机场，2为顺丰，3为银行）
	 * @param type
	 * @return
	 */
	@RequestMapping("/query_site")
	public Result<List<SiteModel>> findSiteList(Integer type){
		Result<List<SiteModel>> result = new Result<List<SiteModel>>();
		List<SiteModel> weiXinBaseCourseList = siteSupport.findSiteList(type);
		result.setDatas(weiXinBaseCourseList);
		return result;
	}
	
	/**
	 *  顺丰预计到达时间接口查询
	  * @Title: query_sf_expect_recive_time
	  * @Description: TODO
	  * @author: share 2016年8月10日
	  * @modify: share 2016年8月10日
	  * @param request
	  * @return
	 */
	@RequestMapping("/query_sf_recive_time")
	public Result<List<DeliverResponse>> query_sf_expect_recive_time(@RequestBody DeliverRequest request){
		Result<List<DeliverResponse>> result = new Result<List<DeliverResponse>>();
		 
		try {
			VlidationUtil.validate(request); 
			
			// 预约取包时间校验
			String timeSection = request.getConsigned_time();
			Date consignedTime = DateUtil.str2Date(timeSection.substring(0,16)+":00", DateUtil.DATE_TIME_FORMAT_01);
			
			List<DeliverResponse> datas = sfExpressSupport.query_sf_expect_recive_time(request);
			Collections.sort(datas);
			Iterator<DeliverResponse> it= datas.iterator();
			Set<String> deliverTime = new HashSet<String>();
			while(it.hasNext()){
				DeliverResponse deliver = it.next();
				if(deliverTime.contains(deliver.getDeliver_time()) 
						|| (consignedTime.getTime() < 1486051200000l 
						&& ExpressTypeEnum.after_tomorow.getType().equals(deliver.getBusiness_type()))){
					it.remove();  
				}else{
					deliverTime.add(deliver.getDeliver_time());
				}
			}
			result.setDatas(datas);
		}catch (ValidationException e) {  
            result.setError(ErrorCodeEnum.ERROR_VALID_FAIL);
            result.setMsg(e.getMessage());
        }catch (WechatException e) {
			result.setCode(e.getCode());
			result.setMsg(e.getMsg());
		}catch (Exception e) {
			result.setCode(ErrorCodeEnum.ERROR.getCode());
			result.setMsg(ErrorCodeEnum.ERROR.getMsg());
			LogCvt.error(e.getMessage(),e);
		}
		return result;
	}
	
	/**
	 * 获取最热门的球场
	  * @Title: findByHotCourse
	  * @Description: TODO
	  * @author: chenxiaocong 2017年3月6日
	  * @modify: chenxiaocong 2017年3月6日
	  * @return
	 */
	@RequestMapping("/findByHotCourse")
	public Result<List<BaseCourseResp>> findByHotCourse(){
		Result<List<BaseCourseResp>> result = new Result<List<BaseCourseResp>>();
		try {
			result.setDatas(baseCourseSupport.findByHotCourse());
		}catch (Exception e) {
			result.setCode(ErrorCodeEnum.ERROR.getCode());
			result.setMsg(ErrorCodeEnum.ERROR.getMsg());
			LogCvt.error(e.getMessage(),e);
		}
		return result;
	}
	
	@RequestMapping("/v2/query_sf_recive_time")
	public Result<List<DeliverV2Response>> query_sf_expect_recive_time_v2(@RequestBody DeliverRequest request){
		Result<List<DeliverV2Response>> result = new Result<List<DeliverV2Response>>();
		try {
			VlidationUtil.validate(request); 
			List<DeliverV2Response> datas = sfExpressSupport.query_sf_expect_recive_time_v2(request);
			Collections.sort(datas);
			Iterator<DeliverV2Response> it= datas.iterator();
			Map<String, DeliverV2Response> dataMap = new LinkedHashMap<String, DeliverV2Response>();
			while(it.hasNext()){
				DeliverV2Response deliver = it.next();
				if(ExpressTypeEnum.after_tomorow.getType().equals(deliver.getBusiness_type())){
					long days = DateUtil.getDiffDaysForDay(
							DateUtil.str2Date(request.getConsigned_time(), DateUtil.DATE_TIME_FORMAT_01), 
							DateUtil.str2Date(deliver.getDeliver_time().split(",")[0], DateUtil.DATE_TIME_FORMAT_01)
							);
					deliver.setDescribe("预计"+days+"天后送达");
				}
				//如果同时有次晨和次日，只显示次日件
				if(ExpressTypeEnum.tormorow_noon.getType().equals(deliver.getBusiness_type())){
					it.remove();
					continue;
				}
				dataMap.put(deliver.getBusiness_type(), deliver);
			}
			//空运(次日)和陆运(隔日)，时间一样时，只显示空运(次日)
			if(dataMap.containsKey(ExpressTypeEnum.tormorow.getType()) && dataMap.containsKey(ExpressTypeEnum.after_tomorow.getType())) {
				DeliverV2Response ciri = dataMap.get(ExpressTypeEnum.tormorow.getType());
				DeliverV2Response geri = dataMap.get(ExpressTypeEnum.after_tomorow.getType());
				
				String[] ciriDeliverTimes = ciri.getDeliver_time().split(",");
				String[] geriDeliverTimes = geri.getDeliver_time().split(",");
				Date ciriDeliverTime_0 = DateUtil.str2Date(ciriDeliverTimes[0], DateUtil.DATE_TIME_FORMAT_01);
				Date ciriDeliverTime_1 = DateUtil.str2Date(ciriDeliverTimes[1], DateUtil.DATE_TIME_FORMAT_01);
				Date geriDeliverTime_0 = DateUtil.str2Date(geriDeliverTimes[0], DateUtil.DATE_TIME_FORMAT_01);
				Date geriDeliverTime_1 = DateUtil.str2Date(geriDeliverTimes[1], DateUtil.DATE_TIME_FORMAT_01);
				if(ciriDeliverTime_0.compareTo(geriDeliverTime_0) == 0 
						&& ciriDeliverTime_1.compareTo(geriDeliverTime_1) == 0) {
					dataMap.remove(ExpressTypeEnum.after_tomorow.getType());
				}
			}
			List<DeliverV2Response> times = new ArrayList<DeliverV2Response>();			
			for(String s : dataMap.keySet()) {
				times.add(dataMap.get(s));
			}
			
			// 加急的数据
			//15点以后不返回boss端的加急数据
			if(!isAfter15NoUrgentDisplay(request.getConsigned_time())) {
				addUrgentDateTime(request, times);
			}
			
			result.setDatas(times);
		}catch (ValidationException e) {  
            result.setError(ErrorCodeEnum.ERROR_VALID_FAIL);
            result.setMsg(e.getMessage());
        }catch (WechatException e) {
			result.setCode(e.getCode());
			result.setMsg(e.getMsg());
		}catch (Exception e) {
			result.setCode(ErrorCodeEnum.ERROR.getCode());
			result.setMsg(ErrorCodeEnum.ERROR.getMsg());
			LogCvt.error(e.getMessage(),e);
		}
		return result;
	}

	/**
	 * 根据加急逻辑增加加急时间
	 * @param request
	 * @param times
	 */
	private void addUrgentDateTime(DeliverRequest request, List<DeliverV2Response> times) {
		Area fromArea = areaSupport.findById(Integer.valueOf(request.getSrc_area_id()));
		Area toArea = areaSupport.findById(Integer.valueOf(request.getDest_area_id()));
		
		UrgentPageReqVo reqCityVo = new UrgentPageReqVo();
		reqCityVo.setFromCity(fromArea.getSuperId()+"");
		reqCityVo.setToCity(toArea.getSuperId()+"");		
		
		List<UrgentVo> urgentVos = urgentSupport.findByPage(reqCityVo);
		if(urgentVos != null && urgentVos.size()>0) {
			UrgentVo urgentVo = urgentVos.get(0);
			
			// 获取快件的价格
			DeliverV2Response deliverTime = null;
			if (times != null) {
				for(DeliverV2Response response: times) {
					if(response.getBusiness_type().equals("1")) {
						deliverTime = response;
					}
				}
			}
			
			if(deliverTime != null) {
				DeliverV2Response urgentTime = new DeliverV2Response();
				urgentTime.setBusiness_type("3");
				urgentTime.setBusiness_type_desc("顺丰重货");
				urgentTime.setOrigin_price(deliverTime.getOrigin_price() + urgentVo.getPrice());
				urgentTime.setOut_range_price(deliverTime.getOut_range_price());
				urgentTime.setSf_price(deliverTime.getSf_price() + urgentVo.getPrice());
				
				String consigneTime = request.getConsigned_time();
				consigneTime = consigneTime.length() > 20 ? consigneTime.substring(0,16)+":00" : consigneTime;
				Date consignedDate = DateUtil.str2Date(consigneTime, DateUtil.DATE_TIME_FORMAT_01);
				
				// 判断取件时间是否是17点以后
				if(DateUtil.isNight(consignedDate)) {
					// 调整为第二天的9点
					consignedDate = DateUtil.ajustNight(consignedDate);
				}

				// 快件送达时间是取件时间的24小时以后
				consignedDate = DateUtils.addDays(consignedDate, 1);
				String consignedDateStr = DateUtil.formatDateTime(consignedDate);
				String urgentDeliverTime = consignedDateStr +","+ consignedDateStr;
				urgentTime.setDeliver_time(urgentDeliverTime);
				
				// 如果快件的到达时间早于加急的到达时间，就不需要加急的数据了
				if(deliverTime.getDeliver_time().indexOf(",")>0) {
					Date deliverDate = DateUtil.str2Date(deliverTime.getDeliver_time().split(",")[1],
							DateUtil.DATE_TIME_FORMAT_01);
					
					if(!deliverDate.before(consignedDate)) {
						times.add(0, urgentTime);	
					}
				}else {
					times.add(0, urgentTime);
				}
			}
			
		}
	}
	
	@RequestMapping("/v2/query_sf_recive_time_boss")
	public Result<List<DeliverV2Response>> query_sf_expect_recive_time_v2_boss(@RequestBody DeliverRequest request){
		Result<List<DeliverV2Response>> result = new Result<List<DeliverV2Response>>();
		try {
			VlidationUtil.validate(request); 
			List<DeliverV2Response> datas = sfExpressSupport.query_sf_expect_recive_time_v2(request);
			Collections.sort(datas);
			Iterator<DeliverV2Response> it= datas.iterator();
			while(it.hasNext()){
				DeliverV2Response deliver = it.next();
				if(ExpressTypeEnum.after_tomorow.getType().equals(deliver.getBusiness_type())){
					long days = DateUtil.getDiffDaysForDay(
							DateUtil.str2Date(request.getConsigned_time(), DateUtil.DATE_TIME_FORMAT_01), 
							DateUtil.str2Date(deliver.getDeliver_time().split(",")[0], DateUtil.DATE_TIME_FORMAT_01)
							);
					deliver.setDescribe("预计"+days+"天后送达");
				}
				//如果同时有次晨和次日，只显示次日件
				if(ExpressTypeEnum.tormorow_noon.getType().equals(deliver.getBusiness_type())){
					it.remove();
					continue;
				}
				
			}
			
			// 加急的数据
			//15点以后不返回boss端的加急数据
			if(!isAfter15NoUrgentDisplay(request.getConsigned_time())) {
				addUrgentDateTime(request, datas);
			}
			
			result.setDatas(datas);
		}catch (ValidationException e) {  
            result.setError(ErrorCodeEnum.ERROR_VALID_FAIL);
            result.setMsg(e.getMessage());
        }catch (WechatException e) {
			result.setCode(e.getCode());
			result.setMsg(e.getMsg());
		}catch (Exception e) {
			result.setCode(ErrorCodeEnum.ERROR.getCode());
			result.setMsg(ErrorCodeEnum.ERROR.getMsg());
			LogCvt.error(e.getMessage(),e);
		}
		return result;
	}
	
	/**
	 * 寄件时间,格式为 YYYY-MM-DD HH24:MM:SS,示例 2013-12-27 17:54:20
	 * 15点以后，不显示加急的数据
	 */
	private boolean isAfter15NoUrgentDisplay(String consignedTime) {
		
		if(consignedTime == null) {
			return false;
		}
		
		String hours = consignedTime.substring(11,13);
		
		if(Integer.parseInt(hours)>15) {
			return true;
		}
		
		return false;
	}
	
	@RequestMapping(value="/isOutRangeArea",method = RequestMethod.POST)
	public Result<Boolean> isOutRangeArea(@RequestBody OutRangeVo vo){
		Result<Boolean> result=  new Result<Boolean>();
		try{
			result.setDatas(areaSupport.isOutRangeArea(vo.getAreaId(), vo.getAddress()));
		}catch(Exception e){
			e.printStackTrace();
			LogCvt.error(e.getMessage(),e);
		}
		return result;
	}
	
}