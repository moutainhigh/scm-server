package org.trc.resource.api;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.trc.biz.allocateOrder.IAllocateInOrderBiz;
import org.trc.biz.allocateOrder.IAllocateOutOrderBiz;
import org.trc.form.wms.WmsAllocateOutInRequest;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * 自营仓库
 * Created by hzcyn on 2018/5/22.
 */
@Component
@Path("wmsApi")
public class WmsApiResource {

    private Logger logger = LoggerFactory.getLogger(WmsApiResource.class);

    @Autowired
    private IAllocateOutOrderBiz allocateOutOrderBiz;
    @Autowired
    private IAllocateInOrderBiz allocateInOrderBiz;

    @POST
    @Path("allocateOutOrder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response outFinishCallBack(@FormParam("request") String request) throws Exception {
        WmsAllocateOutInRequest req = JSON.parseObject(request, WmsAllocateOutInRequest.class);
        return allocateOutOrderBiz.outFinishCallBack(req);
    }

    @POST
    @Path("allocateInOrder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response inFinishCallBack(@FormParam("request") String request) throws Exception {
        WmsAllocateOutInRequest req = JSON.parseObject(request, WmsAllocateOutInRequest.class);
        return allocateInOrderBiz.inFinishCallBack(req);
    }
}
