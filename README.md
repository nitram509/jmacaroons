Macaroons are Better Than Cookies!
==================================

This Java library provides an implementation of macaroons[1], which are flexible
authorization tokens that work great in distributed systems.  Like cookies,
macaroons are bearer tokens that enable applications to ascertain whether their
holders' actions are authorized.  But macaroons are better than cookies!


Creating Your First Macaroon
----------------------------------

Lets create a simple macaroon:
````java
String location = "http://www.example.org";
String secretKey = "this is our super secret key; only we should know it";
String identifier = "we used our secret key";
Macaroon macaroon = MacaroonsBuilder.create(location, secretKey, identifier);
System.out.println(macaroon.inspect());
````

````
location http://www.example.org
identifier we used our secret key
signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f
````


Serializing
----------------------------------

````java
String serialized = macaroon.serialize();
System.out.println("Serialized: " + serialized);
````

````
Serialized: MDAyNGxvY2F0aW9uIGh0dHA6Ly93d3cuZXhhbXBsZS5vcmcKMDAyNmlkZW50aWZpZXIgd2UgdXNlZCBvdXIgc2VjcmV0IGtleQowMDJmc2lnbmF0dXJlIOPZ4CkIUmxMADmuFRFBFdl/3Wi/K6N5s0Kq8PYX0FUvCg==
````


Verifying Your Macaroon
----------------------------------

````java
MacaroonsVerifier verifier = new MacaroonsVerifier();
String secret = "this is our super secret key; only we should know it";
boolean valid = verifier.verify(macaroon, secret);
System.out.println("Macaroon is " + (valid ? "Valid" : "Invalid"));
````


Build Status
--------------------

[![Build Status](https://travis-ci.org/nitram509/jmacaroons.svg?branch=master)](https://travis-ci.org/nitram509/jmacaroons)


[1] http://research.google.com/pubs/pub41892.html