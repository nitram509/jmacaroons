package java.security;

import java.util.LinkedHashSet;
import java.util.Set;
import java.security.Provider;

import com.google.gwt.core.client.GWT;
import com.googlecode.cryptogwt.provider.CryptoGwtProvider;

public class Security {
        
    private static final Set<Provider> providers = new LinkedHashSet<Provider>();
    
    public static Provider getProvider(String name) {
        for (Provider provider : Security.getProviders()) {
            if (provider.getName().equals(name)) {
                return provider;
            }
        }
        return null;
    }
    
    public static Provider[] getProviders() {
        if (GWT.isClient()) {
         // TODO: Use deferred binding to load provider from config
            return new Provider[] { CryptoGwtProvider.INSTANCE };
        }
        return providers.toArray(new Provider[providers.size()]);
    }
    
    public static int addProvider(Provider provider) {
        providers.add(provider);
        return providers.size() - 1;
    }

}
