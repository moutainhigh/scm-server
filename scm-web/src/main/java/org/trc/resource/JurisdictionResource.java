package org.trc.resource;

import org.springframework.stereotype.Component;
import org.trc.biz.impower.IJurisdictionBiz;
import org.trc.constants.SupplyConstants;
import org.trc.domain.impower.Jurisdiction;
import org.trc.util.AppResult;
import org.trc.util.ResultUtil;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * 资源控制
 * Created by sone on 2017/5/11.
 */
@Component
@Path(SupplyConstants.Jurisdiction.ROOT)
public class JurisdictionResource {
    @Resource
    private IJurisdictionBiz jurisdictionBiz;
    /**
     * 提供两种角色下对应的角色权限
     * 1.提供全局角色对应的权限资源
     * 2.提供渠道角色对应的权限资源
     */
    @GET
    @Path(SupplyConstants.Jurisdiction.JURISDICTION_WHOLE)
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult<Jurisdiction> findWholeJurisdiction() throws Exception{

        return ResultUtil.createSucssAppResult("查询全局角色成功", jurisdictionBiz.findWholeJurisdiction());

    }
    @GET
    @Path(SupplyConstants.Jurisdiction.JURISDICTION_CHANNEL)
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult<Jurisdiction> findChannelJurisdiction() throws Exception{

        return ResultUtil.createSucssAppResult("查询渠道角色成功", jurisdictionBiz.findChannelJurisdiction());

    }
    /**
     * 提供两种角色下对应的角色权限，用于回写被选中的权限
     * 1.提供带有角色id的，角色与权限的关联信息查询<全局角色>
     * 2.提供带有角色id的，角色与权限的关联信息查询<渠道角色>
     */
    @GET
    @Path(SupplyConstants.Jurisdiction.JURISDICTION_WHOLE+"/{roleId}")
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult<Jurisdiction> findWholeJurisdictionAndCheckedByRoleId(@PathParam("roleId") Long roleId ) throws Exception{

        return ResultUtil.createSucssAppResult("查询全局角色成功", jurisdictionBiz.findWholeJurisdictionAndCheckedByRoleId(roleId));

    }
    @GET
    @Path(SupplyConstants.Jurisdiction.JURISDICTION_CHANNEL+"/{roleId}")
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult<Jurisdiction> findChannelJurisdictionAndCheckedByRoleId(@PathParam("roleId") Long roleId ) throws Exception{

        return ResultUtil.createSucssAppResult("查询全局角色成功", jurisdictionBiz.findChannelJurisdictionAndCheckedByRoleId(roleId));

    }

}
