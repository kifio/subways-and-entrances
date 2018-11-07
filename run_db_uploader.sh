#!/bin/bash

./gradlew clean run -p db \
  --args '../entrances.csv'
