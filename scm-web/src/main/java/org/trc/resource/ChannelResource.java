package org.trc.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.trc.biz.system.IChannelBiz;
import org.trc.constants.SupplyConstants;
import org.trc.domain.System.Channel;
import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.form.system.ChannelForm;
import org.trc.util.Pagenation;
import org.trc.util.ResultUtil;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author sone
 * @date 2017/5/2
 */
@Component
@Path(SupplyConstants.Channel.ROOT)
public class ChannelResource {

    @Autowired
    private IChannelBiz channelBiz;

    //渠道分页查询
    @GET
    @Path(SupplyConstants.Channel.CHANNEL_PAGE)
    @Produces(MediaType.APPLICATION_JSON)
    public Response channelPage(@BeanParam ChannelForm form, @BeanParam Pagenation<Channel> page){
        return ResultUtil.createSuccessPageResult(channelBiz.channelPage(form,page));
    }

    //渠道分页查询
    @GET
    @Path(SupplyConstants.Channel.SALES_CHANNEL)
    @Produces(MediaType.APPLICATION_JSON)
    public Response querySellChannelByChannelCode(@Context ContainerRequestContext requestContext){
        return  ResultUtil.createSuccessResult("查询销售渠道列表成功", channelBiz.querySellChannelByChannelCode((AclUserAccreditInfo) requestContext.getProperty(SupplyConstants.Authorization.ACL_USER_ACCREDIT_INFO)));
    }

    //根据渠道名查询渠道
    @GET
    @Path(SupplyConstants.Channel.CHANNEL)
    @Produces(MediaType.APPLICATION_JSON)
    public Response findChannelByName(@QueryParam("name") String name){
        //  前台接受为null则数据没问题 ，有数据则名称不能使用，"1" 为标志存在数据
        return  ResultUtil.createSuccessResult("查询业务线成功", channelBiz.findChannelByName(name)==null ? null :"1");
    }

    //渠道列表查询
    @GET
    @Path(SupplyConstants.Channel.CHANNEL_LIST)
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryChannels(@BeanParam ChannelForm channelForm) {
        return  ResultUtil.createSuccessResult("查询业务线列表成功", channelBiz.queryChannels(channelForm));
    }


    //销售渠道列表查询
    @GET
    @Path(SupplyConstants.Channel.SELL_CHANNEL_LIST)
    @Produces(MediaType.APPLICATION_JSON)
    public Response querySellChannelList() {
        return  ResultUtil.createSuccessResult("查询销售渠道列表成功", channelBiz.querySellChannel());
    }

    //保存渠道
    @POST
    @Path(SupplyConstants.Channel.CHANNEL)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/x-www-form-urlencoded")
    public Response saveChannel(@BeanParam Channel channel,@Context ContainerRequestContext requestContext){
        channelBiz.saveChannel(channel,(AclUserAccreditInfo) requestContext.getProperty(SupplyConstants.Authorization.ACL_USER_ACCREDIT_INFO));
        return  ResultUtil.createSuccessResult("保存成功","");
    }

    //根据id查询
    @GET
    @Path(SupplyConstants.Channel.CHANNEL+"/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findChannelById(@PathParam("id") Long id){
        return ResultUtil.createSuccessResult("查询业务线成功", channelBiz.findChannelById(id));
    }

    //根据id查询,编辑页面回写数据
    @GET
    @Path(SupplyConstants.Channel.CHANNEL_ID_SELL_CHANNEL+"/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findChannelByIdForUpdate(@PathParam("id") Long id){
        return ResultUtil.createSuccessResult("查询业务线信息成功", channelBiz.queryChannelForUpdate(id));
    }

    //根据id查询已关联的销售渠道
    @GET
    @Path(SupplyConstants.Channel.CHANNEL_ID+"/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLinkSellChannelById(@PathParam("id") Long id){
        return ResultUtil.createSuccessResult("查询已关联的销售渠道成功", channelBiz.selectLinkSellChannelById(id));
    }
    //渠道修改
    @PUT
    @Path(SupplyConstants.Channel.CHANNEL+"/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateChannel(@BeanParam  Channel channel,@Context ContainerRequestContext requestContext){
        channelBiz.updateChannel(channel,(AclUserAccreditInfo) requestContext.getProperty(SupplyConstants.Authorization.ACL_USER_ACCREDIT_INFO));
        return  ResultUtil.createSuccessResult("修改业务线信息成功","");
    }

    //渠道状态的修改
    @PUT
    @Path(SupplyConstants.Channel.UPDATE_STATE+"/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateChannelState(@BeanParam Channel channel){
        channelBiz.updateChannelState(channel);
       return ResultUtil.createSuccessResult("状态修改成功","");
    }

    //查询当前登录用户所属业务线已关联的销售渠道
    @GET
    @Path(SupplyConstants.Channel.YWX_SELL_CHANNEL_LIST)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ywxSellChannelList(@Context ContainerRequestContext requestContext){
        return ResultUtil.createSuccessResult("查询当前登录用户所属业务线已关联的销售渠道成功",
                channelBiz.querySellChannelList((AclUserAccreditInfo) requestContext.getProperty(SupplyConstants.Authorization.ACL_USER_ACCREDIT_INFO)));
    }
}
