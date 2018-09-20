package com.puppet.pipelines.api.addr.ajax;

import com.puppet.pipelines.webserver.JSHandler;
import com.puppet.pipelines.webserver.JSRequest;
import com.puppet.pipelines.webserver.JSResponse;
import javax.inject.Singleton;
import javax.inject.Inject;
import com.puppet.pipelines.api.addr.AddressDB;
import com.puppet.pipelines.webcomponents.PageRowsIterator;

@Singleton
public class ListAddresses implements JSHandler {
    
    @Inject
    protected ListAddresses() {
    }

    @Inject
    private AddressDB _addressDB;
    @Inject
    private PageRowsIterator.Factory _pageRowsFactory;

    @Override
    public JSResponse service(JSRequest req) throws Exception {
        PageRowsIterator it = _pageRowsFactory.createDefaultForward(req);
        return it.response(_addressDB.list(it.getIterator()));
    }
}
