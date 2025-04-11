# Simple Messaging Protocol (SMP)

## Running the Application

### Compilation
```sh
# Create bin directory
mkdir -p bin

# Compile protocol classes first
javac -d bin protocol/*.java

# Compile client and server with protocol classes in classpath
javac -cp bin -d bin client/*.java
javac -cp bin -d bin server/*.java
```

### Running the Server
```sh
java -cp bin -Djavax.net.ssl.keyStore=smpkeystore.jks -Djavax.net.ssl.keyStorePassword=smppassword server.AppServer
```

### Running the Client
```bash
java -cp bin -Djavax.net.ssl.trustStore=smpkeystore.jks -Djavax.net.ssl.trustStorePassword=smppassword client.SMPClientGUI
```

## Class Diagram

```mermaid
classDiagram

    class SMPClientGUI {
        -SMPClientWrapper client
        -boolean isLoggedIn
        +login()
        +sendMessage()
        +downloadOne()
        +downloadAll()
        +logout()
    }
    
    namespace SMP Protocol Components {
        class SMPStreamSocket {
            -SSLSocket sslSocket
            -DataInputStream input
            -DataOutputStream output
            +SMPStreamSocket(SSLSocket sslSocket)
            +SMPStreamSocket(InetAddress host, int port)
            +sendMessage(byte command, byte[] payload)
            +sendError(String errorMessage)
            +getInputStream()
            +getOutputStream()
        }

        class SMPServerWrapper {
            -int port
            -ExecutorService threadPool
            -SSLServerSocket serverSocket
            -SMPCommandHandler handler
            -SMPSessionManager sessionManager
            +SMPServerWrapper(int port, int maxClients, SMPCommandHandler handler, SMPSessionManager sessionManager)
            +start()
            +shutdown()
        }

        class SMPServerThread {
            -SMPStreamSocket socket
            -SMPCommandHandler handler
            +SMPServerThread(SMPStreamSocket socket, SMPCommandHandler handler, SMPSessionManager sessionManager) 
            +run()
        }

        class SMPClientWrapper {
            -String host
            -int port
            -SMPStreamSocket streamSocket
            +SMPClientWrapper(String host, int port)
            +connect()
            +sendMessage(byte type, byte[] payload)
            +receiveMessage(): byte[]
            +close()
        }

        class SMPCommandHandler {
            <<interface>>
            +handleLoginSMPStreamSocket socket, byte[] payload()
            +handleUpload(SMPStreamSocket socket, byte[] payload)
            +handleDownloadAll(SMPStreamSocket socket)
            +handleDownloadOne(SMPStreamSocket socket, byte[] payload)
            +handleLogout(SMPStreamSocket socket)
            +getSessionManager(): SMPSessionManager
        }

        class SMPSessionManager {
            <<interface>>
            +loginUser()
            +getUsername()
            +logoutUser()
        }
    }


		namespace App Server Implementation{
	    class AppCommandHandler {
	        -List[String] messages
	        -SMPSessionManager sessionManager
	    }
	
	    class AppSessionManager {
	        -Map~SMPStreamSocket,String~ userSessions
	    }
	
	    class AppServer {
	        -SMPServerWrapper server
	        +start()
	        +shutdown()
	    }
    }
    
    
    AppCommandHandler ..|> SMPCommandHandler : implements
    AppSessionManager ..|> SMPSessionManager : implements
    AppCommandHandler --> SMPSessionManager : uses
    
		
		
    SMPServerWrapper --> SMPServerThread : creates
    SMPServerWrapper --> SMPSessionManager : uses
    SMPServerThread --> SMPCommandHandler : uses
    SMPServerThread --> SMPStreamSocket : uses
    SMPClientWrapper --> SMPStreamSocket : uses
    
    AppServer --> SMPServerWrapper : uses
    AppServer --> AppCommandHandler : creates
    AppServer --> AppSessionManager : creates
    
    SMPClientGUI --> SMPClientWrapper : uses
    

```