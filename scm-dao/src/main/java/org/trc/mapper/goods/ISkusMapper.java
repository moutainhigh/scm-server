package org.trc.mapper.goods;

import org.apache.ibatis.annotations.Param;
import org.trc.domain.goods.Skus;
import org.trc.util.BaseMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hzwdx on 2017/5/24.
 */
public interface ISkusMapper extends BaseMapper<Skus>{

    Integer updateSkus(List<Skus> skusList) throws Exception;

    List<Skus> selectSkuList(Map<String, Object> map);

    Integer selectSkuListCount(Map<String, Object> map);

    List<String> selectAllBarCode(@Param("notInList")List<String> notInList);

    Set<String> selectSkuListByBarCode(@Param("barCodeList")List<String> barCodeList);
}
