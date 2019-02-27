#!/bin/bash

rm entrances.csv
./gradlew clean run -p loader \
  --args 'odm ../android/src/main/assets/data-2019-02-27.json entrances.csv'
