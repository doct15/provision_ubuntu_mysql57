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
import com.puppet.pipelines.webserver.ParamExtractor;
import com.puppet.pipelines.webserver.InvalidParamValue;
import java.util.function.Function;
import com.distelli.apigen.jackson.DataNodeModule;
import com.puppet.pipelines.webcomponents.RBAC;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;

@Singleton
public class UpdateAddress implements JSHandler {
    
    @Inject
    protected UpdateAddress() {
        _om.registerModule(new DataNodeModule());
        _om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        _om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Inject
    private AddressDB _addressDB;
    @Inject
    private ParamExtractor _extractor;
    @Inject
    private RBAC.Factory _rbac;

    private ObjectMapper _om = new ObjectMapper();

    @Override
    public JSResponse service(JSRequest req) throws Exception {
        _rbac.anyone().check(req);
        AddressDB.Update update = _addressDB.update(
            _extractor.fromRouteAttr(req, "id").asLong().get());
        DataNode body = req.getContent();
        if ( body.asMap().isEmpty() ) {
            throw new InvalidParamValue(
                "Expected non-empty JSON object", "body");
        }
        update(body, "firstName", update::firstName);
        update(body, "lastName", update::lastName);
        update(body, "companyName", update::companyName);
        update(body, "phoneNumber", update::phoneNumber);
        update(body, "altPhoneNumber", update::altPhoneNumber);
        update(body, "streetLine1", update::streetLine1);
        update(body, "streetLine2", update::streetLine2);
        update(body, "state", update::state);
        update(body, "postalCode", update::postalCode);
        update(body, "city", update::city);
        update(body, "country", update::country);
        return req.responseFactory().ok(
            _om.convertValue(update.now(), DataNode.class));
    }
    private void update(DataNode body, String fieldName, Function<String, AddressDB.Update> fn) {
        DataNode value = body.get(fieldName);
        if ( value.isEmpty() ) return;
        if ( !value.isNull() && !value.isString() ) {
            throw new InvalidParamValue(
                "Expected '"+fieldName+"' to be a string or null type",
                fieldName);
        }
        fn.apply(value.asString(null));
    }
}
