#!/bin/bash
echo "Clean up tomcat dir"
RootFile=/opt/tomcat8/webapps/ROOT
RootWar=/opt/tomcat8/webapps/ROOT.war
LogFile=/opt/tomcat8/appLogs/

if [ -e "$RootFile" ] || [ -d "$RootFile" ]; then
   sudo rm -rf $RootFile
else
  echo "default ROOT file does not exist"
fi

if [ -e "$RootWar" ]; then
    sudo rm $RootWar
else
  echo "ROOT.war does not exist"
fi

if [ -d "$LogFile" ]; then
    sudo rm -rf $LogFile
else
  echo "appLogs dir does not exist"
fi
echo "Clean up done..!!"