package com.googlecode.cryptogwt.util;

import java.security.*;
import java.security.Provider;
import java.security.Provider.Service;
import java.util.List;
import java.util.Map;

/**
 * Service that supports creation of Service Provider Interfaces using an SpiFactory instance.
 *
 */
public class SpiFactoryService extends Service {

    private SpiFactory<?> factory;
    
    public SpiFactoryService(Provider provider, String type, String algorithm, 
            String className, List<String> aliases, Map<String, String> attributes,
            SpiFactory<?> factory) {
        super(provider, type, algorithm, className, aliases, attributes);
        this.factory = factory;
    }

    @Override
    public Object newInstance(Object constructorParameter)
            throws NoSuchAlgorithmException {
        return factory.create(constructorParameter);
    }    
}
