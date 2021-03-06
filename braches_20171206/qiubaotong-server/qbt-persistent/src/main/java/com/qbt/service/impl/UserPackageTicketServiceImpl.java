package com.qbt.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qbt.common.entity.PageEntity;
import com.qbt.persistent.entity.ActivityTicketPackage;
import com.qbt.persistent.entity.UserPackageTicket;
import com.qbt.persistent.mapper.UserPackageTicketMapper;
import com.qbt.service.UserPackageTicketService;

@Service
public class UserPackageTicketServiceImpl implements UserPackageTicketService {
	
	@Resource
	private UserPackageTicketMapper userPackageTicketMapper;
	
	@Override
	public List<UserPackageTicket> findByPage(PageEntity<UserPackageTicket> pageEntity) {
		return userPackageTicketMapper.findByPage(pageEntity);
	}

	@Override
	public UserPackageTicket findById(Integer id) {
		return userPackageTicketMapper.selectById(id);
	}

	@Override
	public int insert(UserPackageTicket ticket) {
		userPackageTicketMapper.insert(ticket);
		return ticket.getId();
	}

	@Override
	public int update(UserPackageTicket ticket) {
		if(ticket.getId() > 0){
			int count = userPackageTicketMapper.updateById(ticket);			
			return count;
		}
		return 0;
	}

	@Override
	public List<UserPackageTicket> findTicketsForOrderDetail(Integer orderDetailId){
		return userPackageTicketMapper.findTicketsForOrderDetail(orderDetailId);
	}
	
	@Override
	public List<UserPackageTicket> findAvailableTicketByUserId(
			int userId, String sysdate) {
		
		return userPackageTicketMapper.findAvailableTicketByUserId(userId, sysdate);
	}

	@Override
	public List<UserPackageTicket> findDisableTicketByUserId(int userId,
			String sysdatetime) {
		
		return userPackageTicketMapper.findDisableTicketByUserId(userId, sysdatetime);
	}


	@Override
	public ActivityTicketPackage findByPackageId(int packageId) {
		return userPackageTicketMapper.findByPackageId(packageId);
	}

	@Override
	public List<UserPackageTicket> findTicketsByPackageId(int packageId) {
		return userPackageTicketMapper.findTicketsByPackageId(packageId);
	}
	
	
	@Override
	public int checkPackageIsUsed(int packageId) {
		return userPackageTicketMapper.checkPackageIsUsed(packageId);
	}
}
