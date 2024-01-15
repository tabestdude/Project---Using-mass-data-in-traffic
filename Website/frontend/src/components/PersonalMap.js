import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Polyline } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import GraphComponent from './Graph';


function MapComponent() {

    const [paths, setPaths] = useState([]);
    const [data, setData] = useState([]);


    useEffect(function(){
        const getPaths = async function(){
            const res = await fetch("http://localhost:3001/users/personal/map", {credentials: "include"});
            const data = await res.json();
            if(!res.ok){
                console.log(data);
                return;
            }
            setData(data);
            var tempPaths = [];
            var date1 = new Date();
            var date2 = new Date();
            var timeDifference = 0;
            for (var j = 0; j < data.roadStates.length - 1; j++) {
                date1 = Date.parse(data.roadStates[j].acquisitionTime);
                date2 = Date.parse(data.roadStates[j + 1].acquisitionTime);
                // Calculate the time difference in milliseconds
                timeDifference = date2 - date1;
                if (timeDifference > 5000) {
                    continue;
                }
                var tempStateOfRoad = data.roadStates[j].stateOfRoad;
                var tempColor = tempStateOfRoad === 0 ? 'green' : tempStateOfRoad === 1 ? 'yellow' : tempStateOfRoad === 2 ? 'red' : 'black';
                tempPaths.push({ polylineOptions: {color: tempColor, dashArray: '3, 6', weight: 4}, path: [[data.roadStates[j].latitude, data.roadStates[j].longitude], [data.roadStates[j + 1].latitude, data.roadStates[j + 1].longitude]]});
            }
                
            
            setPaths(tempPaths);
        }

        // Set interval to call getPaths every 1000ms (1 seconds)
        const interval = setInterval(() => {
            getPaths();
        }, 1000);

        // Clear interval on component unmount
        return () => clearInterval(interval);
    }, []);

    const [mariborCoordinates, setCoordinates] = React.useState([46.558993, 15.638081]);

    return (
        <div style={{ display: 'flex', height: '90vh' }}>
            <div style={{ flex: 3 }}>
            <MapContainer center={mariborCoordinates} zoom={16} scrollWheelZoom={true} style={{ height: "90vh" }}>
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                {paths.length > 0 && paths.map((roadPath, index) => (
                    <Polyline key={index} positions={roadPath.path} pathOptions={roadPath.polylineOptions} />
                ))}
            </MapContainer>
            </div>
            <div style={{ flex: 1 }}>
                {data.roadStates ? <GraphComponent data={data.roadStates} /> : <div>No data available</div>}
            </div>
        </div>
    );
}

export default MapComponent;
