var m;

$("#btnCreate").click(function (event) {
  var location = $("#txtLocation").val();
  var identifier = $("#txtIdentifier").val();
  var secret = $("#txtSecret").val();
  m = com.github.nitram509.jmacaroons.MacaroonsBuilder.create(location, secret, identifier);
  $('#txtSerialized').val(m.inspect());
});

$("#btnAddCaveat").click(function (event) {
  if (m) {
    var caveat = $("#txtCaveat").val();
    var secret = $("#txtSecret").val();
    var mb = com.github.nitram509.jmacaroons.MacaroonsBuilder.modify(m, secret);
    mb.add_first_party_caveat(caveat);
    m = mb.getMacaroon();
    $('#txtSerialized').val(m.inspect());
  }
});

$("#btnVerify").click(function (event) {
  if (m) {
    var secret = $("#txtSecret").val();
    var v = new com.github.nitram509.jmacaroons.MacaroonsVerifier(m);
    alert("Valid = " + v.isValid(secret));
  }
});
