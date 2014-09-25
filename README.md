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
  <version>0.2.0</version>
</dependency>
````

Gradle
````groovy
compile 'com.github.nitram509:jmacaroons:0.2.0'
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

Like first-party caveats, third-party caveats restrict the context in which a
macaroon is authorized, but with a different form of restriction.  Where a
first-party caveat is checked directly within the verifier, a third-party caveat
is checked by the third-party, who provides a discharge macaroon to prove that
the original third-party caveat is true.  The discharge macaroon is recursively
inspected by the verifier; if it verifies successfully, the discharge macaroon
serves as a proof that the original third-party caveat is satisfied.  Of course,
nothing stops discharge macaroons from containing embedded first- or third-party
caveats for the verifier to consider during verification.

Let's rework the above example to provide Alice with access to her account only
after she authenticates with a service that is separate from the service
processing her banking transactions.

As before, we'll start by constructing a new macaroon with the caveat that is
limited to Alice's bank account.

````java
// create a simple macaroon first
String location = "http://mybank/";
String secret = "this is a different super-secret key; never use the same secret twice";
String publicIdentifier = "we used our other secret key";
MacaroonsBuilder mb = new MacaroonsBuilder(location, secret, publicIdentifier)
    .add_first_party_caveat("account = 3735928559");

// add a 3rd party caveat
// you'll likely want to use a higher entropy source to generate this key
String caveat_key = "4; guaranteed random by a fair toss of the dice";
String predicate = "user = Alice";
// send_to_3rd_party_location_and_do_auth(caveat_key, predicate);
// identifier = recv_from_auth();
String identifier = "this was how we remind auth of key/pred";
Macaroon m = mb.add_third_party_caveat("http://auth.mybank/", caveat_key, identifier)
    .getMacaroon();
    
m.inspect();
// > location http://mybank/
// > identifier we used our other secret key
// > cid account = 3735928559
// > cid this was how we remind auth of key/pred
// > vid AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA027FAuBYhtHwJ58FX6UlVNFtFsGxQHS7uD/w/dedwv4Jjw7UorCREw5rXbRqIKhr
// > cl http://auth.mybank/
// > signature 6b99edb2ec6d7a4382071d7d41a0bf7dfa27d87d2f9fea86e330d7850ffda2b2
````

In a real application, we'd look at these third party caveats, and contact each
location to retrieve the requisite discharge macaroons.  We would include the
identifier for the caveat in the request itself, so that the server can recall
the secret used to create the third-party caveat.  The server can then generate
and return a new macaroon that discharges the caveat:

````java
Macaroon d = new MacaroonsBuilder("http://auth.mybank/", caveat_key, identifier)
    .add_first_party_caveat("time < 2015-01-01T00:00")
    .getMacaroon();
````

This new macaroon enables the verifier to determine that the third party caveat
is satisfied.  Our target service added a time-limiting caveat to this macaroon
that ensures that this discharge macaroon does not last forever.  This ensures
that Alice (or, at least someone authenticated as Alice) cannot use the
discharge macaroon indefinitely and will eventually have to re-authenticate.

Once Alice has both the root macaroon and the discharge macaroon in her
possession, she can make the request to the target service.  Making a request
with discharge macaroons is only slightly more complicated than making requests
with a single macaroon.  In addition to serializing and transmitting all
involved macaroons, there is preparation step that binds the discharge macaroons
to the root macaroon.  This binding step ensures that the discharge macaroon is
useful only when presented alongside the root macaroon.  The root macaroon is
used to bind the discharge macaroons like this:

````java
Macaroon dp = MacaroonsBuilder.modify(m)
    .prepare_for_request(d)
    .getMacaroon();
````

If we were to look at the signatures on these prepared discharge macaroons, we
would see that the binding process has irreversibly altered their signature(s).

````java
// > d.signature = 82a80681f9f32d419af12f6a71787a1bac3ab199df934ed950ddf20c25ac8c65
// > dp.signature = b38b26ab29d3724e728427e758cccc16d9d7f3de46d0d811b70b117b05357b9b
````

The root macaroon 'm' and its discharge macaroons 'dp' are ready for the
request.  Alice can serialize them all and send them to the bank to prove she is
authorized to access her account.  The bank can verify them using the same
verifier we built before.  We provide the discharge macaroons as a third
argument to the verify call:

````java
new MacaroonsVerifier(m)
    .satisfyExcact("account = 3735928559")
    .satisfyGeneral(new TimestampCaveatVerifier())
    .satisfy3rdParty(dp)
    .assertIsValid(secret);
// > ok.
````

Without the 'prepare_for_request()' call, the verification would fail.

Interestingly, when you're verifying a macaroon without satisfy3rdParty(),
then the verification process will proceed and ignore them. 
 
````java
new MacaroonsVerifier(m)
    .satisfyExcact("account = 3735928559")
    .satisfyGeneral(new TimestampCaveatVerifier())
    /* don't verify 3rd party caveat - will be valid, too */
    .assertIsValid(secret);
// > ok.
````


Choosing Secrets
-------------------

For clarity, we've generated human-readable secrets that we use as the root keys
of all of our macaroons.  In practice, this is terribly insecure and can lead to
macaroons that can easily be forged because the secret is too predictable.  To
avoid this, we recommend generating secrets using a sufficient number of
suitably random bytes.  Because the bytes are a secret key, they should be drawn
from a source with enough entropy to ensure that the key cannot be guessed
before the macaroon falls out of use.

The jmacaroons library exposes a constant that is the ideal number of bytes these
secret keys should contain.  Any shorter is wasting an opportunity for security.

````java
com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_SUGGESTED_SECRET_LENGTH = 32
````
