package org.trc.biz.impl.afterSale;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.trc.biz.afterSale.IAfterSaleOrderDetailBiz;
import org.trc.domain.afterSale.AfterSaleOrderDetail;
import org.trc.form.afterSale.AfterSaleOrderDetailForm;
import org.trc.service.afterSale.IAfterSaleOrderDetailService;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service("afterSaleOrderDetailBiz")
public class AfterSaleOrderDetailBiz implements IAfterSaleOrderDetailBiz{

    private Logger LOGGER = LoggerFactory.getLogger(AfterSaleOrderDetailBiz.class);

    @Autowired
    private IAfterSaleOrderDetailService afterSaleOrderDetailService;

    /**
     * @Description: 售后单字表查询
     * @Author: hzluoxingcheng
     * @Date: 2018/8/29
     */ 
    @Override
    public List<AfterSaleOrderDetail> queryListByCondition(AfterSaleOrderDetailForm afterSaleOrderDetailForm) {
        //查询条件是skucode
        String skuCode = afterSaleOrderDetailForm.getSkuCode();
        //sku名称
        String skuName = afterSaleOrderDetailForm.getSkuName();

        Example example = new Example(AfterSaleOrderDetail.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotBlank(skuCode)){
            criteria.andEqualTo("skuCode",skuCode);
        }
        if(StringUtils.isNotBlank(skuName)){
            criteria.andEqualTo("skuName",skuName);
        }
        return  afterSaleOrderDetailService.selectByExample(example);
    }
}
