(function () {
  "use strict";

  var m;

  $("#btnCreate").click(function (event) {
    var location = $("#txtLocation").val();
    var identifier = $("#txtIdentifier").val();
    var secret = $("#txtSecret").val();
    m = com.github.nitram509.jmacaroons.MacaroonsBuilder.create(location, secret, identifier);
    $('#txtDetails').text(m.inspect());
    $('#txtSerialized').text(m.serialize());
    $('#copy-serialized').attr('data-clipboard-text', m.serialize()).show("fast");
  });

  $("#btnDeSerialize").click(function (event) {
    var serialized = $('#txtSerialized').val();
    m = com.github.nitram509.jmacaroons.MacaroonsBuilder.deserialize(serialized);
    $('#txtInspect').text(m.inspect());
  });

  $("#btnAddCaveat").click(function (event) {
    if (m) {
      var caveat = $("#txtCaveat").val();
      var secret = $("#txtSecret").val();
      var mb = com.github.nitram509.jmacaroons.MacaroonsBuilder.modify(m, secret);
      mb.add_first_party_caveat(caveat);
      m = mb.getMacaroon();
      $('#txtInspect').text(m.inspect());
    }
  });

  $("#btnVerify").click(function (event) {
    if (m) {
      var secret = $("#txtSecret").val();
      var v = new com.github.nitram509.jmacaroons.MacaroonsVerifier(m);
      alert("Valid = " + v.isValid(secret));
    }
  });

  var serializedClipboard = new ZeroClipboard(document.getElementById("copy-serialized"));
  serializedClipboard.on("ready", function (readyEvent) {
    /* nothing to do */
  });

  function enableCreateButton(event) {
    var location = $("#txtLocation").val() || "";
    var identifier = $("#txtIdentifier").val() || "";
    var secret = $("#txtSecret").val() || "";
    if (location.length > 0 && identifier.length > 0 && secret.length > 0) {
      $('#btnCreate').removeAttr('disabled');
      $('#btnCreate').attr('title', 'Build a macaroon');
    } else {
      $('#btnCreate').attr('disabled', 'disabled');
      $('#btnCreate').attr('title', 'Please, fill all data fields first.');
    }
  }

  $("#txtLocation").on('keypress', enableCreateButton).on('change', enableCreateButton);
  $("#txtIdentifier").on('keypress', enableCreateButton).on('change', enableCreateButton);
  $("#txtSecret").on('keypress', enableCreateButton).on('change', enableCreateButton);

})();