#!/bin/bash
sleep 60
stty -F /dev/ttyACM0 litout raw iutf8 115200
sleep 5
echo "hello" > /dev/ttyACM0
java -jar starway-1.0-SNAPSHOT.jar bigstar.json 
