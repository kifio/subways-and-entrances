***Subway Sandbox***

Sandbox for learning some good Android dev practices.

**Features:**
- Downloading and parsing .pbf file, extracting stations and entrances.
- Showing stations and entrances of Moscow subway on Mapbox Map.

TODO: Add Google Maps
TODO: Add OpenDataMos downloader & parser
TODO: Add stations names on maps.
TODO: Add Swing app for showing OSM data on map and editing parsed data.

Launch parser:

Osmosis tool must be installed.

launch script ```run_parser.sh```
It will starts downloading latest .pbf for Moscow, extract stations and entrances and save them to android/app/src/main/assets directory.
After this, android app could work with this data.

Launching android:

For compiling android app, you mus have keys for Mapbox and GoogleMaps and save them in resources.

Osmosis installation instructions:
https://wiki.openstreetmap.org/wiki/Osmosis/Installation

***Screenshots:***
![](https://github.com/kifio/subways-and-entrances/raw/master/1.jpg)
![](https://github.com/kifio/subways-and-entrances/raw/master/2.jpg)
