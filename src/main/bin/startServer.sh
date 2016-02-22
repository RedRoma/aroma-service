#!/usr/bin/env bash

version="1.0"
jar="banana-service-$version.jar"

nohup java -jar $jar > server.log &