# A Command Line Chat-APP with End-to-End Encryption

This is a small End-to-End demo App with a server and two clients.

## How to run this App
You will need 3 different Computers (Although you can use only one).
All the devises should have Java 16 or up installed, and 
An Java IDE (IntelliJ is recommended)
## Steps for all 3 devices
### Java 16
You can get java 16 at https://www.oracle.com/java/technologies/javase-jdk16-downloads.html

### IntelliJ
You can get IntelliJ IDE at https://www.jetbrains.com/idea/

### Clone the project
Assuming you have git installed in all 3 devices.
Run ```git clone git@github.com:makungaj1/crypto-chat-app-cli.git```

## Pick devices roles
All 3 devices should have Java 16 or up, IntelliJ (or another Java IDE)
and the clone of the project.

Get the IPs of all the devices by running
```ifconfig``` on mac or linux and ``ipconfig`` on Windows device

### Modify Constants
On all 3 devices, open the project in IntelliJ
go to ``src/com/jm/utils`` open ``Constant.java`` file
Locate ``SERVER_IP``, ``CLIENT_A_IP``, and ``CLIENT_B_IP``
Modify the IP accordingly.

## Server
On the device that you choose as the server
go to ``src/jm.server`` run the ``Server.java`` file.
this will start the server. You should see some like this
````
Jul 07, 2021 1:21:09 PM com.jm.server.Server main
INFO: Server started, listening on: IP:8000
Server Private Key: -----
Server Public Key: --------
Waiting for clients requests
````

## Run CLIENT B
On the device that you choose as client B, it's IP address
should match what you typed in ``Constat.CLIENT_B_IP`` go to ``src/jm/utils/Constant.java`` file to verify

open ``src/com/jm/client/Main.java`` and run the main method

## Run CLIENT A
Same as above

## Start sending messages between clients.
