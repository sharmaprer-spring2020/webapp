#!/bin/bash
echo "Stop tomcat service" 
sudo systemctl stop tomcat.service
sudo systemctl stop cloudwatch.service
#sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -m ec2 -a stop