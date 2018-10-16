***Simple project for:***
* parsing OSM
* uploading subway stations and entrances into Firebas
* drawing them on map in mobile client

For running project you must place file *pkey.json* with your Firebase project private key in two directories:
* android/src/main/assets/ - for android client.
* loader/ - for data parser.

Also, you must have drawables for subway entrances. Current logic implemented for Moscow subway, where entrances have numbers from 1 to 16. 
Every entrance in response have an integer field ref. If you want to draw entrances you need to add icons with names in format: metro{ref}.xml into *android/src/main/drawable/* folder. 
