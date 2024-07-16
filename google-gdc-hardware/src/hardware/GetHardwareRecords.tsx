import React, { useState } from 'react';
import axios from 'axios';

interface HardwareRecord {
  name: string;
  locationId: string;
  hardwareId: string;
  // Add other necessary fields here
}

const GetHardwareRecords: React.FC = () => {
  const [records, setRecords] = useState<HardwareRecord[]>([]);
  const [projectId, setProjectId] = useState('');
  const [locationId, setLocationId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchRecords = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get(`http://127.0.0.1:5000/get_hardware`, {
        params: {
          projectId,
          locationId,
        },
      });
      setRecords(response.data.hardwareRecords || []);
    } catch (err) {
      setError('Error fetching records');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>Hardware Records</h1>
      <div>
        <label>Project ID:</label>
        <input type="text" value={projectId} onChange={(e) => setProjectId(e.target.value)} />
      </div>
      <div>
        <label>Location ID:</label>
        <input type="text" value={locationId} onChange={(e) => setLocationId(e.target.value)} />
      </div>
      <button onClick={fetchRecords} disabled={loading}>
        {loading ? 'Loading...' : 'Fetch Records'}
      </button>
      {error && <p>{error}</p>}
      <ul>
        {records.map((record) => (
          <li key={record.hardwareId}>
            <strong>Name:</strong> {record.name}<br />
            <strong>Location ID:</strong> {record.locationId}<br />
            <strong>Hardware ID:</strong> {record.hardwareId}<br />
            {/* Add other necessary fields here */}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default GetHardwareRecords;
