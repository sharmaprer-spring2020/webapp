#!/bin/bash
echo "Initiate Stop tomcat service and daemon reload"
sudo systemctl stop tomcat.service && 
sudo systemctl daemon-reload && echo "stop daemon-reload" &&
sudo systemctl status tomcat.service &&
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -m ec2 -a stop
#sudo systemctl stop cloudwatch.service
