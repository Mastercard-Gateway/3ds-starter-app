<h1>3DS Starter App</h1>
<p>The 3DS starter app demos how to support a 3D secure flow when using a Simplify SDK.The flow is based on Card Token API call.  This call is responsible for:</p>
<ul>
  <li>Checking 3D secure card enrollment</li>
  <li>Complete Card tokenisation</li>
  <li>Return 3D secure data in the response to be processed by the merchant</li>
</ul>

* <h3><b>Merchant Setup</b></h3>

  For 3DS to be active:

  - The acquirer configuration is set up for 3D secure handling;
  - The acquirer (or the merchant-specific) capability configurations for 3D secure are turned
    on.
  - The 3D secure merchant setting is set to true.
  - The card used for payment must be 3DS enrolled.

* <h3>Running the Starter App.</h3>

  The starter app is a Vert.x application that's developed in Java 8. It is started using Maven:

mvn clean compile vertx:run -Dthreeds.public_key=lvpb_12345 -Dthreeds.private_key=deadcafe1234...

The private key and public key can be obtained for registered merchants from merchant Portal. To get those go to Settings → API Keys from Merchant Portal.

This starts an HTTP server on port 8085 that can be called to display
the test page: [http://localhost:8085]


* <h3>TroubleShooting Maven</h3>

If the mvn target does not run from your command prompt due to firewall follow the below steps → \
- Download the maven repo(https://repo.maven.apache.org/maven2/) SSL certificate onto your machine.

- Import the certifcates on local keystore, using below command \
   keytool -import -file C:\temp\mavenCert.cer -keystore C:\temp\mavenKeystore

- Now, start the server by using below options→

mvn clean compile vertx:run  -Dthreeds.public_key=lvpb_12345  -Dthreeds.private_key=deadcafe1234... -Djavax.net.ssl.trustStore=C:\temp\mavenKeystore (Replace temp with your folder location)

- For more details refer the link below 

 https://stackoverflow.com/questions/25911623/problems-using-maven-and-ssl-behind-proxy

* <h3>TroubleShooting Java</h3>

 3DS Starter app requires Java 8 and above to run. If your Java home points to a version lesser than Java 8, then follow
 the below steps to run the app.

 I have two JDKs installed on my Windows Machine - JDK 1.7 and JDK 1.8.

 

My default (and set to windows system environment variable) JAVA_HOME is set to JDK 1.7.

 

However, I have a maven project that I need to build (i.e., 3DS) SDK using JDK 1.8.

 

My solution in this scenario (which worked!), is to set it in mvn.cmd.

 
Just add the set JAVA_HOME=<path_to_other_jdk> line after @REM ==== START VALIDATION ==== in mvn.cmd (i.e., %MAVEN_HOME%\bin\mvn.cmd):

 

@REM ==== START VALIDATION ==== set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_191(java 8 version you are using) if not "%JAVA_HOME%" == "" goto OkJHome

<h3>##References</h3>
<ul>
<li><a href="https://www.simplify.com/commerce/docs/examples/threeds" rel="nofollow">https://www.simplify.com/commerce/docs/examples/threeds</a></li>
<li><a href="https://www.simplify.com/commerce/docs/tutorial/index#three-ds" rel="nofollow">https://www.simplify.com/commerce/docs/tutorial/index#three-ds</a></li>
</ul>