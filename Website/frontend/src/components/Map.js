import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle, Polyline } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

function MapComponent() {

    const [paths, setPaths] = useState([]);


    useEffect(function(){
        const getPaths = async function(){
            const res = await fetch("http://localhost:3001/users");
            const data = await res.json();
            if(!res.ok){
                console.log(data);
                return;
            }
            
            
            var tempPaths = [];
            for (var i = 0; i < data.length; i++) {
                for (var j = 0; j < data[i].roadStates.length - 1; j++) {
                    var tempStateOfRoad = data[i].roadStates[j].stateOfRoad;
                    var tempColor = tempStateOfRoad === 0 ? 'green' : tempStateOfRoad === 1 ? 'yellow' : tempStateOfRoad === 2 ? 'red' : 'black';
                    tempPaths.push({ polylineOptions: {color: tempColor, dashArray: '3, 6', weight: 9}, path: [[data[i].roadStates[j].latitude, data[i].roadStates[j].longitude], [data[i].roadStates[j + 1].latitude, data[i].roadStates[j + 1].longitude]]});
                }
                
            }
            setPaths(tempPaths);
        }

        getPaths();

        
    }, []);

    const [mariborCoordinates, setCoordinates] = React.useState([46.558993, 15.638081]);
    const [pathCoordinates, setPathCoordinates] = React.useState([
        [46.558993, 15.638081], // Starting point coordinates
        [46.558, 15.639],
        [46.570, 15.670],
        [46.571, 15.671], // Ending point coordinates
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
            weight: 50, // Make it 10 pixels wide
            });
        }
    }, []);
    

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
