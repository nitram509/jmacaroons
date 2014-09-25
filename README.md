Macaroons are Better Than Cookies!
==================================

This Java library provides an implementation of macaroons[[1]](http://research.google.com/pubs/pub41892.html),
which are flexible authorization tokens that work great in distributed systems.
Like cookies, macaroons are bearer tokens that enable applications to ascertain whether their
holders' actions are authorized.  But macaroons are better than cookies!

This project started as a port of libmacaroons[[2]](https://github.com/rescrv/libmacaroons) library.
The primary goals are
   * being compatible to libmacaroons
   * having no external dependencies, except the Java Runtime
   * being Android compatible, while using Java6
   * being the reference implementation in the Java community ;-)

##### License

[![License](https://img.shields.io/:license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Usage/Import In Your Project
----------------------------------

This library jmacaroons is available via Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/com.github.nitram509/jmacaroons.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.nitram509%22%20AND%20a%3A%22jmacaroons%22)


Maven
````xml
<dependency>
  <groupId>com.github.nitram509</groupId>
  <artifactId>jmacaroons</artifactId>
  <version>0.1.7</version>
</dependency>
````

Gradle
````groovy
compile 'com.github.nitram509:jmacaroons:0.1.7'
````


Build Status
--------------------

[![Build Status](https://travis-ci.org/nitram509/jmacaroons.svg?branch=master)](https://travis-ci.org/nitram509/jmacaroons)

[![Coverage Status](https://coveralls.io/repos/nitram509/jmacaroons/badge.png?branch=master)](https://coveralls.io/r/nitram509/jmacaroons?branch=master)


Community & Badges
--------------------

Listed on Android Arsenal: [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-jmacaroons-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/914)

If you like this project, endorse please: [![endorse](https://api.coderwall.com/nitram509/endorsecount.png)](https://coderwall.com/nitram509)

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
````java
System.out.println(macaroon.inspect());

// > location http://www.example.org
// > identifier we used our secret key
// > signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f
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


De-Serializing
----------------------------------

````java
Macaroon macaroon = MacaroonsBuilder.deserialize(serialized);
System.out.println(macaroon.inspect());

// > location http://www.example.org
// > identifier we used our secret key
// > signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f
````


Verifying Your Macaroon
----------------------------------

A verifier can only ever successfully verify a macaroon
when provided with the macaroon and its corresponding secret - no secret, no authorization.
````java
MacaroonsVerifier verifier = new MacaroonsVerifier(macaroon);
String secret = "this is our super secret key; only we should know it";
boolean valid = verifier.isValid(secret);

// > True
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
Macaroon macaroon = MacaroonsBuilder.modify(macaroon)
    .add_first_party_caveat("account = 3735928559")
    .getMacaroon();
System.out.println(macaroon.inspect());

// > location http://www.example.org
// > identifier we used our secret key
// > cid account = 3735928559
// > signature 1efe4763f290dbce0c1d08477367e11f4eee456a64933cf662d79772dbb82128
````

Verifying Macaroons With Caveats
--------------------------------

The verifier should say that this macaroon is unauthorized because
the verifier cannot prove that the caveat (account = 3735928559) is satisfied.
We can see that it fails just as we would expect.
````java
String location = "http://www.example.org";
String secretKey = "this is our super secret key; only we should know it";
String identifier = "we used our secret key";
Macaroon macaroon = new MacaroonsBuilder(location, secretKey, identifier)
    .add_first_party_caveat("account = 3735928559")
    .getMacaroon();
MacaroonsVerifier verifier = new MacaroonsVerifier(macaroon);
verifier.isValid(secretKey);
// > False
````

Caveats like these are called "exact caveats" because there is exactly one way
to satisfy them.  Either the account number is 3735928559, or it isn't.  At
verification time, the verifier will check each caveat in the macaroon against
the list of satisfied caveats provided to "satisfyExcact()".  When it finds a
match, it knows that the caveat holds and it can move onto the next caveat in
the macaroon.
````java
verifier.satisfyExcact("account = 3735928559");
verifier.isValid(secretKey);
// > True
````

The verifier can be made more general, and be "future-proofed",
so that it will still function correctly even if somehow the authorization
policy changes; for example, by adding the three following facts,
the verifier will continue to work even if someone decides to
self-attenuate itself macaroons to be only usable from IP address and browser:
````java
verifier.satisfyExcact("IP = 127.0.0.1')");
verifier.satisfyExcact("browser = Chrome')");
verifier.isValid(secretKey);
// > True
````

There is also a more general way to check caveats, via callbacks.
When providing such a callback to the verifier,
it is able to check if the caveat satisfies special constrains. 
````java
Macaroon macaroon = new MacaroonsBuilder(location, secretKey, identifier)
    .add_first_party_caveat("time < 2042-01-01T00:00")
    .getMacaroon();
MacaroonsVerifier verifier = new MacaroonsVerifier(macaroon);
verifier.isValid(secretKey);
// > False

verifier.satisfyGeneral(new TimestampCaveatVerifier());
verifier.isValid(secretKey);
// > True
````
    

Third Party Caveats
---------------------

Work in progress ...
see git branch [3rd-party-caveats](https://github.com/nitram509/jmacaroons/tree/3rd-party-caveats)
