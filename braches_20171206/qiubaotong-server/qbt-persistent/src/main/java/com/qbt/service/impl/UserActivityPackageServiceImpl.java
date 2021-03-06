package com.qbt.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qbt.common.entity.PageEntity;
import com.qbt.persistent.entity.UserActivityPackage;
import com.qbt.persistent.mapper.UserActivityPackageMapper;
import com.qbt.service.UserActivityPackageService;

@Service
public class UserActivityPackageServiceImpl implements UserActivityPackageService {
	
	@Resource
	private UserActivityPackageMapper userActivityPackageMapper;
	
	@Override
	public List<UserActivityPackage> findByPage(PageEntity<UserActivityPackage> pageEntity) {
		return userActivityPackageMapper.findByPage(pageEntity);
	}

	@Override
	public UserActivityPackage findById(Integer id) {
		return userActivityPackageMapper.selectById(id);
	}

	@Override
	public int insert(UserActivityPackage ticket) {
		userActivityPackageMapper.insert(ticket);
		return ticket.getId();
	}

	@Override
	public int update(UserActivityPackage ticket) {
		if(ticket.getId() > 0){
			int count = userActivityPackageMapper.updateById(ticket);			
			return count;
		}
		return 0;
	}

	@Override
	public Object findOrderDetailIdByPackageId(int packageId) {
		return userActivityPackageMapper.findOrderDetailIdByPackageId(packageId);
	}
	
	@Override
	public List<UserActivityPackage> findlistUsed(int userId) {
		return userActivityPackageMapper.findlistUsed(userId);
	}

	@Override
	public List<UserActivityPackage> findlistNotActive(int userId) {
		return userActivityPackageMapper.findlistNotActive(userId);
	}
	
	@Override
	public List<UserActivityPackage> findPaperPackageList() {
		return userActivityPackageMapper.findPaperPackageList();
	}

}
