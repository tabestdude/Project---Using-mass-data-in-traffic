import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle, Polyline } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

function MapComponent() {

    const [paths, setPaths] = useState([]);
    var errorDetected = false;

    useEffect(function(){
        const getPaths = async function(){
            var res;
            if(errorDetected){
                return;
            }
            try{
                res = await fetch("http://localhost:3001/users");
                if(!res.ok){
                    errorDetected = true;
                    return;
                }
            } catch (err) {
                errorDetected = true;
                return;
            }

            const data = await res.json();
            
            var tempPaths = [];
            for (var i = 0; i < data.length; i++) {
                for (var j = 0; j < data[i].roadStates.length - 1; j++) {
                    var tempStateOfRoad = data[i].roadStates[j].stateOfRoad;
                    var tempColor = tempStateOfRoad === 0 ? 'green' : tempStateOfRoad === 1 ? 'yellow' : tempStateOfRoad === 2 ? 'red' : 'black';
                    tempPaths.push({ polylineOptions: {color: tempColor, dashArray: '3, 6', weight: 4}, path: [[data[i].roadStates[j].latitude, data[i].roadStates[j].longitude], [data[i].roadStates[j + 1].latitude, data[i].roadStates[j + 1].longitude]]});
                }
                
            }
            setPaths(tempPaths);
        }

        // Set interval to call getPaths every 5000ms (5 seconds)
        const interval = setInterval(() => {
            getPaths();
        }, 1000);

        // Clear interval on component unmount
        return () => clearInterval(interval);
    }, []);

    const [mariborCoordinates, setCoordinates] = React.useState([46.558993, 15.638081]);

    return (
        <MapContainer center={mariborCoordinates} zoom={16} scrollWheelZoom={true} style={{ height: "90vh" }}>
            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />

            {paths.length > 0 && paths.map((roadPath, index) => (
                <Polyline key={index} positions={roadPath.path} pathOptions={roadPath.polylineOptions} />
            ))}
            
        </MapContainer>
    );
}

export default MapComponent;
