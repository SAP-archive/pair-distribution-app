#!/usr/bin/env bash

set -e

environment="$1"

#setup environment
mkdir -p src/main/resources/
if [ -z "$environment" ]; then
  mvn clean install
else
  cp deploy/$environment/manifest.yml .
  cp deploy/$environment/application.properties src/main/resources/
  mvn -P$environment clean install
fi

cf push
