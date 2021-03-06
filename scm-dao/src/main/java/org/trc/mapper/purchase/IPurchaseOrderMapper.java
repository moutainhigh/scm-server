package org.trc.mapper.purchase;

import org.apache.ibatis.annotations.Param;
import org.trc.domain.purchase.PurchaseDetail;
import org.trc.domain.purchase.PurchaseOrder;
import org.trc.domain.supplier.Supplier;
import org.trc.util.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * 采购订单
 * Created by sone on 2017/5/25.
 */
public interface IPurchaseOrderMapper extends BaseMapper<PurchaseOrder>{

    List<Supplier> findSuppliersByChannelCode(@Param("channelCode") String channelCode, @Param("supplierName") String supplierName);

    List<PurchaseDetail> selectItemsBySupplierCode(Map<String, Object> map);

    int selectCountItems(Map<String, Object> map);

    /**
     * 拼接分类的全路径名和ids的拼接
     * @param categoryIds
     * @return
     */
    List<PurchaseDetail> selectAllCategory(List<Long> categoryIds);

    int selectCountItemsForSupplier(Map<String, Object> map);

    int selectItemsBySupplierCodeCount(Map<String, Object> map);

    List<PurchaseDetail> selectItemsBySupplierCodeCheck(Map<String, Object> map);
}
