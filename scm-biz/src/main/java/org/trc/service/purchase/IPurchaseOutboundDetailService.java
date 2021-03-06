package org.trc.service.purchase;

import java.util.Date;
import java.util.List;

import org.trc.domain.purchase.PurchaseOutboundDetail;
import org.trc.enums.warehouse.PurchaseOutboundNoticeStatusEnum;
import org.trc.service.IBaseService;

public interface IPurchaseOutboundDetailService extends IBaseService<PurchaseOutboundDetail,Long> {

	List<PurchaseOutboundDetail> selectDetailByNoticeCode(String outboundNoticeCode);

	void updateByOrderCode(PurchaseOutboundNoticeStatusEnum status, String outboundNoticeCode);

	void updateByDetailId(PurchaseOutboundNoticeStatusEnum detailStatus, Date nowTime, Long actualQty, Long id);
}
