import React, { useState } from 'react';
import axios from 'axios';
import { GoogleAuth } from 'google-auth-library';

interface HardwareRecord {
  // Define the structure of the hardware record based on the API response
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

  const getAccessToken = async (): Promise<string> => {
    const auth = new GoogleAuth({
      scopes: 'https://www.googleapis.com/auth/cloud-platform'
    });
    const client = await auth.getClient();
    const tokenResponse = await client.getAccessToken();
    if (tokenResponse.token) {
      return tokenResponse.token;
    } else {
      throw new Error('Failed to obtain access token');
    }
  };

  const fetchRecords = async () => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAccessToken();
      const response = await axios.get(`https://edge.googleapis.com/v1alpha/projects/${projectId}/locations/${locationId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
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
