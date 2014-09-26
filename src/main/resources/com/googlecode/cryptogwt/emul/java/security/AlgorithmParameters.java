package java.security;

import java.security.spec.AlgorithmParameterSpec;

/**
 * This class is used as an opaque representation of cryptographic parameters.
 * 
 * <p>
 * An AlgorithmParameters object for managing the parameters for a particular
 * algorithm can be obtained by calling one of the getInstance factory methods
 * (static methods that return instances of a given class).
 * 
 * <p>
 * There are two ways to request such an implementation: by specifying either
 * just an algorithm name, or both an algorithm name and a package provider.
 * 
 * <p>
 * If just an algorithm name is specified, the system will determine if there is
 * an AlgorithmParameters implementation for the algorithm requested available
 * in the environment, and if there is more than one, if there is a preferred
 * one. If both an algorithm name and a package provider are specified, the
 * system will determine if there is an implementation in the package requested,
 * and throw an exception if there is not. Once an AlgorithmParameters object is
 * returned, it must be initialized via a call to init, using an appropriate
 * parameter specification or parameter encoding.
 * 
 * <p>
 * A transparent parameter specification is obtained from an AlgorithmParameters
 * object via a call to getParameterSpec, and a byte encoding of the parameters
 * is obtained via a call to getEncoded.
 * 
 */
public class AlgorithmParameters {
    
//    public final AlgorithmParameterSpec getParameterSpec(Class paramSpec) {
//    }

}
