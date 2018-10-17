***Simple project for:***
* parsing OSM
* uploading subway stations and entrances into Firebas
* drawing them on map in mobile client

***Few key steps to running project:***

* place file *pkey.json* with your Firebase project private key in two directories:
    * android/src/main/assets/ - for android client.
    * loader/ - for data parser.

* add Mapbox access token to android/src/main/res:
    <string name="mapbox_access_token" translatable="false">YOUR_TOKEN</string>
    
* replace base url in Common class, with your project url:
```kotlin
    const val baseUrl = "https://YOUR_PROJECT_NAME.firebaseio.com"
 ```

Also, you can add drawables for subway entrances. Current logic implemented for Moscow subway, where entrances have numbers from 1 to 16. 
Every entrance in response have an integer field *ref*. If you want to draw entrances with custom icons, you need to add drawables with names in format: metro{ref}.xml into *android/src/main/drawable/* folder. 


***Parsing OSM***

Inside loader/ folder you can find two files: *entrances.osm* and *stations.osm* . These files contains list of subway staions  and list of subway entrances in Moscow. 

For generating alternative datasets, you can [download specific region](https://wiki.openstreetmap.org/wiki/Downloading_data)
and run commands like:

```osmosis --rbf RU-MOS.pbf --nkv keyValueList="station.subway" --wx stations.osm```

```osmosis --rbf RU-MOS.pbf --nkv keyValueList="railway.subway_entrance" --wx entrances.osm```


***Uploading data***

If you run command ./gradlew run -p loader, data from these filew will be uploaded to your Realtime Database in json format.


***Drawing data***

After this, you can run ./gradlew build -p android for creating .apk, which get data from firebase and draw it on map.

***Screenshots:***
![](https://github.com/kifio/subways-and-entrances/raw/master/1.jpg)
![](https://github.com/kifio/subways-and-entrances/raw/master/2.jpg)
