package com.puppet.pipelines.api.addr;

import com.distelli.persistence.Index;
import com.distelli.persistence.UpdateItemBuilder;
import com.distelli.persistence.PageIterator;
import com.distelli.persistence.AttrType;
import com.distelli.persistence.TableDescription;
import javax.inject.Singleton;
import javax.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.monitor.Sequence;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import javax.persistence.RollbackException;
import com.neovisionaries.i18n.CountryCode;

public class AddressDB {
    public static String CONSTANT = "c";
    public static final TableDescription ADDRESSES_TABLE = TableDescription.builder()
        .tableName("addresses")
        .index(idx -> idx
               .hashKey("c", AttrType.STR)
               .rangeKey("id", AttrType.NUM)
               .build())
        .build();

    private Index<Address> _main;
    @Inject
    private Sequence _sequence;
    @Inject
    private Address.Factory _addressFactory;

    @Inject
    protected AddressDB() {}

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(Address.class, Address.Builder.class)
            .constructor((tree, codec) -> _addressFactory.builder())
            .build(builder -> builder.build())
            .put("c", String.class, (addr) -> CONSTANT)
            .put("id", Long.class, "id")
            .put("fnam", String.class, "firstName")
            .put("lnam", String.class, "lastName")
            .put("cnam", String.class, "companyName")
            .put("ph1", String.class, "phoneNumber")
            .put("ph2", String.class, "altPhoneNumber")
            .put("ln1", String.class, "streetLine1")
            .put("ln2", String.class, "streetLine2")
            .put("st8", String.class, "state")
            .put("zip", String.class, "postalCode")
            .put("city", String.class, "city")
            .put("cou", String.class, "country");
        return module;
    }

    @Inject
    protected void init(Index.Factory indexFactory) {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(createTransforms(new TransformModule()));
        _main = indexFactory.create(Address.class)
            .withTableDescription(ADDRESSES_TABLE)
            .withConvertValue(om::convertValue)
            .build();
    }

    public Address add(Address addr) {
        if ( null == addr ) {
            throw new IllegalArgumentException("Expected non-null address");
        }
        if ( null != addr.getId() ) {
            throw new IllegalArgumentException("Expected id field to be null");
        }
        validate(addr);
        // Assign an ID:
        addr = _addressFactory.builder(addr)
            .id(_sequence.next(ADDRESSES_TABLE.getTableName()))
            .build();
        _main.putItemOrThrow(addr);
        return addr;
    }

    public List<Address> list(PageIterator it) {
        return _main.queryItems(CONSTANT, it).list();
    }

    public void remove(long id) {
        _main.deleteItem(CONSTANT, id);
    }

    public Address get(long id) throws EntityNotFoundException {
        Address result = _main.getItem(CONSTANT, id);
        if ( null != result ) return result;
        throw new EntityNotFoundException(
            "No address was found with id="+id);
    }

    public Update update(long id) {
        return new Update(id);
    }

    public class Update {
        private UpdateItemBuilder<Address> _builder;
        private long _id;

        private Update(long id) {
            _builder = _main.updateItem(CONSTANT, id);
            _id = id;
        }

        public Update firstName(String val) {
            return setOrRemove("fnam", val);
        }

        public Update lastName(String val) {
            return setOrRemove("lnam", val);
        }

        public Update companyName(String val) {
            return setOrRemove("cnam", val);
        }

        public Update phoneNumber(String val) {
            return setOrRemove("ph1", val);
        }

        public Update altPhoneNumber(String val) {
            return setOrRemove("ph2", val);
        }

        public Update streetLine1(String val) {
            return setOrRemove("ln1", val);
        }

        public Update streetLine2(String val) {
            return setOrRemove("ln2", val);
        }

        public Update state(String val) {
            return setOrRemove("st8", val);
        }

        public Update postalCode(String val) {
            return setOrRemove("zip", val);
        }

        public Update city(String val) {
            return setOrRemove("city", val);
        }

        public Update country(String val) {
            return setOrRemove("cou", val);
        }

        public Address now() throws EntityNotFoundException {
            try {
                return _builder
                    .returnAllNew()
                    .when(expr -> expr.exists("c"));
            } catch ( RollbackException ex ) {
                throw new EntityNotFoundException("No address exists with id="+_id);
            }
        }

        private Update setOrRemove(String attrName, String val) {
            if ( null == val ) {
                _builder.remove(attrName);
            } else {
                _builder.set(attrName, val);
            }
            return this;
        }
    }

    private void validate(Address addr) throws IllegalArgumentException {
        if ( !validCountryCode(addr.getCountry()) ) {
            throw new IllegalArgumentException(
                "Expected country to be non-null ISO 3166-1 alpha-2 country code");
        }
        if ( null == addr.getStreetLine1() || addr.getStreetLine1().trim().isEmpty() ) {
            throw new IllegalArgumentException("Expected streetLine1 to be non-empty");
        }
        // TODO: add more validation?
    }

    private boolean validCountryCode(String code) {
        if ( null == code || code.length() != 2 ) return false;
        return null != CountryCode.getByCode(code);
    }
}
