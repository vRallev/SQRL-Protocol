SQRL-Protocol
=============

An implementation of the SQRL protocol. This library helps to parse, validate and create SQRL messages.

Download
--------

Download [the latest JAR][1] or grab via Gradle or Maven:

```groovy
dependencies {
    compile 'net.vrallev.sqrl:sqrl-protocol:0.0.1'
}
```

```xml
<dependency>
    <groupId>net.vrallev.sqrl</groupId>
    <artifactId>sqrl-protocol</artifactId>
    <version>0.0.1</version>
</dependency>
```

Advanced
--------

The library requires to implement the `EccProvider25519` interface. You can either implement this interface or use [one of my other libraries][2]. The libraries are automatically found and you don't need to setup something else. 

Both libraries depend on other code. **Please respect the author's licenses!**

```groovy
repositories {
    maven {
        url 'https://raw.github.com/vRallev/mvn-repo/master/'
    }
}

dependencies {
    compile 'net.vrallev.android.library:ecc-25519:1.0.1' // Android
    compile 'net.vrallev.java.library:ecc-25519:1.0.1' // JVM
}
```

Usage
-----

The class `SqrlProtocol` serves as entry point. You may want to take a look at the [unit tests][3].

```java
SqrlClientBody bodyOriginal = SqrlProtocol.instance()
		.authenticate(identity.getMasterKey(), mSiteKey)
        .buildRequest(mSignatureUri);

SqrlClientBody bodyParsed = SqrlProtocol.instance()
        .readSqrlClientBody()
        .from(bodyOriginal.getBodyEncoded())
        .verified();

SqrlServerBody bodyOriginal = SqrlProtocol.instance()
		.answerClient(body, ServerParameter.SQRL_ACCOUNT_CREATION_ALLOWED)
		.withServerFriendlyName("Unit Test")
		.create()
		.asSqrlServerBody();

SqrlServerBody bodyParsed = SqrlProtocol.instance()
		.readSqrlServerBody()
		.from(bodyOriginal.getBodyEncoded())
		.parsed();
```

License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[1]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=net.vrallev.sqrl&a=sqrl-protocol&v=LATEST
[2]: https://github.com/vRallev/ECC-25519
[3]: https://github.com/vRallev/SQRL-Protocol/tree/master/sqrl-protocol/src/test/java/net/vrallev/java/sqrl/test