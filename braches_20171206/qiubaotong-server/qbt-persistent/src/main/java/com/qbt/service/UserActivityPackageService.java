package com.qbt.service;

import java.util.List;

import com.qbt.common.entity.PageEntity;
import com.qbt.persistent.entity.UserActivityPackage;

public interface UserActivityPackageService {
	
	/**
	 * 分页查询
	 * @param pageEntity
	 * @return
	 */
	List<UserActivityPackage> findByPage(PageEntity<UserActivityPackage> pageEntity);
	
	/**
	 * 根据id查询
	 * @param id
	 * @return
	 */
	UserActivityPackage findById(Integer id);
	
	/**
	 * 新增活动
	 * @param activity
	 * @return
	 */
	int insert(UserActivityPackage ticketPackage);
	
	/**
	 * 更新活动
	 * @param activity
	 * @return
	 */
	int update(UserActivityPackage ticketPackage);
	
	/**
	 * 获取关联的orderDetailId
	 * @param packageId
	 * @return
	 */
	Object findOrderDetailIdByPackageId(int packageId);
	
	List<UserActivityPackage> findlistUsed(int userId);
	    
	List<UserActivityPackage> findlistNotActive(int userId);
	
	List<UserActivityPackage> findPaperPackageList();
}
