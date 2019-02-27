#!/bin/bash

rm entrances.csv
./gradlew clean run -p loader \
  --args 'osm android/src/main/assets/entrances.osm android/src/main/assets/stations.osm entrances.csv'
