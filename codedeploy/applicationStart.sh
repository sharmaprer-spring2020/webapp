#!/bin/bash
echo "In application Start.sh"
sudo systemctl daemon-reload
sudo systemctl start tomcat.service