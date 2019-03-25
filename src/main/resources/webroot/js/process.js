function createInput(name, value) {
  var input = document.createElement("input");
  input.setAttribute("type", "hidden");
  input.setAttribute('name', name);
  input.setAttribute('value', value);
  return input;
}

function processPayment() {

  $.post("/pay", {
    cc_number: $('#cc-number').val(),
    cc_exp_month : $('#cc-exp-month').val(),
    cc_exp_year : $('#cc-exp-year').val(),
    cc_cvc : $('#cc-cvc').val(),
    currency : $('#currency').val(),
    amount : $('#amount').val()
  }, function(d, status, jq) {
    console.log(d);

    var token = d.token;
    var currency = d.currency
    if (d['3dsecure'].enrolled) {

      var content = document.createElement('div');

      var secure3dForm = document.createElement('form');
      secure3dForm.setAttribute("method", "POST");
      secure3dForm.setAttribute("action", d['3dsecure'].acsUrl);
      secure3dForm.setAttribute("target", "secure3d-frame");

      var merchantDetails = d['3dsecure'].md;
      var parReq = d['3dsecure'].paReq;
      var termUrl = d['3dsecure'].termUrl;

      secure3dForm.append(createInput('PaReq', parReq));
      secure3dForm.append(createInput('TermUrl', termUrl));
      secure3dForm.append(createInput('MD', merchantDetails));
      content.appendChild(secure3dForm);

      var iframeNode = $('#secure3d-frame');
      $(content).insertAfter(iframeNode);
      iframeNode.show();

      var processPaymentToken = function(data) {
        console.log("Processing token...");
        window.removeEventListener('message', processPaymentToken);
        var parsedJson = JSON.parse(data.data);
        if (parsedJson.secure3d.authenticated) {
          $.post("/complete", {
            amount: 1500,
            currency: currency,
            description: 'description',
            token: token
          }, function(dd, sstatus, jjqq) {
            console.log(dd);

            if (dd.success) {
              $('#simplify-payment-form').hide();
              $('#simplify-success').show();
            }
            iframeNode.hide();
          });
        }
      };

      $('#secure3d-frame').on('load', function() {
        window.addEventListener('message', processPaymentToken);
      });

      secure3dForm.submit();
    }

  });

}

$(document).ready(function() {
  $("#simplify-payment-form").on("submit", function() {
    processPayment();
    return false;
  });
});
