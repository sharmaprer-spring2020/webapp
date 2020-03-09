#!/bin/bash
#Stop running tomcat service 
echo 'ps -aef | grep tomcat'
sudo systemctl stop tomcat.service