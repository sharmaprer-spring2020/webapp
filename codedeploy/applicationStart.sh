#!/bin/bash
echo "configure cloudwatch agent"
CWconfigFile=/home/ubuntu/cloudwatch-config.json
if [ -e "$CWconfigFile" ]; then
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/home/ubuntu/cloudwatch-config.json \
    -s
    #sudo systemctl start cloudwatch.service
    echo "cloudWatch configured"
else
    echo "cloudWatch is already configured"
fi
sudo systemctl start tomcat.service 
#mkdir /opt/tomcat8/appLogs &&
sudo chmod -R 755 /opt/tomcat8/appLogs

echo "tomcat is now started"