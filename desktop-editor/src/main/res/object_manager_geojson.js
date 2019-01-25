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
        center: [37.64, 55.76], // Moscow
        zoom: 11,
        controls: ['zoomControl']
    });
    objectManager = new ymaps.ObjectManager();
}

function addFeatures(geoJson) {
    geoJson.features.forEach(function (obj) {
        // app.log(String(obj.properties.name));
        // Creating a circle.
            var circle = new ymaps.Circle([
                // The coordinates of the center of the circle.
                obj.geometry.coordinates,
                // The radius of the circle in meters.
                15
            ], {
                /**
                 * Describing the properties of the circle.
                 * The contents of the balloon.
                 */
                balloonContent: "Radius of the circle: 15 m"
            }, {
                /**
                 * Fill color.
                 * The last byte (77) defines transparency.
                 * The transparency of the fill can also be set using the option "fillOpacity".
                 */
                fillColor: obj.properties.color,
                // Stroke color.
                strokeColor: obj.properties.color,
                // The width of the stroke in pixels.
                strokeWidth: 1
            });

            // Adding objects to the map.
            map.geoObjects.add(circle);
    });
}
