package com.puppet.pipelines.api.addr;

import org.immutables.value.Value.Immutable;
import javax.annotation.Nullable;
import javax.inject.Inject;

@Immutable(copy=false)
public interface Address {
    public static class Factory {
        @Inject
        protected Factory() {}
        public Builder builder() {
            return ImmutableAddress.builder();
        }
        public Builder builder(Address address) {
            return ImmutableAddress.builder()
                .from(address);
        }
    }
    public interface Builder {
        public Builder id(Long value);
        public Builder firstName(String value);
        public Builder lastName(String value);
        public Builder companyName(String value);
        public Builder phoneNumber(String value);
        public Builder altPhoneNumber(String value);
        public Builder streetLine1(String value);
        public Builder streetLine2(String value);
        public Builder state(String value);
        public Builder postalCode(String value);
        public Builder city(String value);
        public Builder country(String value);
        public Address build();
    }

    @Nullable
    public Long getId();

    @Nullable
    public String getFirstName();
    @Nullable
    public String getLastName();
    @Nullable
    public String getCompanyName();

    @Nullable
    public String getPhoneNumber();
    @Nullable
    public String getAltPhoneNumber();

    @Nullable
    public String getStreetLine1();
    @Nullable
    public String getStreetLine2();
    @Nullable
    public String getState();
    @Nullable
    public String getPostalCode();
    @Nullable
    public String getCity();

    // https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
    @Nullable
    public String getCountry();
}
