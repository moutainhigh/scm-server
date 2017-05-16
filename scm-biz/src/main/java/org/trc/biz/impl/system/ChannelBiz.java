package org.trc.biz.impl.system;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.trc.biz.system.IChannelBiz;
import org.trc.domain.System.Channel;
import org.trc.enums.CommonExceptionEnum;
import org.trc.enums.ExceptionEnum;
import org.trc.enums.ZeroToNineEnum;
import org.trc.exception.ConfigException;
import org.trc.exception.ParamValidException;
import org.trc.form.system.ChannelForm;
import org.trc.service.System.IChannelService;
import org.trc.service.util.ISerialUtilService;
import org.trc.util.CommonUtil;
import org.trc.util.Pagenation;
import org.trc.util.ParamsUtil;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by sone on 2017/5/2.
 */
@Service("channelBiz")
public class ChannelBiz implements IChannelBiz {

    private final static Logger log = LoggerFactory.getLogger(ChannelBiz.class);

    private final static String  SERIALNAME="QD";

    private final static Integer LENGTH=3;

    @Resource
    private IChannelService channelService;

    @Resource
    private ISerialUtilService serialUtilService;

    @Override
    public Pagenation<Channel> channelPage(ChannelForm form, Pagenation<Channel> page) throws Exception {
        Example example = new Example(Channel.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtil.isNotEmpty(form.getName())) {
            criteria.andLike("name", "%" + form.getName() + "%");
        }
        if(StringUtil.isNotEmpty(form.getIsValid())) {
            criteria.andEqualTo("isValid", form.getIsValid());
        }
        example.orderBy("updateTime").desc();
        Pagenation<Channel> pagenation = channelService.pagination(example,page,form);

        return pagenation;
    }

    public Channel findChannelByName(String name) throws Exception{
        if(StringUtil.isEmpty(name) || name==null){
            String msg = CommonUtil.joinStr("根据渠道名称查询渠道的参数name为空").toString();
            log.error(msg);
            throw  new ParamValidException(CommonExceptionEnum.PARAM_CHECK_EXCEPTION, msg);
        }
        Channel channel=new Channel();
        channel.setName(name);
        return channelService.selectOne(channel);
    }
    @Override
    public int saveChannel(Channel channel) throws Exception {

        Channel tmp = findChannelByName(channel.getName());
        if(null != tmp){
            String msg = CommonUtil.joinStr("渠道名称[name=",channel.getName(),"]的数据已存在,请使用其他名称").toString();
            log.error(msg);
            throw new ConfigException(ExceptionEnum.SYSTEM_CHANNEL_SAVE_EXCEPTION, msg);
        }

        String code = serialUtilService.getSerialCode(SERIALNAME,LENGTH);//查询当前的序列位置


        channel.setCode(code);
        ParamsUtil.setBaseDO(channel);
        int count=0;
        count=channelService.insert(channel);
        if(count == 0){
            String msg = CommonUtil.joinStr("保存渠道", JSON.toJSONString(channel),"数据库操作失败").toString();
            log.error(msg);
            throw new ConfigException(ExceptionEnum.SYSTEM_CHANNEL_SAVE_EXCEPTION, msg);
        }
        return count;
    }

    @Override
    public int updateChannel(Channel channel, Long id) throws Exception {
        if(null == id){
            String msg = CommonUtil.joinStr("修改渠道参数ID为空").toString();
            log.error(msg);
            throw new ParamValidException(CommonExceptionEnum.PARAM_CHECK_EXCEPTION, msg);
        }
        int count = 0;
        channel.setId(id);
        channel.setUpdateTime(Calendar.getInstance().getTime());
        count = channelService.updateByPrimaryKeySelective(channel);
        if(count == 0){
            String msg = CommonUtil.joinStr("修改渠道",JSON.toJSONString(channel),"数据库操作失败").toString();
            log.error(msg);
            throw new ConfigException(ExceptionEnum.SYSTEM_CHANNEL_UPDATE_EXCEPTION, msg);
        }
        return count;
    }

    @Override
    public Channel findChannelById(Long id) throws Exception {
        if(null == id){
            String msg = CommonUtil.joinStr("根据ID查询渠道参数ID为空").toString();
            log.error(msg);
            throw new ParamValidException(CommonExceptionEnum.PARAM_CHECK_EXCEPTION, msg);
        }
        Channel channel = new Channel();
        channel.setId(id);
        channel = channelService.selectOne(channel);
        if(null == channel) {
            String msg = CommonUtil.joinStr("根据主键ID[id=", id.toString(), "]查询渠道为空").toString();
            throw new ConfigException(ExceptionEnum.SYSTEM_CHANNEL_QUERY_EXCEPTION,msg);
        }
        return channel;
    }

    @Override
    public int updateChannelState(Channel channel) throws Exception {
        Long id = channel.getId();
        String state = channel.getIsValid();
        int stateInt = Integer.parseInt(state);
        if(stateInt==1){
            channel.setIsValid(0+"");
        }else if(stateInt==0){
            channel.setIsValid(1+"");
        }else {
            String msg = CommonUtil.joinStr("参数的状态值不符合要求").toString();
            log.error(msg);
            throw new ParamValidException(CommonExceptionEnum.PARAM_CHECK_EXCEPTION, msg);
        }
        Channel updateChannel=new Channel();
        updateChannel.setId(channel.getId());
        updateChannel.setIsValid(channel.getIsValid());
        updateChannel.setUpdateTime(Calendar.getInstance().getTime());
        int count=channelService.updateByPrimaryKeySelective(updateChannel);
        if(count == 0){
            String msg = CommonUtil.joinStr("修改渠道",JSON.toJSONString(channel),"数据库操作失败").toString();
            log.error(msg);
            throw new ConfigException(ExceptionEnum.SYSTEM_CHANNEL_UPDATE_EXCEPTION, msg);
        }
        return count;
    }

    @Override
    public List<Channel> channelList(ChannelForm form) throws Exception {
        Channel channel = new Channel();
        BeanUtils.copyProperties(form,channel);
        if(StringUtils.isEmpty(form.getIsValid())){
            channel.setIsValid(ZeroToNineEnum.ONE.getCode());
        }
        channel.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
        return channelService.select(channel);
    }
}