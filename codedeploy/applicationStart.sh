#!/bin/bash
echo "Initiate Stop tomcat service"
sudo systemctl stop tomcat.service && 
sudo systemctl daemon-reload && 
sudo systemctl start tomcat.service

echo "tomcat is now started"