Macaroons are Better Than Cookies!
==================================

This Java library provides an implementation of macaroons[1], which are flexible
authorization tokens that work great in distributed systems.  Like cookies,
macaroons are bearer tokens that enable applications to ascertain whether their
holders' actions are authorized.  But macaroons are better than cookies!

Creating Your First Macaroon
----------------------------------

Lets create a simple macaroon:
````
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



Build Status
--------------------

[![Build Status](https://travis-ci.org/nitram509/jmacaroons.svg?branch=master)](https://travis-ci.org/nitram509/jmacaroons)


[1] http://research.google.com/pubs/pub41892.html