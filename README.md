# java-communication-client

Current protocol version : v2

## Prerequisite

To use the Obopay REST APIs, the client will have to register with Obopay. During the registration process, the client will get a unique client id, Obopay server host name, Obopay server port and Obopay server public key (for data encryption in the API request / response). The client will also have to provide their certficate and / or public key (for data encryption in the API request / response), and their ip from which the Obopay API will be hit. After registration, Obopay will provide a list of APIs, giving their respective name and param / response type with examples.

The library requires Java 1.8 or above version.

## Building from source

1. Install Eclipse on your machine.
2. Git clone this repo to your local disk.
3. Import this project as a new project.
4. Right click on the project -> Run As -> Maven build. Enter "clean install" as Goal.
5. Once the build finishes, press F5 on the project to refresh it. You can see a target folder, which contains jar java-communication-client-1.0.jar.

## Dependencies
ObopayClient is dependant on the below jars
### Maven
```xml
	<dependency>
  		<groupId>org.slf4j</groupId>
  		<artifactId>slf4j-api</artifactId>
  		<version>1.7.26</version>
  	</dependency>
  	<dependency>
  		<groupId>org.bouncycastle</groupId>
  		<artifactId>bcprov-jdk15on</artifactId>
  		<version>1.61</version>
  	</dependency>
```
The library uses SLF4J api for logging purpose.
To use SLF4J with Log4j2 you should add the following dependenccies
```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
    <version>2.7</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.7</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j-impl</artifactId>
    <version>2.7</version>
</dependency>
```

## Usage

Make sure that java-communication-client.jar and all the dependencies mentioned above are present in the classpath.

Initialize **ObopayClient** with the respective config values.
```Java
   //read the server public key into a byte array
   byte[] serverPublicKey = 
   //read the client private key into a byte array
   byte[] clientPrivateKey =
   //create Config object
   Config config = new Config();
   config.setCid("test_cid").setHostName("localhost").setPort(7001).setProtocolVersion("v2")
	.config.setServerPublicKey(serverPublicKey).setClientPrivateKey(clientPrivateKey);
	//initialize the client once. ObopayClient is thread safe
	ObopayClient client = ObopayClient.init(config);
	//invoke the api with json and api name
	String resposne = client.sendRequest("testApi", jsonString);
```
### Response 
The json response from **ObopayClient** will be of the following structure.

    {
      error   : null OR error-code
              // null value or absence of this field indicates success, otherwise error
      data    : response data OR error-message
              // empty object body or absense indicates no data
	}
