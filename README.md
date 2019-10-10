Virus Checker
=============

This repository contains a standalone virus checker application stack similar to VirusTotal.

It contains several executable modules:

- **driver** program provides a REST API for communication with the supported antivirus solutions.
- **gateway** serves to upload files to several drivers in parallel. 
- **client-cli** is a simple console REST client for uploading files to the gateway and exporting 
reports to CSV files.  
- **client-web** is a simple graphical web frontend with the similar purpose as **client-cli**.

There are also some helper modules that contain common dependencies or classes:

- **common**
- **cli-common**

The architecture of this software solution is visualized below.

```dtd
client-cli ---:               :--- driver (Avast, Eset, Kaspersky, Microsoft)
              :--- gateway ---:
client-web ---:               :--- driver (Comodo, VirusTotal)                               
```

Before diving further we denote the following terms:
- AV = Antivirus software
- JDK = Java Development Kit
- JRE = Java Runtime Environment
- VM = Virtual Machine
- VM/C = Virtual Machine or Container
- host = The native OS on your machine

Installation
============

First of all, we do not currently provide a one-click installer.
Also, this software does not include ready-to-use VM/Cs with deployed AVs.

These steps describe how to build and deploy this program from scratch.
They can be summarized in the following steps.

1. Compile the source code
2. Deploy the driver/s
   - Create a VMs with supported antivirus programs.
   - Install and configure each antivirus - i.e. disable the automatic protections.
   - Run the driver.
3. Deploy gateway.
4. Deploy web client.
5. (Optional) Run console client.

The following subsections thoroughly describe each of the five steps. 

1 Compiling the source code
---------------------------

Building this software requires JDK 1.8 or later (OpenJDK is sufficient).

Open a terminal in the project directory and
- on a Windows machine run
    ```bash 	
    gradlew.bat clean build shadowJar
    ```
- on a Linux machine run
    ```bash 	
    ./gradlew clean build shadowJar
    ```
    
2 Deploy antivirus driver program
---------------------------------

The location of the compiled Java executable is `driver/build/libs/driver-[VERSION]-all.jar`.

A running driver performs these tasks in the following order:
1. Receive a file from the gateway.
2. Invoke the AV command line tool to scan the received file.
3. Read and process the scan reports.
4. Send back the scan result.
  
As of this moment the supported antivirus software includes
- Avast
- Comodo
- Eset
- Kaspersky
- Windows defender

In addition to these AVs, the driver also supports querying VirusTotal online virus 
database with SHA-256 hashes computed automatically from the scanned file.
Please note that the driver uploads only hash of the file to VirusTotal, not the file itself.

### 2.1 Prepare a virtual machine

The recommended setup for the entire VirusChecker is to have all AVs and their driver 
installed on one or more VMs with their local port 8080 forwarded to the host.

We provide documentation for two ways to deploy the drivers - on Windows based virtual 
machines or on Linux based virtual machines.
We assume that VirtualBox is used as a virtualization platform.

* Create a [Windows virtual machine using VirtualBox](docs/driver/drivers-on-windows.md)

* Create a [Linux virtual machine using VirtualBox](docs/driver/drivers-on-linux.md)

* (TODO) Create a [Linux containers using Docker](docs/driver/drivers-on-docker.md)

* Enable VirusTotal hash database [guide](docs/driver/driver-virustotal.md)

###### Note

Currently, commercial AVs are supported only on Windows, and the free 
one(s) are supported only on Linux.
If you want to run some commercial AV, for example Eset, on Linux or implement your own driver for a 
currently unsupported AV you may visit this [guide](docs/driver/extensions.md).

### 2.2 Run the deployed driver

The location of the compiled Java executable is `driver/build/libs/driver-[VERSION]-all.jar`.
Copy this file to some reasonable place on the VM, for example `~/virus-checker` (on Linux VM) or 
`C:\virus-checker` (on Windows VM).

If the guest VM add-ons work properly, you may simply drag & drop the file from your 
system's file manager to the VM.
 
* On the VM, open a terminal in the folder with the driver executable.   

* Type `java -jar [NAME-OF-PROGRAM] [ANTIVIRUSES]` and press enter.
    * `[NAME-OF-PROGRAM]` is the name of the driver JAR file.
    * `[ANTIVIRUSES]` must be one or more of the following: 
    `AVAST, COMODO, ESET, KASPERSKY, MICROSOFT, VIRUS_TOTAL`.
    * Optionally you may use `-a` option to auto-detect all installed AVs.
     This includes VirusTotal if there is an API key specified in `viruschecker-driver.properties`. 

* Examples:
    * `java -jar driver-1.0.0-all.jar KASPERSKY` if you only have Kaspersky Antivirus installed on 
    the VM.
    * `java -jar driver-1.0.0-all.jar ESET KASPESRY VIRUS_TOTAL` if you have both Eset and Kaspersky
     installed on the VM and also want to use the VirusTotal service.

If Windows firewall popup window asks for permission, then allow it at least for private networks.
 
To verify the successful launch of the driver program, open a web browser on the VM and visit 
`http://127.0.0.1:8080/`.
The driver should respond with JSON containing some basic info about itself.

If the network adapter of your running VMs had been attached to a NAT with a guest port 
**8080** forwarded to host's **8081**, then you may visit `http://127.0.0.1:8081/` on the host with 
the same respond.   

###### Different port

You can specify the listening port other than **8080** using the `-port=` parameter, e.g. 
`java -jar driver-1.0.0-all.jar AVAST ESET -port=9595` will set the listening port to **9595**.
Please be aware, that ports **7978** and **7979** are reserved by default for other modules of the 
VirusChecker. 
You will also need to reset port forwarding for the new port instead of **8080**.

### 2.3 Driver REST API

If you are a developer and want to use the driver programmatically, you can 
explore its REST web API.
The API endpoints are documented [here](docs/rest-api/rest-api.md).

For most users, however, this API is not important.

### 2.4 Extend driver

If you and want to configure the driver our you are a developer wanting to add support for 
a new antivirus by yourself, this [guide](docs/driver/extensions.md) is the place to go.

3 Deploy gateway
----------------


The purpose of the gateway is to simplify the implementation of client applications.
It receives data from the client and then sends it to all deployed drivers in parallel and then
store all scan reports in its embedded database.
Third party clients can either use the unified gateway API or upload files directly to the drivers.

The location of the compiled JAR executable is `gateway/build/libs/gateway-[VERSION]-all.jar`.

Gateway can be run from host or from the its own VM as well.
Theoretically it can be deployed on any machine with JRE 1.8, however, it was tested only on 
Ubuntu 18.04.

We will assume that the gateway will be deployed on a dedicated VM.

* Create a (linked) clone of some VM running the driver or install a new one. 
(It is ample to you use the Linux virtual machine.)

* Set the network adapter in the VM settings to *Bridged*.

* Allow host ports that are forwarded from the driver's VM to be opened for IP address of the 
gateway's VM. 
For example, if you have two running drivers on VMs with their listening port **8080** forwarded to 
host's ports **8081** and **8082** and your host is Linux you may use the command bellow with 
superuser privileges 
    ```bash
    ufw allow proto tcp from <insert-gateway-VM-IP> to any port 8081,8082
    ```
  On a Windows host follow these steps.
    * Press the *Start* button, search the program called *Windows Firewall with Advanced Security* 
    and open it.
    * In the left-hand side pane choose the *Inbound Rules* option. 
    * In the right-hand side panel choose the *New rule* option.
    * Choose *Custom* checkbox and press the *Next >* button until you reach the *Scope* pane.
        * Navigate to the *Which remote IP addresses does this rule apply to?* label and choose the 
        *These IP addresses* options.
        * In the large text field bellow write IP address of the gateway VM and press the *Next >*.
    * When you reach the *Name* pane, type the **Gateway** in the first text field and press the *Finnish*
     button.

* Copy the gateway JAR executable to the VM.

* In the folder with the copied JAR executable, create a new text file and put the full urls of the 
running drivers. 
For example, if you have two running drivers on VMs with their listening port **8080** forwarded to 
the host ports **8081** and **8082**, the file should look like below.
    ```dtd
    http://<insert-host-IP>:8081
    http://<insert-host-IP>:8082
    ```

* Save the file as, for example, `driverUrls.txt`.

* Assuming Java is in the *Path*, run terminal in this directory.

* Type `java -jar gateway-[VERSION]-all.jar driverUrls.txt` and press enter.
    
To verify the successful launch of the gateway, open a web browser in a VM and go 
to `http://127.0.0.1:8080/`.
The gateway should respond with JSON containing some basic info about itself.

Remember to open port 8080 for TCP on the VM if you wish to connect to the gateway from other 
computers on the network.

### 3.1 Gateway REST API

If you are a developer and want to use the gateway programmatically, you can 
explore its REST web API [here](docs/rest-api/rest-api.md).

4 Deploy client web application
-------------------------------

This client provides simple web based interface to send files to the gateway and displaying 
retrieved reports.

The location of the compiled JRE executable is `client-web/build/libs/client-web-[VERSION]-all.jar`.
Copy it to the VM with deployed gateway, but create a new folder for it. 

Assuming Java is in the *Path* on the VM and the gateway is running on the same machine, run terminal in this 
directory.

Type `java -jar client-web-[VERSION]-all.jar http://localhost:8080` and press enter.
By default, the web application will run on port `7979`.

Open the web browser and go to `http://localhost:7979/`.
If the application started successfully you should see a graphical web interface.

Do not forget to open port **7979** on the machine/VM running the client for machines that are 
intended to use its graphical interface.  
   
5 Deploy client cli application
-------------------------------

This client provides simple text based interface to send files to the gateway and export reports to 
file.
Currently it supports text and `csv` formats.

The location of the compiled JRE executable is `client-cli/build/libs/client-cli-[VERSION]-all.jar`.

Assuming Java is in the *Path*, run terminal in this directory.

Type `java -jar client-cli-[VERSION]-all.jar -h` and press enter.
Successful launch should print this help
```bash
java -jar client-cli-0.14.1-all.jar -h
usage: [-h] URL FILE-TO-SCAN [-t TIMEOUT] [-o OUT]

optional arguments:
  -h, --help          show this help message and exit

  -t TIMEOUT,         Optional: Sets socket timeout in milliseconds. Default
  --timeout TIMEOUT   value is 50000.

  -o OUT, --out OUT   Specify file to store report. Csv format is supported if
                      the filename ends with appropriate postfix.


positional arguments:
  URL                 Gateway url.

  FILE-TO-SCAN        File to scan. Does not support directories (only
                      archived).
``` 

###### Example

Assume we want to scan the file named `eicar.exe` that you placed in the same directory as the 
client executable, the url of running gateway is, for example `http://192.168.1.110`.
Also we want to save the report as `scanReport.csv`.
We can achieve this by running `java -jar client-cli-[VERSION]-all.jar http://192.168.1.110 eicar.exe --out scanReport.csv`.

6 SSL support
-------------

VirusChecker does not currently use SSL for the driver and gateway, although both clients programs
 support SSL and accept self-signed SSL certificates by default. 
Therefore if you wish to enable SSL for the gateway then you are free to use some reverse proxy like 
Nginx.