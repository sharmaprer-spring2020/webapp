#!/bin/bash
echo "In application Start.sh"
sudo systemctl stop tomcat.service && 
sudo systemctl daemon-reload && 
sudo systemctl start tomcat.service

echo "executed tomcat start stop"