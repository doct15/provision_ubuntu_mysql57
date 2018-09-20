package com.puppet.pipelines.api.addr.ajax;

import com.puppet.pipelines.webserver.JSHandler;
import com.puppet.pipelines.webserver.JSRequest;
import com.puppet.pipelines.webserver.JSResponse;
import javax.inject.Singleton;
import javax.inject.Inject;
import com.puppet.pipelines.api.addr.AddressDB;
import com.puppet.pipelines.webserver.ParamExtractor;
import com.puppet.pipelines.webcomponents.RBAC;

@Singleton
public class DeleteAddress implements JSHandler {
    
    @Inject
    protected DeleteAddress() {}

    @Inject
    private AddressDB _addressDB;
    @Inject
    private RBAC.Factory _rbac;
    @Inject
    private ParamExtractor _extractor;

    @Override
    public JSResponse service(JSRequest req) throws Exception {
        _rbac.anyone().check(req);

        _addressDB.remove(
            _extractor.fromRouteAttr(req, "id").asLong().get());

        return req.responseFactory().ok();
    }
}
