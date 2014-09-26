package com.github.nitram509.jmacaroons_js;

import com.google.gwt.core.client.EntryPoint;

public class MacaroonExporterJs implements EntryPoint {

  private static final int REFRESH_INTERVAL = 5000; // ms

  /**
   * Entry point method.
   */
  public void onModuleLoad() {
    registerMethods();
    registerVerifier();
  }

  public native void registerMethods() /*-{

    function enhanceMacaroonInterface(m) {
      m.serialize = m.@com.github.nitram509.jmacaroons.Macaroon::serialize();
      m.inspect = m.@com.github.nitram509.jmacaroons.Macaroon::inspect();
      m.indentifier = m.@com.github.nitram509.jmacaroons.Macaroon::identifier;
      m.location = m.@com.github.nitram509.jmacaroons.Macaroon::location;
      m.signature = m.@com.github.nitram509.jmacaroons.Macaroon::signature;
      return m;
    }

    $wnd.MacaroonsBuilder = {
      create: function (location, secret, identifier) {
        var m = @com.github.nitram509.jmacaroons.MacaroonsBuilder::create(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(location, secret, identifier);
        return enhanceMacaroonInterface(m);
      },
      modify: function (macaroon, secret) {
        var mb = @com.github.nitram509.jmacaroons.MacaroonsBuilder::modify(Lcom/github/nitram509/jmacaroons/Macaroon;Ljava/lang/String;)(macaroon, secret);
        mb.add_first_party_caveat = mb.@com.github.nitram509.jmacaroons.MacaroonsBuilder::add_first_party_caveat(Ljava/lang/String;);
        mb.getMacaroon = function () {
          var m = mb.@com.github.nitram509.jmacaroons.MacaroonsBuilder::getMacaroon()();
          return enhanceMacaroonInterface(m);
        };
        return mb;
      }
    };

  }-*/;

  public native void registerVerifier() /*-{

    $wnd.MacaroonsVerifier = function (macaroon) {
      var v = new @com.github.nitram509.jmacaroons.MacaroonsVerifier::new(Lcom/github/nitram509/jmacaroons/Macaroon;)(macaroon);
      return {
        isValid: function (secret) {
          return v.@com.github.nitram509.jmacaroons.MacaroonsVerifier::isValid(Ljava/lang/String;)(secret);
        }
      }
    }
  }-*/;

}