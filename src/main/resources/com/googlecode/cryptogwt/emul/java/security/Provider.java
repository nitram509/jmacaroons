package java.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.googlecode.cryptogwt.util.SpiFactory;

public class Provider {
    
    public static class Service {
        private final Provider provider;
        private final String type;
        private final String algorithm;
        private final String className;
        private final List<String> aliases;
        private final Map<String, String> attributes;
        private final SpiFactory<?> factory;
        
        protected Service(Provider provider, String type, String algorithm, 
                String className, List<String> aliases, Map<String, String> attributes,
                SpiFactory<?> factory) {
            this.provider = provider;
            this.type = type;
            this.algorithm = algorithm;
            this.className = className;
            this.aliases = aliases;
            this.attributes = attributes;
            this.factory = factory;
        }
        
        public Service(Provider provider, String type, String algorithm,
                String className, List<String> aliases,
                Map<String, String> attributes) {
            this.provider = provider;
            this.type = type;
            this.algorithm = algorithm;
            this.className = className;
            this.aliases = aliases;
            this.attributes = attributes;
            this.factory = null;
        }
        
        public Object newInstance(Object constructorParameter) throws 
            NoSuchAlgorithmException {
            return this.factory.create(constructorParameter);
        }
        
        public Provider getProvider() {
            return provider;
        }
        
        public String getType() {
            return type;
        }
        
        public String getAlgorithm() {
            return algorithm;
        }
        
        public String getClassName() {
            return className;
        }
        
        public List<String> getAliases() {
            return aliases;
        }
        
        public Map<String, String> getAttributes() {
            return attributes;
        }

        protected SpiFactory<?> getSpiFactory() {
            return factory;
        }
    }
          
    private final Map<String, Service> services = new HashMap<String, Service>();
    
    private final String name;
    
    private final double version;
    
    private final String info;
    
    protected Provider(String name, double version, String info) {
        this.name = name;
        this.version = version;
        this.info = info;
    }

    public Object getName() {
        return name;
    }

    public double getVersion() {
        return version;
    }

    public String getInfo() {
        return info;
    }
    
    public Provider.Service getService(String type, String algorithm) {
        return services.get(makeKey(algorithm, type));
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getSpi(String algorithm, Class<T> type) throws NoSuchAlgorithmException {
        Service service = services.get(makeKey(algorithm, asType(type)));
        if (service == null) return null;
        SpiFactory<T> factory = (SpiFactory<T>) service.getSpiFactory(); 
        if (factory == null) return null;
        return factory.create(null);
    }
    
    private String asType(Class<?> spiClass) {
        String spiName = spiClass.getName();
        assert spiName.contains(".");
        assert spiName.endsWith("Spi");
        return spiName.substring(spiName.lastIndexOf('.') + 1, spiName.length() - 3);
    }
    
    public Set<Provider.Service> getServices() {
        return new HashSet<Provider.Service>(services.values());
    }
    
    protected void putService(Provider.Service service) {
        services.put(makeKey(service.getAlgorithm(), service.getType()), service);
    }
    
    private <T> String makeKey(String algorithm, String type) {
        return type + "." + algorithm;
    }
    

}
