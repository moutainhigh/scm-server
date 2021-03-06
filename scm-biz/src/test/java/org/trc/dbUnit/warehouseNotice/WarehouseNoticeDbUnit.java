package org.trc.dbUnit.warehouseNotice;

import com.alibaba.fastjson.JSON;
import com.qimen.api.request.EntryorderCreateRequest;
import org.dbunit.dataset.ReplacementDataSet;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.trc.biz.warehouseNotice.IWarehouseNoticeBiz;
import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.domain.warehouseNotice.WarehouseNotice;
import org.trc.service.BaseTest;
import org.trc.service.IQimenService;
import org.trc.service.impl.purchase.WarehouseNoticeService;
import org.trc.util.AppResult;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WarehouseNoticeDbUnit extends BaseTest {
	
	@Autowired
	private IWarehouseNoticeBiz warehouseNoticeBiz;
	@Autowired
	private WarehouseNoticeService warehouseNoticeService;
	
    // private static final String WAREHOUSE_NOTICE = "warehouse_notice";
	static ClassLoader loader = Thread.currentThread().getContextClassLoader();
	
	/**
	 * 数据准备
	 * @param arr  测试数据源文件名数组
	 * @throws Exception
	 */
	private void preTest (String... arr) throws Exception {

		execSql(conn,"delete from purchase_order"); // 采购单
		execSql(conn,"delete from warehouse_notice"); // 入库通知单
		execSql(conn,"delete from warehouse_notice_details"); // 入库明细表
		execSql(conn,"delete from sku_stock"); // 库存表
		
		if (null != arr && arr.length > 0) {
			for (int i = 0; i < 4; i++) {
				prepareData(conn, "warehouseNotice/pre/" + arr[i] + ".xml");
			}
		}
		
	}
	
	private void resultCompare (String caseString) throws Exception {
		/**
		 * 采购单状态修改为 已通知
		 **/
        ReplacementDataSet expResult = createDataSet(loader.getResourceAsStream("warehouseNotice/exp/" + 
        		caseString + "/expPurchaseOrder.xml"));
        expResult.addReplacementObject("null", null);
        assertDataSet("purchase_order","select * from purchase_order where id = 365",expResult,conn);
        
        /**
         * 更新入库通知单为 (成功：待仓库反馈状态 ；失败：仓库接收失败)
         **/
        ReplacementDataSet expResult1 = createDataSet(loader.getResourceAsStream("warehouseNotice/exp/" + 
        		caseString + "/expWarehouseNotice.xml"));
        expResult1.addReplacementObject("null", null);
        assertDataSet("warehouse_notice","select * from warehouse_notice where id = 35",expResult1,conn);
        
		/**
		 * 更新入库明细表中的商品为 (成功：待仓库反馈状态 ；失败：仓库接收失败)
		 **/
        ReplacementDataSet expResult3 = createDataSet(loader.getResourceAsStream("warehouseNotice/exp/" + 
        		caseString + "/expWarehouseNoticeDetails.xml"));
        expResult3.addReplacementObject("null", null);
        assertDataSet("warehouse_notice_details",
        		"select * from warehouse_notice_details where warehouse_notice_code = 'CGRKTZ2017120500166'",expResult3,conn);
       
        /**
         * 成功则更新相应sku的在途库存数
         **/
        ReplacementDataSet expResult4 = createDataSet(loader.getResourceAsStream("warehouseNotice/exp/" +
        		caseString + "/expSkuStock.xml"));
        expResult4.addReplacementObject("null", null);
        assertDataSet("sku_stock","select * from sku_stock where id = 8",expResult4,conn);
	}
	/**
	 * case 1
	 * 入库单通知收货-仓库接收成功
	 * @throws Exception 
	 */
	@Test
	public void receiptAdvice_success () throws Exception {
		
		preTest("case1/preWarehouseNotice","case1/prePurchaseOrder","case1/preSkuStock",
				"case1/preWarehouseNoticeDetails");
		
		/**  入库通知成功  **/
		mockQimenEntryOrderCreate(true);
		warehouseNoticeBiz.receiptAdvice(createWarehouseNotice(), createAclUserAccreditInfo());
		
		resultCompare("case1");
		
	}
	
	/**
	 * case 2
	 * 入库单通知收货-入库通知单为空
	 * @throws Exception 
	 */
	@Test
	public void receiptAdvice_warehouseNoticeNull () throws Exception {
		mockQimenEntryOrderCreate(true);
		warehouseNoticeBiz.receiptAdvice(null, createAclUserAccreditInfo());
	}
	
	/**
	 * case 3
	 * 入库单通知收货-查询采购单失败(采购单状态不合法)
	 * @throws Exception 
	 */
	@Test
	public void receiptAdvice_purchaseOrderStatusErr () throws Exception {
		
		preTest("case3/preWarehouseNotice","case3/prePurchaseOrder","case3/preSkuStock",
				"case3/preWarehouseNoticeDetails");
		
		mockQimenEntryOrderCreate(true);
		warehouseNoticeBiz.receiptAdvice(createWarehouseNotice(), createAclUserAccreditInfo());
	}
	
	/**
	 * case 4
	 * 入库单通知收货-入库单通知状态不符合修改条件,无法进行入库通知的操作
	 * @throws Exception 
	 */
	@Test
	public void receiptAdvice_warehouseNoticeOrderStatusErr () throws Exception {
		
		preTest("case4/preWarehouseNotice","case4/prePurchaseOrder","case4/preSkuStock",
				"case4/preWarehouseNoticeDetails");
		
		mockQimenEntryOrderCreate(true);
		warehouseNoticeBiz.receiptAdvice(createWarehouseNotice(), createAclUserAccreditInfo());
	}
	
	
	/**
	 * case 5
	 * 入库单通知收货-仓库接收失败
	 * @throws Exception 
	 */
	@Test
	public void receiptAdvice_fail () throws Exception {
		
		preTest("case1/preWarehouseNotice","case1/prePurchaseOrder","case1/preSkuStock",
				"case1/preWarehouseNoticeDetails");
		
		/**  入库通知失败  **/
		mockQimenEntryOrderCreate(false);
		warehouseNoticeBiz.receiptAdvice(createWarehouseNotice(), createAclUserAccreditInfo());
		
		resultCompare("case5");
		
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void mockQimenEntryOrderCreate(Boolean isSucc) {
		
		IQimenService qimenService = mock(IQimenService.class);
		warehouseNoticeBiz.setQimenService(qimenService);
		AppResult ret = new AppResult();
		if (isSucc) {
			ret.setAppcode("200");
			ret.setDatabuffer("入库单创建成功");
		} else {
			ret.setAppcode("0");
			ret.setDatabuffer("mock测试，入库单创建失败");
		}
		String body = "{\"flag\":\"success\",\"code\":\"200\",\"success\":true,\"entryOrderId\":\"WMS-CGRKTZ2017120500166\",\"message\":\"入库单创建成功\",\"body\":\"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\"?><response>   <flag>success</flag>    <code>200</code>    <message>入库货单创建成功</message>    <entryOrderId>dbtest001</entryOrderId> </response>\"}";
		ret.setResult(JSON.parseObject(body));
		when(qimenService.entryOrderCreate(any(EntryorderCreateRequest.class))).thenReturn(ret);	
	}

	private WarehouseNotice createWarehouseNotice () {
		WarehouseNotice notice = new WarehouseNotice();
		notice.setId(35L);
		WarehouseNotice one = warehouseNoticeService.selectOne(notice);
		return one;
	}
	
    private AclUserAccreditInfo createAclUserAccreditInfo () {
        AclUserAccreditInfo info = new AclUserAccreditInfo();
        info.setId(1L);
        info.setChannelId(2L);
        info.setChannelName("小泰乐活");
        info.setUserId("E2E4BDAD80354EFAB6E70120C271968C");
        info.setPhone("15757195796");
        info.setName("admin");
        info.setUserType("mixtureUser");
        info.setChannelCode("QD002");
        info.setRemark("admin");
        info.setIsValid("1");
        info.setIsDeleted("0");
        info.setCreateOperator("E2E4BDAD80354EFAB6E70120C271968C");
        return info;
    }
    
    /**
     * 从数据库中导出指定表数据到xml文件中
     * @throws Exception
     */
    @Test
    public void exportData() throws Exception {
        List<String> tableNameList = new ArrayList<>();
        tableNameList.add("purchase_order");
        exportData(tableNameList, "src/test/resources/warehouseNotice/expPurchaseOrder.xml");
    }

}
