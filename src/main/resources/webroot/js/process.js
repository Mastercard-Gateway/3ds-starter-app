function createInput(name, value) {
  var input = document.createElement('input');
  input.setAttribute('type', 'hidden');
  input.setAttribute('name', name);
  input.setAttribute('value', value);
  return input;
}

function createSecure3dForm(data) {
  var secure3dData = data['3dsecure'];
  var secure3dForm = document.createElement('form');
  secure3dForm.setAttribute('method', 'POST');
  secure3dForm.setAttribute('action', secure3dData.acsUrl);
  secure3dForm.setAttribute('target', 'secure3d-frame');

  var merchantDetails = secure3dData.md;
  var parReq = secure3dData.paReq;
  var termUrl = secure3dData.termUrl;

  secure3dForm.append(createInput('PaReq', parReq));
  secure3dForm.append(createInput('TermUrl', termUrl));
  secure3dForm.append(createInput('MD', merchantDetails));

  return secure3dForm;
}

function createSecure3dEmvForm(data) {
  var secure3dData = data['3dsecure'];
  return secure3dData.redirectHtml;
}

function processPayment() {
  var payload = {
    cc_number: $('#cc-number').val(),
    cc_exp_month: $('#cc-exp-month').val(),
    cc_exp_year: $('#cc-exp-year').val(),
    cc_cvc: $('#cc-cvc').val(),
    currency: $('#currency').val(),
    amount: $('#amount').val()
  };

  if ($('#emv3ds').is(":checked")) {

    $.post('/payEmvCreate', payload, function (createResponse) {

      var initiate3dsForm = createSecure3dEmvForm(createResponse);
      var iframe3dsNode = $('#methodFrame');

      $(initiate3dsForm).insertAfter(iframe3dsNode);
      iframe3dsNode.show();

      $('#initiate3dsForm').on('submit', function () {
      });

      iframe3dsNode.hide();

      var token = createResponse.token;
      var currency = createResponse.currency;
      var browser = navigator.userAgent;
      const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;

      var updatePayload = {
        currency: currency,
        token: token,
        browser: browser,
        timezone: timezone
      };

      $.post('/payEmvUpdate', updatePayload, function (response) {

        var token = response.token;
        var currency = response.currency;

        var secure3dForm = createSecure3dEmvForm(response);
        var iframeNode = $('#challengeFrame');

        $(secure3dForm).insertAfter(iframeNode);
        iframeNode.show();

        var process3dSecureCallback = function (threedsResponse) {
          console.log('Processing EMV 3D Secure callback...');
          window.removeEventListener('message', process3dSecureCallback);

          var simplifyDomain = 'https://simplify.com'; // For UAT you can use 'https://uat.simplify.com'

          if (threedsResponse.origin === simplifyDomain && JSON.parse(threedsResponse.data)['secure3d']['authenticated']) {  // JSON.parse(data.data)['secure3d.authenticated']) {
            var completePayload = {
              amount: 1500,
              currency: currency,
              description: 'description',
              token: token
            };

            $.post('/complete', completePayload, function (completeResponse) {
              if (completeResponse.success) {
                $('#simplify-payment-form').hide();
                $('#simplify-success').show();
              }
              iframeNode.hide();
            });
          }
        };

        iframeNode.on('load', function () {
          window.addEventListener('message', process3dSecureCallback);
        });

        secure3dForm.submit();

      });
    });
  } else {

    $.post('/pay', payload, function (response) {
      var token = response.token;
      var currency = response.currency;

      if (response['3dsecure'].enrolled) {
        var secure3dForm = createSecure3dForm(response);
        var iframeNode = $('#secure3d-frame');

        $(secure3dForm).insertAfter(iframeNode);
        iframeNode.show();

        var process3dSecureCallback = function (threedsResponse) {
          console.log('Processing 3D Secure callback...');
          window.removeEventListener('message', process3dSecureCallback);

        var simplifyDomain = 'https://simplify.com'; // For UAT you can use 'https://uat.simplify.com'

          if (threedsResponse.origin === simplifyDomain && JSON.parse(threedsResponse.data)['secure3d']['authenticated']) {  // JSON.parse(data.data)['secure3d.authenticated']) {
            var completePayload = {
              amount: 1500,
              currency: currency,
              description: 'description',
              token: token
            };

            $.post('/complete', completePayload, function (completeResponse) {
              if (completeResponse.success) {
                $('#simplify-payment-form').hide();
                $('#simplify-success').show();
              }
              iframeNode.hide();
            });
          }
        };

        iframeNode.on('load', function () {
          window.addEventListener('message', process3dSecureCallback);
        });

        secure3dForm.submit();
      }
    });
  }
}

$(document).ready(function () {
  $('#simplify-payment-form').on('submit', function () {
    processPayment();
    return false;
  });
});
