package com.puppet.pipelines.api.addr.ajax;

import com.puppet.pipelines.webserver.JSHandler;
import com.puppet.pipelines.webserver.JSRequest;
import com.puppet.pipelines.webserver.JSResponse;
import javax.inject.Singleton;
import javax.inject.Inject;
import com.puppet.pipelines.api.addr.AddressDB;
import com.puppet.pipelines.api.addr.Address;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.distelli.apigen.DataNode;
import com.puppet.pipelines.webcomponents.RBAC;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;
import com.distelli.apigen.jackson.DataNodeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;

@Singleton
public class AddAddress implements JSHandler {
    
    @Inject
    protected AddAddress() {
        _om.registerModule(new MrBeanModule());
        _om.registerModule(new DataNodeModule());
        _om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        _om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Inject
    private AddressDB _addressDB;

    private ObjectMapper _om = new ObjectMapper();

    @Override
    public JSResponse service(JSRequest req) throws Exception {
        Address addr = _om.convertValue(req.getContent(), Address.class);
        return req.responseFactory().ok(
            _om.convertValue(
                _addressDB.add(addr),
                DataNode.class));
    }
}
