#!/bin/bash
#Clean up tomcat artifacts
if [ -e /opt/tomcat8/webapps/ROOT.war ]
then
    sudo rm /opt/tomcat8/webapps/ROOT.war
else
    echo "No ROOT/ROOT.war exists"
fi


if [-e /opt/tomcat8/webapps/ROOT]
then
    sudo rm -r /opt/tomcat8/webapps/ROOT
else
    echo "No ROOT/ROOT directory exists"
fi


