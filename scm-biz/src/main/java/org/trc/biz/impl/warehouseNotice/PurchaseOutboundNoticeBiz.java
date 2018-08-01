package org.trc.biz.impl.warehouseNotice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.trc.biz.warehouseNotice.IPurchaseOutboundNoticeBiz;
import org.trc.domain.allocateOrder.AllocateSkuDetail;
import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.domain.purchase.PurchaseOutboundDetail;
import org.trc.domain.warehouseInfo.WarehouseItemInfo;
import org.trc.domain.warehouseNotice.PurchaseOutboundNotice;
import org.trc.enums.LogOperationEnum;
import org.trc.enums.WarehouseTypeEnum;
import org.trc.enums.warehouse.PurchaseOutboundNoticeStatusEnum;
import org.trc.form.JDWmsConstantConfig;
import org.trc.form.warehouse.PurchaseOutboundNoticeForm;
import org.trc.form.warehouse.ScmEntryOrderItem;
import org.trc.form.warehouse.allocateOrder.ScmAllocateOrderInResponse;
import org.trc.form.warehouse.entryReturnOrder.ScmEntryReturnItem;
import org.trc.form.warehouse.entryReturnOrder.ScmEntryReturnOrderCreateRequest;
import org.trc.form.warehouse.entryReturnOrder.ScmEntryReturnOrderCreateResponse;
import org.trc.service.config.ILogInfoService;
import org.trc.service.jingdong.ICommonService;
import org.trc.service.warehouse.IWarehouseApiService;
import org.trc.service.warehouseInfo.IWarehouseItemInfoService;
import org.trc.service.warehouseNotice.IPurchaseOutboundNoticeService;
import org.trc.util.AppResult;
import org.trc.util.AssertUtil;
import org.trc.util.Pagenation;

/**
 * Description〈〉
 *
 * @author hzliuwei
 * @create 2018/7/24
 */
@Service("purchaseOutboundNoticeBiz")
public class PurchaseOutboundNoticeBiz implements IPurchaseOutboundNoticeBiz {
	
	@Autowired
	private IPurchaseOutboundNoticeService noticeService;
    @Autowired
    private IWarehouseApiService warehouseApiService;
    @Autowired
    private ICommonService commonService;
    @Autowired
    private JDWmsConstantConfig jDWmsConstantConfig;
    @Autowired
    private IWarehouseItemInfoService warehouseItemInfoService;
    @Autowired
    private ILogInfoService logInfoService;

	@Override
	public Pagenation<PurchaseOutboundNotice> getPageList(PurchaseOutboundNoticeForm form,
			Pagenation<PurchaseOutboundNotice> page, String channelCode) {
		AssertUtil.notNull(channelCode, "业务线编号不能为空");
		return noticeService.pageList(form, page, channelCode);
	}

	@Override
	public PurchaseOutboundNotice getDetail(Long id) {
		PurchaseOutboundNotice notice = noticeService.selectByPrimaryKey(id);
		AssertUtil.notNull(notice, "未找到相应的退货单号");
		
		List<PurchaseOutboundDetail> skuList = noticeService.selectDetailByNoticeCode(notice.getOutboundNoticeCode());
		notice.setSkuList(skuList);
		return notice;
	}


	@Override
	public void noticeOut(String code, AclUserAccreditInfo userInfo) {
		
		AssertUtil.notNull(code, "退货出库通知单编号不能为空!");
		
		List<PurchaseOutboundNotice> noticeList = noticeService.selectNoticeBycode(code);
		if (CollectionUtils.isEmpty(noticeList)) {
			throw new IllegalArgumentException("退货出库通知单编号有误!");
		} else if (noticeList.size() > 1) {
			throw new IllegalArgumentException("退货出库通知单编号重复!");
		}
		
		PurchaseOutboundNotice notice = noticeList.get(0);
		
		/**
		 * 待通知出库, 出库仓接收失败, 已取消 
		 * 上面几种状态才允许退货出库
		 */
		if (!PurchaseOutboundNoticeStatusEnum.TO_BE_NOTIFIED.getCode().equals(notice.getStatus())
				&& !PurchaseOutboundNoticeStatusEnum.WAREHOUSE_RECEIVE_FAILED.getCode().equals(notice.getStatus())
				&& !PurchaseOutboundNoticeStatusEnum.CANCEL.getCode().equals(notice.getStatus())) {
			throw new IllegalArgumentException("当前退货出库通知单状态不允许通知出库!");
		}
		
		ScmEntryReturnOrderCreateRequest request = new ScmEntryReturnOrderCreateRequest();
		commonService.getWarehoueType(notice.getWarehouseCode(), request); // 获取仓库类型，并设置到request中
		
		/**
		 * 京东仓库处理逻辑
		 */
		if (WarehouseTypeEnum.Jingdong.getCode().equals(request.getWarehouseType())) {
			BeanUtils.copyProperties(notice, request);
			request.setDeptNo(jDWmsConstantConfig.getDeptNo()); // 事业部编号
			
			/**
			 * 商品详情
			 */
			List<PurchaseOutboundDetail> skuList = noticeService.selectDetailByNoticeCode(notice.getOutboundNoticeCode());
			if (CollectionUtils.isEmpty(skuList)) {
				throw new IllegalArgumentException("退货出库通知单的商品列表为空!");
			}
			ScmEntryReturnItem item = null;
			
			List<String> skuCodeList = skuList.stream().map(
					PurchaseOutboundDetail :: getSkuCode).collect(Collectors.toList());
			
			List<WarehouseItemInfo> whiList = warehouseItemInfoService.
					selectInfoListBySkuCodeAndWarehouseCode(skuCodeList, notice.getWarehouseCode());
			List<ScmEntryReturnItem> list = new ArrayList<>();
	        for (PurchaseOutboundDetail sku : skuList) {
	        	for (WarehouseItemInfo info : whiList) {
					if (StringUtils.equals(info.getSkuCode(), sku.getSkuCode())) {
						item = new ScmEntryReturnItem();
						item.setItemId(info.getWarehouseItemId());
						item.setReturnQuantity(sku.getOutboundQuantity());
						list.add(item);
						break;
					}
				}
	        }
	        request.setEntryOrderItemList(list);
			
		} else {
			throw new IllegalArgumentException("暂时不支持自营仓库的退货出库操作!");
		}
		
		AppResult<ScmEntryReturnOrderCreateResponse> response = warehouseApiService.entryReturnOrderCreate(request);
		
        //记录操作日志 (动作：通知出库;操作人：用户姓名)
        logInfoService.recordLog(notice, notice.getId().toString(),
        		userInfo.getUserId(), LogOperationEnum.ENTRY_RETURN_NOTICE.getMessage(), "",null);
		
		
	}

        
}
