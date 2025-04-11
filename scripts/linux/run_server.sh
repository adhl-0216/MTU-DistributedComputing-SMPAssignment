#!/bin/bash

# Change to project root directory (two levels up from script location)
cd "$(dirname "$0")/../.."

java -cp bin -Djavax.net.ssl.keyStore=smpkeystore.jks -Djavax.net.ssl.keyStorePassword=smppassword server.AppServer