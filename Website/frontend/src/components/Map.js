import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle, Polyline } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

function MapComponent() {

    const [mariborCoordinates, setCoordinates] = React.useState([46.558993, 15.638081]);
    const [pathCoordinates, setPathCoordinates] = React.useState([
        [46.558993, 15.638081], // Starting point coordinates
        [46.558, 15.639], // Ending point coordinates
    ]);

    const [polylineOptions, setPolylineOptions] = React.useState({
        color: 'green', // Change the color to red
        dashArray: '1, 4', // Make it a dotted line
    });
    
    const shouldChangeOptions = false;

    useEffect(() => {
        if (shouldChangeOptions) {
            setPolylineOptions({
            color: 'red', // Change the color to blue
            dashArray: '3, 6', // Make it a dashed line
            });
        }
    }, []);
    

    return (
        <MapContainer center={mariborCoordinates} zoom={13} scrollWheelZoom={true} style={{ height: "90vh" }}>
            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        {/* Add markers and other components here */}
            <Polyline positions={pathCoordinates} pathOptions={polylineOptions}/>
        </MapContainer>
    );
}

export default MapComponent;