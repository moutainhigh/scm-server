package org.trc.service.impl.allocateOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.trc.domain.allocateOrder.AllocateOutOrder;
import org.trc.mapper.allocateOrder.AllocateOutOrderMapper;
import org.trc.service.allocateOrder.IAllocateOutOrderService;
import org.trc.service.impl.BaseService;

@Service("allocateOutOrderSerivce")
public class AllocateOutOrderSerivce extends BaseService<AllocateOutOrder, Long> implements IAllocateOutOrderService{
	
	@Autowired
	private AllocateOutOrderMapper mapper;
	
	@Override
	public void updateOutOrderStatusById(String status, Long id) {
		AllocateOutOrder record = new AllocateOutOrder();
		record.setId(id);
		record.setStatus(status);
		mapper.updateByPrimaryKeySelective(record);
	}

}
