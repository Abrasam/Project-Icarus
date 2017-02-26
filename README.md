# Project-Icarus
This is a piece of software which allows two way communications with an airborne HAB payload.
This is being developed for the UKHAS and is my A-Level Computer Science coursework for AQA.
This project will be regularly updated and hopefully, when finished, forked and improved by the active HAB community.
For information on HAB (High Altitube Ballooning) see ukhas.org.uk or come talk to us on #highaltitude on freenode.

# Installation Instructions
You will need to have Oracle Java 8 (or above) in order to run this, this should come pre-installed on the latest Raspbian builds, for those with older Pis you can install with ```sudo apt-get update && sudo apt-get install oracle-java8-jdk```.

In order to run this you will need to have Pi4J installed on your Pis, this can be installed with the below command:

```curl -s get.pi4j.com | sudo bash```

You should also install git if it is not already installed (it should be on Raspbian). The jars can then be downloaded like so:

Ground jar:
```wget https://github.com/Abrasam/Project-Icarus/raw/master/out/artifacts/Ground_jar/Ground.jar -o Ground.jar ```

Payload jar:
```wget https://github.com/Abrasam/Project-Icarus/raw/master/out/artifacts/Payload_jar/Payload.jar -o Payload.jar ```

These files can now be executed with ```sudo java -jar <jarfile>.jar``` note that if running the ground station via VNC you will need to use '''sudo -E''' (in order to preserve environmental variables).

The software is configured to work with this board by Uputronics: https://store.uputronics.com/index.php?route=product/product&path=61&product_id=68 with the LoRa module in the CE1 slot.

If you have any problems please raise an issue!
