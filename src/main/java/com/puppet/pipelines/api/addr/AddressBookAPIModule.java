package com.puppet.pipelines.api.addr;

import com.puppet.pipelines.api.addr.handlers.RestWebHandler;
import com.puppet.pipelines.api.addr.handlers.NotFound;
import com.puppet.pipelines.api.addr.ajax.*;
import com.puppet.pipelines.webserver.WebBinder;
import com.puppet.pipelines.webserver.WebMethod;
import com.google.inject.AbstractModule;
import com.puppet.pipelines.webcomponents.WebComponentModuleBuilder;
import com.puppet.pipelines.webcomponents.SecretService;
import com.google.inject.multibindings.Multibinder;
import com.distelli.persistence.TableDescription;
import javax.inject.Inject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import javax.inject.Provider;
public class AddressBookAPIModule extends AbstractModule {
    private static class RestProvider implements Provider<RestWebHandler> {
        @Inject
        private RestWebHandler.Factory _factory;
        private String _op;
        public RestProvider(String op) {
            _op = op;
        }
        @Override
        public RestWebHandler get() {
            return _factory.create(_op);
        }
    }
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(RestWebHandler.Factory.class));
        bind(Address.Factory.class);
        // This is a demo, so don't bother with this...
        bind(SecretService.MasterKey.class)
            .toInstance(
                new SecretService.MasterKey() {
                    public byte[] getMasterKey(String masterKeyId) {
                        if ( "INSECURE".equals(masterKeyId) ) {
                            return "0123456789abcdef".getBytes();
                        }
                        return null;
                    }
                    public String getDefaultMasterKeyId() {
                        return "INSECURE";
                    }
                });
        Multibinder<TableDescription> tables = Multibinder.newSetBinder(binder(), TableDescription.class);
        tables.addBinding().toInstance(AddressDB.ADDRESSES_TABLE);

        WebComponentModuleBuilder.create()
            .disableStandardRoutes()
            .disableStandardFilters("RBAC")
            .disableStandardAjaxFilters("AntiCSRF", "RBAC")
            .disableBuiltinMasterKeys()
            .disableStandardAjax()
            .build(binder());

        WebBinder webBinder = WebBinder.create(binder());

        webBinder.addRoute()
            .path("/addresses")
            .method(WebMethod.GET)
            .linked()
            .toProvider(new RestProvider("ListAddresses"));
        webBinder.addRoute()
            .path("/addresses/:id")
            .method(WebMethod.GET)
            .linked()
            .toProvider(new RestProvider("GetAddress"));
        webBinder.addRoute()
            .path("/addresses")
            .method(WebMethod.POST)
            .linked()
            .toProvider(new RestProvider("AddAddress"));
        webBinder.addRoute()
            .path("/addresses/:id")
            .method(WebMethod.PUT)
            .linked()
            .toProvider(new RestProvider("UpdateAddress"));
        webBinder.addRoute()
            .path("/addresses/:id")
            .method(WebMethod.DELETE)
            .linked()
            .toProvider(new RestProvider("DeleteAddress"));

        webBinder.addJSHandler()
            .name("ListAddresses")
            .linked()
            .to(ListAddresses.class);
        webBinder.addJSHandler()
            .name("GetAddress")
            .linked()
            .to(GetAddress.class);
        webBinder.addJSHandler()
            .name("AddAddress")
            .linked()
            .to(AddAddress.class);
        webBinder.addJSHandler()
            .name("UpdateAddress")
            .linked()
            .to(UpdateAddress.class);
        webBinder.addJSHandler()
            .name("DeleteAddress")
            .linked()
            .to(DeleteAddress.class);

        // Define a simple 404 page:
        webBinder.addRoute()
            .path("/404")
            .method(WebMethod.GET)
            .linked()
            .to(NotFound.class);
    }
}
