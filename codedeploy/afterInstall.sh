#!/bin/bash
echo "Initiate stop cloud watch agent"
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -m ec2 -a stop
#sudo systemctl stop cloudwatch.service
