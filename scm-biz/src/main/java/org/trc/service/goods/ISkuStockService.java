package org.trc.service.goods;

import java.util.List;

import org.trc.domain.goods.SkuStock;
import org.trc.domain.warehouseNotice.WarehouseNoticeDetails;
import org.trc.form.goods.RequsetUpdateStock;
import org.trc.service.IBaseService;

/**
 * Created by hzwdx on 2017/5/24.
 */
public interface ISkuStockService extends IBaseService<SkuStock, Long>{

	/**
	 * 批量更新在途库存
	 * @param channelCode
	 * @param warehouseCode
	 * @param detailsList
	 */
	void batchUpdateStockAirInventory(String channelCode, String warehouseCode,
			List<WarehouseNoticeDetails> detailsList);
	
	
	/**
	 * 库存更新接口
	 * @param stockList {@see RequsetUpdateStock}
	 * @return
	 */
	void updateSkuStock (List<RequsetUpdateStock> stockList);
}
