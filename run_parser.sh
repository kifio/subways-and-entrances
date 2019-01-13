#!/bin/bash

# Clear old data
rm RU-MOS.pbf
rm android/src/main/assets/*

# Download latest data
curl https://needgeo.com/data/current/region/RU/RU-MOS.pbf --output RU-MOS.pbf

# Generate new .osm files with stations and entrances
osmosis --rbf RU-MOS.pbf --nkv keyValueList="railway.subway_entrance" --wx android/src/main/assets/entrances.osm
osmosis --rbf RU-MOS.pbf --nkv keyValueList="station.subway" --wx android/src/main/assets/stations.osm

#./gradlew clean run -p loader \
#  --args '../android/src/main/assets/entrances.osm ../android/src/main/assets/stations.osm ../entrances.csv'