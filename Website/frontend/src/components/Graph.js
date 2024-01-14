import React, { useEffect, useRef } from 'react';
import { Chart } from 'chart.js/auto';

const GraphComponent = ({ data }) => {
  const chartRef = useRef(null);

  useEffect(() => {

    // Function to create a new dataset for the Chart
    const createDataset = (label, data, color) => ({
      label,
      data,
      borderColor: color,
      backgroundColor: 'rgba(0, 123, 255, 0.3)',
      borderWidth: 2,
      pointRadius: 0,
      fill: false,
    });

    // Extracting data for X, Y, and Z axes
    const accXData = data.map((state) => state.accX);
    const accYData = data.map((state) => state.accY);
    const accZData = data.map((state) => state.accZ);

    // Chart configuration
    const config = {
      type: 'line',
      data: {
        labels: Array.from({ length: accXData.length }, (_, i) => i + 1),
        datasets: [
          createDataset('AccX', accXData, 'red'),
          createDataset('AccY', accYData, 'green'),
          createDataset('AccZ', accZData, 'blue'),
        ],
      },
      options: {
        scales: {
          x: {
            type: 'linear',
            position: 'bottom',
          },
        },
      },
    };

    // Create the chart
    const ctx = chartRef.current.getContext('2d');
    chartRef.current = new Chart(ctx, config);
  }, [data]);

  return <canvas ref={chartRef} width="400" height="200"></canvas>;
};

export default GraphComponent;
