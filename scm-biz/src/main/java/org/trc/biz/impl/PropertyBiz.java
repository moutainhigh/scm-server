package org.trc.biz.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.trc.biz.IPropertyBiz;
import org.trc.domain.category.Property;
import org.trc.form.PropertyForm;
import org.trc.service.category.IPropertyService;
import org.trc.service.category.impl.PropertyService;
import org.trc.util.Pagenation;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by hzqph on 2017/5/5.
 */
public class PropertyBiz implements IPropertyBiz {
    @Autowired
    private IPropertyService propertyService;
    @Override
    public Pagenation<Property> propertyPage(PropertyForm queryModel, Pagenation<Property> page) throws Exception {
        Example example=new Example(Property.class);
        Example.Criteria criteria=example.createCriteria();
        if (!StringUtils.isBlank(queryModel.getName())){
            criteria.andLike("name","%"+ queryModel.getName()+"%");
        }
        if (!StringUtils.isBlank(queryModel.getTypeCode())){
            criteria.andEqualTo("typeCode",queryModel.getTypeCode());
        }
        if(!StringUtils.isBlank(queryModel.getIsValid())){
            criteria.andEqualTo("isValid", queryModel.getIsValid());
        }
        example.orderBy("isValid").desc();
        example.orderBy("updateTime").desc();
        Pagenation<Property> pagenation=propertyService.pagination(example,page,queryModel);
        List<Property> list=pagenation.getResult();
        int num=1;
        for (Property property:list) {
            property.setNum(num++);
        }
        pagenation.setResult(list);
        return pagenation;
    }
}
