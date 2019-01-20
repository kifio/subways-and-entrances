var map, objectManager;

// Waiting for the API to load and DOM to be ready.
ymaps.ready(init);

function init () {
    /**
     * Creating an instance of the map and binding it to the container
     * with the specified ID ("map").
     */
    map = new ymaps.Map('map', {
        /**
         * When initializing the map, you must specify
         * its center and the zoom factor.
         */
        center: [55.76, 37.64], // Moscow
        zoom: 9,
        controls: ['zoomControl']
    });

    objectManager = new ymaps.ObjectManager();
}

function addFeatures() {
    $.getJSON('geoObjects.geojson')
        .done(function (geoJson) {

        geoJson.features.forEach(function (obj) {
            // Setting the balloon content.
            obj.properties.balloonContent = obj.properties.name;
        });

        // Adding JSON object descriptions to the object manager.
        objectManager.add(geoJson);
        // Adding objects to the map.
        map.geoObjects.add(objectManager);
    });
}
