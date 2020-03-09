#!/bin/bash
#Clean up tomcat artifacts
if [ -f /opt/tomcat8/webapps/ROOT.war ]
then
    sudo rm -rf /opt/tomcat8/webapps/ROOT.war
else
    echo "No ROOT.war exists"
fi


if [ -f /opt/tomcat8/webapps/ROOT ]
then
    sudo rm -rf /opt/tomcat8/webapps/ROOT
else
    echo "No ROOT exists"
fi


