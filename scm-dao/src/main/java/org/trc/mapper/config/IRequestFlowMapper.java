package org.trc.mapper.config;


import org.trc.domain.config.RequestFlow;
import org.trc.util.BaseMapper;

/**
 * Created by hzdzf on 2017/6/7.
 */
public interface IRequestFlowMapper extends BaseMapper<RequestFlow> {

    int changeState(String requestNum) throws Exception;

    int updateRequestFlowByRequestNum(RequestFlow requestFlow) throws Exception;
}
