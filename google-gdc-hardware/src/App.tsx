import React, { useState } from 'react';
import CreateHardwareForm from './hardware/CreateHardwareForm';
import GetHardwareRecords from './hardware/GetHardwareRecords';

const App: React.FC = () => {
  const [refresh, setRefresh] = useState(false);

  const handleHardwareCreated = () => {
    setRefresh(!refresh); // Toggle the refresh state to force re-fetching records
  };

  return (
    <div>
      <h1>Google GDC Hardware Management</h1>
      <CreateHardwareForm onHardwareCreated={handleHardwareCreated} />
      <GetHardwareRecords key={refresh.toString()} /> {/* Key prop to force re-mount */}
    </div>
  );
};

export default App;
