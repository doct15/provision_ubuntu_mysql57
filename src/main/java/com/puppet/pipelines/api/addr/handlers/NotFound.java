package com.puppet.pipelines.api.addr.handlers;

import com.distelli.webserver.GenericRequestHandler;
import com.distelli.webserver.GenericRouteSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puppet.pipelines.webserver.JSErrors;
import com.puppet.pipelines.webserver.WebHandler;
import com.puppet.pipelines.webserver.WebRequest;
import com.puppet.pipelines.webserver.WebResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import static java.nio.charset.StandardCharsets.UTF_8;
import com.google.common.base.Suppliers;
import com.google.common.base.Supplier;

@Singleton
public class NotFound implements WebHandler {
    
    @Inject
    protected NotFound() {}

    private Supplier<List<String>> _routesSupplier;

    @Inject
    protected void init(Set<GenericRouteSpec<GenericRequestHandler>> routeSpecs) {
        _routesSupplier = Suppliers.memoize(
            () -> routeSpecs.stream()
            .map(spec -> spec.getHttpMethod() + " " + spec.getPath())
            .collect(Collectors.toList()));
    }

    @Override
    public WebResponse service(WebRequest req) throws Exception {
        String msg = "No route matched "+req.getMethod()+" "+
            req.getUrl().toRawUrl(UTF_8).getRawPath()+" available routes:\n"+
            _routesSupplier.get().stream().collect(Collectors.joining("\n"));
        return req.responseFactory().create(
            req.jsResponseFactory().error(
                JSErrors.NotFound, msg));
    }
}
