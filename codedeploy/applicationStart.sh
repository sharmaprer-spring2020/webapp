#!/bin/bash
echo "configure cloudwatch agent"
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/home/ubuntu/cloudwatch-config.json \
    -s
    #sudo systemctl start cloudwatch.service
echo "cloudWatch configured"
while [ ! -f /home/ubuntu/list.txt ]
do
  sleep 2 # or less like 0.2
  echo "waiting for file"
done
echo "catalina.properties"
sudo cat opt/tomcat8/conf/catalina.properties
sudo systemctl daemon-reload
sudo systemctl restart tomcat.service 
echo "tomcat is now restarted"
