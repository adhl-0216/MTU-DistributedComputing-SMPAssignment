#!/bin/bash

java -cp bin -Djavax.net.ssl.keyStore=smpkeystore.jks -Djavax.net.ssl.keyStorePassword=smppassword server.AppServer