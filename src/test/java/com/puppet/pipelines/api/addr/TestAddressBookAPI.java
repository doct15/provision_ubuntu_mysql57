package com.puppet.pipelines.api.addr;

import com.distelli.apigen.DataNode;
import com.distelli.cred.CredProvider;
import com.distelli.crypto.KeyProvider;
import com.distelli.persistence.PersistenceConfig;
import com.distelli.persistence.impl.PersistenceModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.puppet.pipelines.webserver.WebDispatcher;
import com.puppet.pipelines.webserver.WebRequest;
import com.puppet.pipelines.webserver.WebResponse;
import java.io.File;
import java.net.URI;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.distelli.utils.Log4JConfigurator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

public class TestAddressBookAPI {
    private static Injector INJECTOR;

    @Inject
    private WebDispatcher _dispatcher;

    @Inject
    private WebRequest.Factory _requestFactory;

    @BeforeClass
    public static void initClass() {
        Log4JConfigurator.configure(true);
        Log4JConfigurator.setLogLevel("INFO");
        Log4JConfigurator.setLogLevel("com.puppet.pipelines.api.addr", "DEBUG");
        Log4JConfigurator.setLogLevel("com.puppet.pipelines.webserver.impl.JSDispatcherImpl", "DEBUG");
        PersistenceConfig config = new PersistenceConfig() {
                public Double getCapacityScaleFactor() {
                    return 1.0;
                }
                public String getTableNameFormat() {
                    return null;
                }
                public URI getEndpoint() {
                    return URI.create("local://localhost");
                }
                public URI getProxy() {
                    return null;
                }
                public CredProvider getCredProvider() {
                    return null;
                }
                public File getFile() {
                    return null;
                }
                public KeyProvider getKeyProvider() {
                    return null;
                }
            };
        INJECTOR = Guice.createInjector(
            new PersistenceModule(config),
            new AddressBookAPIModule());
    }

    @Before
    public void before() {
        INJECTOR.injectMembers(this);
    }

    @Test
    public void test() throws Exception {
        DataNode res = dispatch("GET", "/addresses", null);
        assertEquals(DataNode.NULL_LIST, res.get("rows"));
        DataNode brianSmith = dispatch(
            "POST", "/addresses", DataNode.createMap()
            .put("firstName", "Brian")
            .put("lastName", "Smith")
            .put("country", "US")
            .put("streetLine1", "1 Infinite Loop")
            .put("streetLine2", "Apt# 33")
            .build());
        assertNotNull(brianSmith.get("id").asLong());
        res = dispatch("GET", "/addresses/"+brianSmith.get("id").asLong(), null);
        assertEquals(brianSmith, res);
        res = dispatch("GET", "/addresses", null);
        assertEquals(DataNode.createList().add(brianSmith).build(), res.get("rows"));
        brianSmith = dispatch(
            "PUT", "/addresses/"+brianSmith.get("id").asLong(), DataNode.createMap()
            .put("city", "Portland")
            .put("streetLine1", "308 SW 2nd Ave. Fifth Floor")
            .putNull("streetLine2")
            .build());
        assertTrue(brianSmith.get("streetLine2")+"", brianSmith.get("streetLine2").isEmpty());
        assertEquals(brianSmith.get("streetLine1").asString(null), "308 SW 2nd Ave. Fifth Floor");
        assertEquals(brianSmith.get("firstName").asString(null), "Brian");
        assertEquals(brianSmith.get("city").asString(null), "Portland");
    }

    public static class ErrorStatus extends RuntimeException {
        private int _code;
        public ErrorStatus(int code, String msg) {
            super(code + "Error: "+msg);
            _code = code;
        }
        public int getCode() {
            return _code;
        }
    }

    private DataNode dispatch(String method, String path, DataNode body) throws Exception {
        WebResponse response = _dispatcher.dispatch(
            _requestFactory.builder()
            .method(method)
            .rawUrl("http://localhost"+path)
            .body(factory -> factory.createJson(body))
            .build())
            .call();
        if ( response.getStatusCode()/100 != 2 ) {
            throw new ErrorStatus(response.getStatusCode(), response.getBody().readString(UTF_8));
        }
        return response.getBody().readJson();
    }
}
