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
   * being very much backward compatible, while using Java8
   * focus on binary serialization format (currently, JSON format isn't supported)
   * being the reference implementation in the Java community ;-)

There is a [playground](http://www.macaroons.io/) (testing environment) available,
where you can build and verify macaroons online.

##### License

[![License](https://img.shields.io/:license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Usage/Import In Your Project
----------------------------------

This library jmacaroons is available via Maven Central.
Requires Java 1.8+

[![Maven Central](https://img.shields.io/maven-central/v/com.github.nitram509/jmacaroons.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.nitram509%22%20AND%20a%3A%22jmacaroons%22)


Maven
````xml
<dependency>
  <groupId>com.github.nitram509</groupId>
  <artifactId>jmacaroons</artifactId>
  <version>0.4.2</version>
</dependency>
````

Gradle
````groovy
compile 'com.github.nitram509:jmacaroons:0.4.2'
````


Build Status
--------------------

[![maven test](https://github.com/nitram509/jmacaroons/actions/workflows/maven-test.yml/badge.svg)](https://github.com/nitram509/jmacaroons/actions/workflows/maven-test.yml)

[![codecov](https://codecov.io/gh/nitram509/jmacaroons/branch/master/graph/badge.svg?token=G4BT0ayhLV)](https://codecov.io/gh/nitram509/jmacaroons)

Community & Badges
--------------------

Listed on Android Arsenal: [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-jmacaroons-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/914)


Creating Your First Macaroon
----------------------------------

Lets create a simple macaroon
Of course, this macaroon can be displayed in a more human-readable form
for easy debugging
<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=37-47) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
  Macaroon create() {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";
    Macaroon macaroon = Macaroon.create(location, secretKey, identifier);
    System.out.println(macaroon.inspect());
    // > location http://www.example.org
    // > identifier we used our secret key
    // > signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f
    return macaroon;
  }
```
<!-- MARKDOWN-AUTO-DOCS:END -->


Serializing
----------------------------------

Macaroons are serialized, using Base64 URL safe encoding [RFC 4648](http://www.ietf.org/rfc/rfc4648.txt).
This way you can very easily append it to query string within URIs.

<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=49-54) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
  void serialize() {
    Macaroon macaroon = create();
    String serialized = macaroon.serialize();
    System.out.println("Serialized: " + serialized);
    // Serialized: MDAyNGxvY2F0aW9uIGh0dHA6Ly93d3cuZXhhbXBsZS5vcmcKMDAyNmlkZW50aWZpZXIgd2UgdXNlZCBvdXIgc2VjcmV0IGtleQowMDJmc2lnbmF0dXJlIOPZ4CkIUmxMADmuFRFBFdl_3Wi_K6N5s0Kq8PYX0FUvCg
  }
```
<!-- MARKDOWN-AUTO-DOCS:END -->

Alternatively, the V2 binary serializer format is supported.
<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=216-221) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
  void serialize_v2_binary_format() {
    Macaroon macaroon = create();
    String serialized = macaroon.serialize(V2);
    System.out.println("Serialized: " + serialized);
    // Serialized: AgEWaHR0cDovL3d3dy5leGFtcGxlLm9yZwIWd2UgdXNlZCBvdXIgc2VjcmV0IGtleQAABiDj2eApCFJsTAA5rhURQRXZf91ovyujebNCqvD2F9BVLw
  }
```
<!-- MARKDOWN-AUTO-DOCS:END -->


_Note:_
Base64 URL safe is supported since v0.3.0. jmacaroons also de-serializes regular Base64 to maintain backward compatibility.


De-Serializing
----------------------------------

<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=56-63) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
  void deserialize() {
    String serialized = create().serialize();
    Macaroon macaroon = Macaroon.deserialize(serialized);
    System.out.println(macaroon.inspect());
    // > location http://www.example.org
    // > identifier we used our secret key
    // > signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f
  }
```
<!-- MARKDOWN-AUTO-DOCS:END -->


Verifying Your Macaroon
----------------------------------

A verifier can only ever successfully verify a macaroon
when provided with the macaroon and its corresponding secret - no secret, no authorization.
<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=65-71) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
  void verify() throws InvalidKeyException, NoSuchAlgorithmException {
    Macaroon macaroon = create();
    MacaroonsVerifier verifier = new MacaroonsVerifier(macaroon);
    String secret = "this is our super secret key; only we should know it";
    boolean valid = verifier.isValid(secret);
    // > True
  }
```
<!-- MARKDOWN-AUTO-DOCS:END -->


Adding Caveats
-----------------------------------

When creating a new macaroon, you can add a caveat to our macaroon that
restricts it to just the account number 3735928559.
<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=73-81) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
  void addCaveat() throws InvalidKeyException, NoSuchAlgorithmException {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";
    Macaroon macaroon = Macaroon.builder(location, secretKey, identifier)
        .addCaveat("account = 3735928559")
        .build();
    System.out.println(macaroon.inspect());
  }
```
<!-- MARKDOWN-AUTO-DOCS:END -->

Because macaroon objects are immutable, they have to be modified
via Macaroon Builder. Thus, a new macaroon object will be created.
<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=83-93) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
  void addCaveat_modify() throws InvalidKeyException, NoSuchAlgorithmException {
    Macaroon macaroon = create();
    macaroon = Macaroon.builder(macaroon)
        .addCaveat("account = 3735928559")
        .build();
    System.out.println(macaroon.inspect());
    // > location http://www.example.org
    // > identifier we used our secret key
    // > cid account = 3735928559
    // > signature 1efe4763f290dbce0c1d08477367e11f4eee456a64933cf662d79772dbb82128
  }
```
<!-- MARKDOWN-AUTO-DOCS:END -->


Verifying Macaroons With Caveats
--------------------------------

The verifier should say that this macaroon is unauthorized because
the verifier cannot prove that the caveat (account = 3735928559) is satisfied.
We can see that it fails just as we would expect.
<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=95-104) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
  void verify_required_caveats() throws InvalidKeyException, NoSuchAlgorithmException {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";
    Macaroon macaroon = Macaroon.builder(location, secretKey, identifier)
        .addCaveat("account = 3735928559")
        .build();
    MacaroonsVerifier verifier = new MacaroonsVerifier(macaroon);
    verifier.isValid(secretKey);
    // > False
```
<!-- MARKDOWN-AUTO-DOCS:END -->

Caveats like these are called "exact caveats" because there is exactly one way
to satisfy them.  Either the account number is 3735928559, or it isn't.  At
verification time, the verifier will check each caveat in the macaroon against
the list of satisfied caveats provided to "satisfyExact()".  When it finds a
match, it knows that the caveat holds and it can move onto the next caveat in
the macaroon.
<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=106-108) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
    verifier.satisfyExact("account = 3735928559");
    verifier.isValid(secretKey);
    // > True
```
<!-- MARKDOWN-AUTO-DOCS:END -->

The verifier can be made more general, and be "future-proofed",
so that it will still function correctly even if somehow the authorization
policy changes; for example, by adding the three following facts,
the verifier will continue to work even if someone decides to
self-attenuate itself macaroons to be only usable from IP address and browser:
<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=110-115) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
    verifier.satisfyExact("IP = 127.0.0.1')");
    verifier.satisfyExact("browser = Chrome')");
    verifier.satisfyExact("action = deposit");
    verifier.isValid(secretKey);
    // > True
  }
```
<!-- MARKDOWN-AUTO-DOCS:END -->

There is also a more general way to check caveats, via callbacks.
When providing such a callback to the verifier,
it is able to check if the caveat satisfies special constrains. 
<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=117-132) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
  void verify_general_caveats() throws InvalidKeyException, NoSuchAlgorithmException {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";

    Macaroon macaroon = Macaroon.builder(location, secretKey, identifier)
        .addCaveat("time < 2042-01-01T00:00")
        .build();
    MacaroonsVerifier verifier = new MacaroonsVerifier(macaroon);
    verifier.isValid(secretKey);
    // > False

    verifier.satisfyGeneral(new TimestampCaveatVerifier());
    verifier.isValid(secretKey);
    // > True
  }
```
<!-- MARKDOWN-AUTO-DOCS:END -->
    

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

<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=134-159) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
  void with_3rd_party_caveats() {
    // create a simple macaroon first
    String location = "http://mybank/";
    String secret = "this is a different super-secret key; never use the same secret twice";
    String publicIdentifier = "we used our other secret key";
    MacaroonsBuilder mb = Macaroon.builder(location, secret, publicIdentifier)
        .addCaveat("account = 3735928559");

    // add a 3rd party caveat
    // you'll likely want to use a higher entropy source to generate this key
    String caveat_key = "4; guaranteed random by a fair toss of the dice";
    String predicate = "user = Alice";
    // send_to_3rd_party_location_and_do_auth(caveat_key, predicate);
    // identifier = recv_from_auth();
    String identifier = "this was how we remind auth of key/pred";
    Macaroon m = mb.addCaveat("http://auth.mybank/", caveat_key, identifier)
        .build();

    System.out.println(m.inspect());
    // > location http://mybank/
    // > identifier we used our other secret key
    // > cid account = 3735928559
    // > cid this was how we remind auth of key/pred
    // > vid AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA027FAuBYhtHwJ58FX6UlVNFtFsGxQHS7uD_w_dedwv4Jjw7UorCREw5rXbRqIKhr
    // > cl http://auth.mybank/
    // > signature d27db2fd1f22760e4c3dae8137e2d8fc1df6c0741c18aed4b97256bf78d1f55c
```
<!-- MARKDOWN-AUTO-DOCS:END -->

In a real application, we'd look at these third party caveats, and contact each
location to retrieve the requisite discharge macaroons. We would include the
identifier for the caveat in the request itself, so that the server can recall
the secret used to create the third-party caveat. The server can then generate
and return a new macaroon that discharges the caveat:

<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=161-167) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
    final String oneHourFromNow = Instant.now()
        .plus(Duration.ofHours(1))
        .toString();

    Macaroon d = Macaroon.builder("http://auth.mybank/", caveat_key, identifier)
        .addCaveat("time < " + oneHourFromNow)
        .build();
```
<!-- MARKDOWN-AUTO-DOCS:END -->

This new macaroon enables the verifier to determine that the third party caveat
is satisfied. Our target service added a time-limiting caveat to this macaroon
that ensures that this discharge macaroon does not last forever.  This ensures
that Alice (or, at least someone authenticated as Alice) cannot use the
discharge macaroon indefinitely and will eventually have to re-authenticate.

Once Alice has both the root macaroon and the discharge macaroon in her
possession, she can make the request to the target service. Making a request
with discharge macaroons is only slightly more complicated than making requests
with a single macaroon. In addition to serializing and transmitting all
involved macaroons, there is preparation step that binds the discharge macaroons
to the root macaroon. This binding step ensures that the discharge macaroon is
useful only when presented alongside the root macaroon. The root macaroon is
used to bind the discharge macaroons like this:

<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=169-171) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
    Macaroon dp = Macaroon.builder(m)
        .prepareForRequest(d)
        .build();
```
<!-- MARKDOWN-AUTO-DOCS:END -->

If we were to look at the signatures on these prepared discharge macaroons, we
would see that the binding process has irreversibly altered their signature(s).

<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=173-176) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
    System.out.println("d.signature = " + d.signature);
    System.out.println("dp.signature = " + dp.signature);
    // > d.signature = 82a80681f9f32d419af12f6a71787a1bac3ab199df934ed950ddf20c25ac8c65
    // > dp.signature = 2eb01d0dd2b4475330739140188648cf25dda0425ea9f661f1574ca0a9eac54e
```
<!-- MARKDOWN-AUTO-DOCS:END -->

The root macaroon 'm' and its discharge macaroons 'dp' are ready for the
request.  Alice can serialize them all and send them to the bank to prove she is
authorized to access her account. The bank can verify them using the same
verifier we built before.  We provide the discharge macaroons as a third
argument to the verify call:

<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=178-184) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
    new MacaroonsVerifier(m)
        .satisfyExact("account = 3735928559")
        .satisfyGeneral(new TimestampCaveatVerifier())
        .satisfy3rdParty(dp)
        .assertIsValid(secret);
    // > ok.
  }
```
<!-- MARKDOWN-AUTO-DOCS:END -->

Without the 'prepare_for_request()' call, the verification would fail.


Commonly used verifier, shipped with jmacaroons
--------------------------------------------------

##### Time to live verification

Applying a timestamp in the future to a macaroon will provide time to live semantics.
Given that all machines have synchronized clocks, a general macaroon verifier is able to check
for expiration.

<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=186-199) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
  void timestamp_verifier() {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";

    Macaroon macaroon = Macaroon.builder(location, secretKey, identifier)
        .addCaveat("time < 2015-01-01T00:00")
        .build();

    new MacaroonsVerifier(macaroon)
        .satisfyGeneral(new TimestampCaveatVerifier())
        .isValid(secretKey);
    // > True
  }
```
<!-- MARKDOWN-AUTO-DOCS:END -->

##### Authorities verification

Macaroons may also embed authorities. Thus a general macaroon verifier is able
to check for a single authority.

<!-- MARKDOWN-AUTO-DOCS:START (CODE:src=./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java&lines=201-214) -->
<!-- The below code snippet is automatically added from ./src/example/java/com/github/nitram509/jmacaroons/examples/MacaroonsExamples.java -->
```java
  void authorities_verifier() {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";

    Macaroon macaroon = Macaroon.builder(location, secretKey, identifier)
        .addCaveat("authorities = ROLE_USER, DEV_TOOLS_AVAILABLE")
        .build();

    new MacaroonsVerifier(macaroon)
        .satisfyGeneral(hasAuthority("DEV_TOOLS_AVAILABLE"))
        .isValid(secretKey);
    // > True
  }
```
<!-- MARKDOWN-AUTO-DOCS:END -->


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


Performance
--------------

There's a little micro benchmark, which demonstrates the performance of jmacaroons.

Source: https://gist.github.com/nitram509/b6f836a697b405e5f440

Environment: Windows 8.1 64bit, JRE 1.8.0_25 64bit, Intel i7-4790 @3.60GHz

````text
Results
----------
Benchmark                                                                    Mode  Samples        Score       Error  Units
o.s.JMacaroonsBenchmark.benchmark_Deserialize                               thrpt        5  2190474,677 ± 44591,197  ops/s
o.s.JMacaroonsBenchmark.benchmark_Deserialize_and_Verify_key_bytes          thrpt        5   457262,262 ±  5868,723  ops/s
o.s.JMacaroonsBenchmark.benchmark_Deserialize_and_Verify_key_string         thrpt        5   262689,398 ±  4270,857  ops/s
o.s.JMacaroonsBenchmark.benchmark_Serialize_with_key_bytes                  thrpt        5   424008,024 ± 16222,450  ops/s
o.s.JMacaroonsBenchmark.benchmark_Serialize_with_key_bytes_and_1_caveat     thrpt        5   242060,835 ±  5696,272  ops/s
o.s.JMacaroonsBenchmark.benchmark_Serialize_with_key_bytes_and_2_caveats    thrpt        5   166017,277 ±   870,467  ops/s
o.s.JMacaroonsBenchmark.benchmark_Serialize_with_key_bytes_and_3_caveats    thrpt        5   127712,773 ±   478,394  ops/s
o.s.JMacaroonsBenchmark.benchmark_Serialize_with_key_string                 thrpt        5   252302,839 ±  3277,232  ops/s
````


## Stargazers over time

[![Stargazers over time](https://starchart.cc/nitram509/jmacaroons.svg)](https://starchart.cc/nitram509/jmacaroons)

