package org.trc.mapper.outbound;

import java.util.List;

import org.trc.domain.order.OutboundDetail;
import org.trc.util.BaseMapper;

/**
 * Created by hzcyn on 2017/12/1.
 */
public interface IOutboundDetailMapper extends BaseMapper<OutboundDetail> {

	List<OutboundDetail> selectByWarehouseOrderCode(String warehouseOrderCode);
}
