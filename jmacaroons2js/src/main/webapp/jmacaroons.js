var m;

$("#btnCreate").click(function (event) {
  var location = $("#txtLocation").val();
  var identifier = $("#txtIdentifier").val();
  var secret = $("#txtSecret").val();
  m = com.github.nitram509.jmacaroons.MacaroonsBuilder.create(location, secret, identifier);
  $('#txtInspect').val(m.inspect());
});

$("#btnSerialize").click(function (event) {
  if (m) {
    $('#txtSerialized').val(m.serialize());
  }
});

$("#btnDeSerialize").click(function (event) {
  var serialized = $('#txtSerialized').val();
  m = com.github.nitram509.jmacaroons.MacaroonsBuilder.deserialize(serialized);
  $('#txtInspect').val(m.inspect());
});

$("#btnAddCaveat").click(function (event) {
  if (m) {
    var caveat = $("#txtCaveat").val();
    var secret = $("#txtSecret").val();
    var mb = com.github.nitram509.jmacaroons.MacaroonsBuilder.modify(m, secret);
    mb.add_first_party_caveat(caveat);
    m = mb.getMacaroon();
    $('#txtInspect').val(m.inspect());
  }
});

$("#btnVerify").click(function (event) {
  if (m) {
    var secret = $("#txtSecret").val();
    var v = new com.github.nitram509.jmacaroons.MacaroonsVerifier(m);
    alert("Valid = " + v.isValid(secret));
  }
});
