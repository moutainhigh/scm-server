package org.trc;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.trc.domain.goods.SkuStock;
import org.trc.mapper.goods.ISkuStockMapper;

import tk.mybatis.mapper.entity.Example;

@RunWith(SpringJUnit4ClassRunner.class)  //标记测试运行的环境
@ContextConfiguration({"classpath:config/resource-context.xml"}) //配合spring测试  可以引入多个配置文件
public class TestSql {
	@Autowired
	ISkuStockMapper skuStockMapper;
	@Test
	public void test () {
		SkuStock record = new SkuStock();
		record.setSkuCode("SP0201707250000034");
		Example ex = new Example(SkuStock.class);
		ex.createCriteria().andEqualTo("skuCode", "SP0201707240000002");
		List<SkuStock> select = skuStockMapper.selectByExample(ex);
		System.err.println(select.size());
	}
}