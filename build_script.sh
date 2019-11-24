#!/bin/bash

./mvnw -Dmaven.test.skip=true package

docker-compose build