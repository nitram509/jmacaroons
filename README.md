Macaroons are Better Than Cookies!
==================================

This Java library provides an implementation of macaroons[[1]](http://research.google.com/pubs/pub41892.html),
which are flexible authorization tokens that work great in distributed systems.
Like cookies, macaroons are bearer tokens that enable applications to ascertain whether their
holders' actions are authorized.  But macaroons are better than cookies!

This project started as a port of libmacaroons[[2]](https://github.com/rescrv/libmacaroons) library.
The primary goals are
   * being compatible to libmacaroons
   * having no external dependencies, except the Java Runtime v7
   * being the reference implementation in the Java community ;-)

Creating Your First Macaroon
----------------------------------

Lets create a simple macaroon
````java
String location = "http://www.example.org";
String secretKey = "this is our super secret key; only we should know it";
String identifier = "we used our secret key";
Macaroon macaroon = MacaroonsBuilder.create(location, secretKey, identifier);
````

Of course, this macaroon can be displayed in a more human-readable form
for easy debugging
````
System.out.println(macaroon.inspect());

> location http://www.example.org
> identifier we used our secret key
> signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f
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


Adding Caveats
-----------------------------------

When creating a new macaroon, you can add a caveat to our macaroon that
restricts it to just the account number 3735928559.
````java
String location = "http://www.example.org";
String secretKey = "this is our super secret key; only we should know it";
String identifier = "we used our secret key";
Macaroon macaroon = new MacaroonsBuilder(location, secretKey, identifier)
    .add_first_party_caveat("account = 3735928559")
    .getMacaroon();
````

Because macaroon objects are immutable, they have to be modified
via MacaroonsBuilder. Thus, a new macaroon object will be created.
````java
String secretKey = "this is our super secret key; only we should know it";
macaroon = MacaroonsBuilder.modify(macaroon, secretKey)
    .add_first_party_caveat("account = 3735928559")
    .getMacaroon();
System.out.println(macaroon.inspect());
````

````
location http://www.example.org
identifier we used our secret key
cid account = 3735928559
signature 1efe4763f290dbce0c1d08477367e11f4eee456a64933cf662d79772dbb82128
````


Build Status
--------------------

[![Build Status](https://travis-ci.org/nitram509/jmacaroons.svg?branch=master)](https://travis-ci.org/nitram509/jmacaroons)

