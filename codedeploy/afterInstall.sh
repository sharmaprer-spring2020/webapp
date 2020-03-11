#!/bin/bash
echo "Initiate Stop tomcat service"
sudo systemctl stop tomcat.service && 
sudo systemctl daemon-reload
echo "In afterInstall.sh"