package com.puppet.pipelines.api.addr.handlers;

import com.puppet.pipelines.webserver.WebHandler;
import com.puppet.pipelines.webserver.WebRequest;
import com.puppet.pipelines.webserver.WebResponse;
import com.puppet.pipelines.webserver.JSHandlerNotFound;
import com.puppet.pipelines.webserver.JSDispatcher;
import com.puppet.pipelines.webserver.JSErrors;
import com.puppet.pipelines.webserver.JSRequest;
import com.puppet.pipelines.webserver.JSResponse;
import javax.inject.Inject;
import com.puppet.pipelines.webcomponents.RBAC;
import java.util.concurrent.Callable;
import com.google.inject.assistedinject.Assisted;

public class RestWebHandler implements WebHandler {
    public interface Factory {
        public RestWebHandler create(@Assisted String op);
    }
    @Inject
    private RBAC.Factory _rbac;
    @Inject
    private JSDispatcher _dispatcher;
    private String _op;

    @Inject
    protected RestWebHandler(@Assisted String op) {
        _op = op;
    }

    @Override
    public WebResponse service(WebRequest req) throws Exception {
        Callable<JSResponse> fn = null;
        JSResponse res = null;
        JSRequest request = req.jsRequestBuilder()
            .query(req.getUrl().getQuery())
            .content(req.getBody().readJson())
            .build();
        try {
            fn = _dispatcher.dispatch(_op, request);
        } catch ( JSHandlerNotFound ex ) {
            res = req.jsResponseFactory()
                .error(JSErrors.BadRequest, "No JSHandler for op="+_op);
        }
        if ( null != fn ) {
            res = fn.call();
        }
        return req.responseFactory()
            .create(res);
    }
}
