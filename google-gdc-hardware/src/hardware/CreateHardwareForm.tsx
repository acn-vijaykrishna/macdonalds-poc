import React from 'react';
import { useForm, SubmitHandler } from 'react-hook-form';
import axios from 'axios';
import { GoogleAuth } from 'google-auth-library';

interface FormData {
  projectId: string;
  locationId: string;
  hardwareId: string;
  // Add other necessary fields here
}

const CreateHardwareForm: React.FC<{ onHardwareCreated: () => void }> = ({ onHardwareCreated }) => {
  const { register, handleSubmit, formState: { errors } } = useForm<FormData>();

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

  const onSubmit: SubmitHandler<FormData> = async (data) => {
    try {
      const token = await getAccessToken();
      const response = await axios.post(`https://edge.googleapis.com/v1alpha/projects/${data.projectId}/locations/${data.locationId}/hardware`, {
        hardwareId: data.hardwareId,
        // Add other necessary fields here
      }, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      console.log(response.data);
      onHardwareCreated();  // Notify parent component to refresh hardware list
    } catch (error) {
      console.error('Error creating hardware:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div>
        <label>Project ID:</label>
        <input type="text" {...register('projectId', { required: true })} />
        {errors.projectId && <p>Project ID is required</p>}
      </div>
      <div>
        <label>Location ID:</label>
        <input type="text" {...register('locationId', { required: true })} />
        {errors.locationId && <p>Location ID is required</p>}
      </div>
      <div>
        <label>Hardware ID:</label>
        <input type="text" {...register('hardwareId', { required: true })} />
        {errors.hardwareId && <p>Hardware ID is required</p>}
      </div>
      {/* Add more input fields as required */}
      <button type="submit">Create Hardware</button>
    </form>
  );
};

export default CreateHardwareForm;
