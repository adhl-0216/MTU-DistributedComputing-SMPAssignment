#!/bin/bash

java -cp bin -Djavax.net.ssl.trustStore=smpkeystore.jks -Djavax.net.ssl.trustStorePassword=smppassword client.SMPClientGUI