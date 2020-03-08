#!/bin/bash
#Stop running tomcat service 
ps -aef | grep tomcat
sudo systemctl stop tomcat.service