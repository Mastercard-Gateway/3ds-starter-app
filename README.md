<h1>3DS Starter App</h1>
<p>The 3DS starter app demos how to support a 3D Secure flow when using a Simplify SDK. The flow is based on a Card Token API call.  This call is responsible for:</p>
<ul>
  <li>Checking 3DS card enrollment</li>
  <li>Complete Card tokenisation</li>
  <li>Return 3DS data in the response to be processed by the merchant</li>
</ul>

<p>The EMV 3DS transaction can be performed uisng the Enable EMV 3DS checkbox.</p>

* <h3><b>Merchant Setup</b></h3>

  For 3DS to be active:

  - Both your account and the acquirer is configured to support 3DS transactions
  - The cardholder’s card supports 3DS

* <h3>Running the Starter App.</h3>

  The starter app is a Vert.x application that's developed in Java 8. It is started using Maven:

mvn clean compile vertx:run -Dthreeds.public_key=lvpb_12345 -Dthreeds.private_key=abcd1234...

The private key and public key can be obtained for registered merchants from merchant Portal. To get those go to Settings → API Keys from Merchant Portal.

This starts an HTTP server on port 8085 that can be called to display
the test page: [http://localhost:8085]


* <h3>TroubleShooting Maven</h3>

If the mvn target does not run from your command prompt due to firewall follow the below steps → \
- Download the maven repo(https://repo.maven.apache.org/maven2/) SSL certificate onto your machine.

- Import the certifcates on local keystore, using below command \
   keytool -import -file C:\temp\mavenCert.cer -keystore C:\temp\mavenKeystore

- Now, start the server by using below options→

mvn clean compile vertx:run  -Dthreeds.public_key=lvpb_12345  -Dthreeds.private_key=abcd1234... -Djavax.net.ssl.trustStore=C:\temp\mavenKeystore (Replace temp with your folder location)

- For more details refer the link below 

 https://stackoverflow.com/questions/25911623/problems-using-maven-and-ssl-behind-proxy

<h3>##References</h3>
<ul>
<li><a href="https://www.simplify.com/commerce/docs/examples/threeds" rel="nofollow">https://www.simplify.com/commerce/docs/examples/threeds</a></li>
<li><a href="https://www.simplify.com/commerce/docs/tutorial/index#three-ds" rel="nofollow">https://www.simplify.com/commerce/docs/tutorial/index#three-ds</a></li>
</ul>
