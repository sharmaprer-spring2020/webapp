#!/bin/bash
#Clean up tomcat

RootFile=/opt/tomcat8/webapps/ROOT
RootWar=/opt/tomcat8/webapps/ROOT.war

if [ -e "$RootFile" ]; then
   sudo rm $RootFile
else
  echo "default ROOT file does not exist"
fi

if [ -d "$RootFile" ]; then
   sudo rm -rf $RootFile
else
  echo "ROOT dir does not exist"
fi

if [ -e "$RootWar" ]; then
    sudo rm $RootWar
else
  echo "ROOT.war does not exist"
fi
