#!/usr/bin/env bash

version="2.1-SNAPSHOT"
jar="aroma-service-$version.jar"

nohup java -jar $jar > server.log &