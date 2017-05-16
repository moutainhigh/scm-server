package org.trc.domain.util;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.validator.constraints.Length;
import org.trc.custom.CustomDateSerializer;

import javax.ws.rs.FormParam;
import java.util.Date;

/**
 * Created by hzwdx on 2017/5/9.
 */
public class CommonDO extends ScmDO{

    @FormParam("createOperator")
    @Length(max = 32, message = "字典类型编码字母和数字不能超过32个,汉字不能超过16个")
    private String createOperator; //创建人

    public String getCreateOperator() {
        return createOperator;
    }

    public void setCreateOperator(String createOperator) {
        this.createOperator = createOperator;
    }


}