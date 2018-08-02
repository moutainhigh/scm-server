package org.trc.dbUnit.purchase;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.trc.biz.purchase.IPurchaseOutboundOrderBiz;
import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.domain.purchase.PurchaseOutboundDetail;
import org.trc.domain.purchase.PurchaseOutboundOrder;
import org.trc.form.purchase.PurchaseOutboundItemForm;
import org.trc.form.purchase.PurchaseOutboundOrderForm;
import org.trc.util.Pagenation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Description〈〉
 *
 * @author hzliuwei
 * @create 2018/7/30
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:config/resource-context.xml"})
public class PurchaseOutboundOrderTest {

    @Autowired
    private IPurchaseOutboundOrderBiz purchaseOutboundOrderBiz;

    /**
     * 采购退货单列表
     */

    @Test
    public void getPurchaseOutboundOrderListTest(){
        Pagenation<PurchaseOutboundOrder> purchaseOutboundOrderPagenation = purchaseOutboundOrderBiz.purchaseOutboundOrderPageList(new PurchaseOutboundOrderForm(), new Pagenation<>(), "YWX001");
        System.out.println(JSON.toJSONString(purchaseOutboundOrderPagenation));
    }

    /**
     * 暂存采购退货单
     */
    @Test
    public void savePurchaseOutboundOrderTest(){
        PurchaseOutboundOrder order = new PurchaseOutboundOrder();
        order.setSupplierCode("GYS000166");
        order.setWarehouseInfoId(58L);
        //退货类型1-正品，2-残品
        order.setReturnOrderType("2");
        //提货方式1-到仓自提，2-京东配送，3-其他物流
        order.setPickType("3");

        order.setReceiver("zhang");
        order.setReceiverNumber("17826821234");
        order.setReceiverProvince("330000");
        order.setReceiverCity("330100");
        order.setReceiverArea("330108");
        order.setReceiverAddress("奥斯卡刘德华");
        order.setReturnPolicy("存在残次品");

        List<PurchaseOutboundDetail> list = new ArrayList<>();

        PurchaseOutboundDetail detail = new PurchaseOutboundDetail();
        detail.setCanBackQuantity(100L);
        detail.setBrandName("品牌test");
        detail.setReturnOrderType("2");
        detail.setPrice(new BigDecimal(100));
        detail.setBarCode("99,88,77,66");
        detail.setBrandId("1893");
        detail.setCategoryId("165");
        detail.setItemNo("huohao");
        detail.setSkuCode("SP0201805190000768");
        detail.setSkuName("采购1");
        detail.setSpecNatureInfo("属性:采购1");
        detail.setTaxRate(new BigDecimal(16));
        detail.setOutboundQuantity(5L);
        list.add(detail);

        order.setPurchaseOutboundDetailList(list);


        AclUserAccreditInfo info = new AclUserAccreditInfo();
        info.setChannelCode("YWX001");
        info.setUserId("B571346F625E44DB8FCBA8116E72593D");
        //code [0]暂存，[1]提交审核
        purchaseOutboundOrderBiz.savePurchaseOutboundOrder(order,"1",info);
    }

    /**
     * 更新采购退货单
     */
    @Test
    public void updatePurchaseOutboundOrderTest(){
        PurchaseOutboundOrder order = new PurchaseOutboundOrder();
        order.setId(10L);
        order.setSupplierCode("GYS000166");
        order.setWarehouseInfoId(58L);
        //退货类型1-正品，2-残品
        order.setReturnOrderType("2");
        //提货方式1-到仓自提，2-京东配送，3-其他物流
        order.setPickType("1");

        order.setReceiver("liu");
        order.setReceiverNumber("17826821234");
        order.setReceiverProvince("330000");
        order.setReceiverCity("330100");
        order.setReceiverArea("330108");
        order.setReceiverAddress("奥斯卡刘德华");
        order.setReturnPolicy("存在残次品");


        AclUserAccreditInfo info = new AclUserAccreditInfo();
        info.setChannelCode("YWX001");
        info.setUserId("B571346F625E44DB8FCBA8116E72593D");
        purchaseOutboundOrderBiz.updatePurchaseOutboundOrder(order, info);
    }

    /**
     * 获取商品
     */
    @Test
    public void getPurchaseOutboundOrderDetailTest(){
        PurchaseOutboundItemForm form = new PurchaseOutboundItemForm();
        form.setSupplierCode("GYS000166");
        form.setWarehouseInfoId("58");
        //退货类型1-正品，2-残品
        form.setReturnOrderType("2");

        Pagenation<PurchaseOutboundDetail> detail = purchaseOutboundOrderBiz.getPurchaseOutboundOrderDetail(form, new Pagenation<PurchaseOutboundDetail>(), "");
        System.out.println(JSON.toJSONString(detail));
    }

    /**
     * 获取审核列表
     */
    @Test
    public void getAuditPagelist(){

        PurchaseOutboundOrderForm form = new PurchaseOutboundOrderForm();
        //审核状态：1-未审核,3-审核通过
        form.setAuditStatus("1");

        Pagenation<PurchaseOutboundOrder> pagelist = purchaseOutboundOrderBiz.getAuditPagelist(form, new Pagenation<PurchaseOutboundOrder>(), "YWX001");
        System.out.println(JSON.toJSONString(pagelist));
    }
}
