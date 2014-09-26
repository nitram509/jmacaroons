var m;

$("#btnCreate").click(function (event) {
  var location = $("#txtLocation").val();
  var identifier = $("#txtIdentifier").val();
  var secret = $("#txtSecret").val();
  m = MacaroonsBuilder.create(location, secret, identifier);
  $('#txtSerialized').text(m.inspect());
  return false;
});

$("#btnAddCaveat").click(function (event) {
  var caveat = $("#txtCaveat").val();
  var secret = $("#txtSecret").val();
  var mb = MacaroonsBuilder.modify(m, secret);
  mb.add_first_party_caveat(caveat);
  m = mb.getMacaroon();
  $('#txtSerialized').text(m.inspect());
  return false;
});
