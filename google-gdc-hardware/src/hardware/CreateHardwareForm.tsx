import React from 'react';
import { useForm, SubmitHandler } from 'react-hook-form';
import axios from 'axios';

interface DateField {
  year: number;
  month: number;
  day: number;
}

interface FormData {
  projectId: string;
  locationId: string;
  hardwareId: string;
  name: string;
  displayName: string;
  createTime: string;
  updateTime: string;
  labels: Record<string, string>;
  order: string;
  hardwareGroup: string;
  site: string;
  state: string;
  ciqUri: string;
  config: Record<string, any>;
  estimatedInstallationDate: DateField;
  physicalInfo: Record<string, any>;
  installationInfo: Record<string, any>;
  zone: string;
  requestedInstallationDate: DateField;
  actualInstallationDate: DateField;
}

const defaultValues: FormData = {
  projectId: '001',
  locationId: '001',
  hardwareId: '101',
  name: 'example-hardware',
  displayName: 'Example Hardware',
  createTime: new Date().toISOString(),
  updateTime: new Date().toISOString(),
  labels: { exampleLabel: 'exampleValue' },
  order: 'example-order',
  hardwareGroup: 'example-group',
  site: 'example-site',
  state: 'ACTIVE',
  ciqUri: 'example-uri',
  config: { key: 'value' },
  estimatedInstallationDate: { year: 2024, month: 7, day: 16 },
  physicalInfo: { info: 'example info' },
  installationInfo: { info: 'example installation info' },
  zone: 'example-zone',
  requestedInstallationDate: { year: 2024, month: 7, day: 20 },
  actualInstallationDate: { year: 2024, month: 7, day: 25 },
};

const CreateHardwareForm: React.FC<{ onHardwareCreated: () => void }> = ({ onHardwareCreated }) => {
  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    defaultValues,
  });

  const onSubmit: SubmitHandler<FormData> = async (data) => {
    try {
      const response = await axios.post('http://127.0.0.1:5000/create_hardware', {
        project_id: data.projectId,
        location: data.locationId,
        hardware_id: data.hardwareId,
        hardware_body: {
          name: data.name,
          displayName: data.displayName,
          createTime: data.createTime,
          updateTime: data.updateTime,
          labels: data.labels,
          order: data.order,
          hardwareGroup: data.hardwareGroup,
          site: data.site,
          state: data.state,
          ciqUri: data.ciqUri,
          config: data.config,
          estimatedInstallationDate: data.estimatedInstallationDate,
          physicalInfo: data.physicalInfo,
          installationInfo: data.installationInfo,
          zone: data.zone,
          requestedInstallationDate: data.requestedInstallationDate,
          actualInstallationDate: data.actualInstallationDate,
        },
      });
      console.log(response.data);
      onHardwareCreated(); // Notify parent component to refresh hardware list
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
      <div>
        <label>Name:</label>
        <input type="text" {...register('name', { required: true })} />
        {errors.name && <p>Name is required</p>}
      </div>
      <div>
        <label>Display Name:</label>
        <input type="text" {...register('displayName', { required: true })} />
        {errors.displayName && <p>Display Name is required</p>}
      </div>
      <div>
        <label>Create Time:</label>
        <input type="text" {...register('createTime', { required: true })} />
        {errors.createTime && <p>Create Time is required</p>}
      </div>
      <div>
        <label>Update Time:</label>
        <input type="text" {...register('updateTime', { required: true })} />
        {errors.updateTime && <p>Update Time is required</p>}
      </div>
      <div>
        <label>Labels (key:value):</label>
        <input type="text" {...register('labels', { required: true })} />
        {errors.labels && <p>Labels are required</p>}
      </div>
      <div>
        <label>Order:</label>
        <input type="text" {...register('order', { required: true })} />
        {errors.order && <p>Order is required</p>}
      </div>
      <div>
        <label>Hardware Group:</label>
        <input type="text" {...register('hardwareGroup', { required: true })} />
        {errors.hardwareGroup && <p>Hardware Group is required</p>}
      </div>
      <div>
        <label>Site:</label>
        <input type="text" {...register('site', { required: true })} />
        {errors.site && <p>Site is required</p>}
      </div>
      <div>
        <label>State:</label>
        <input type="text" {...register('state', { required: true })} />
        {errors.state && <p>State is required</p>}
      </div>
      <div>
        <label>CIQ URI:</label>
        <input type="text" {...register('ciqUri', { required: true })} />
        {errors.ciqUri && <p>CIQ URI is required</p>}
      </div>
      <div>
        <label>Config (JSON):</label>
        <textarea {...register('config', { required: true })}></textarea>
        {errors.config && <p>Config is required</p>}
      </div>
      <div>
        <label>Estimated Installation Date (YYYY-MM-DD):</label>
        <input type="text" {...register('estimatedInstallationDate', { required: true })} />
        {errors.estimatedInstallationDate && <p>Estimated Installation Date is required</p>}
      </div>
      <div>
        <label>Physical Info (JSON):</label>
        <textarea {...register('physicalInfo', { required: true })}></textarea>
        {errors.physicalInfo && <p>Physical Info is required</p>}
      </div>
      <div>
        <label>Installation Info (JSON):</label>
        <textarea {...register('installationInfo', { required: true })}></textarea>
        {errors.installationInfo && <p>Installation Info is required</p>}
      </div>
      <div>
        <label>Zone:</label>
        <input type="text" {...register('zone', { required: true })} />
        {errors.zone && <p>Zone is required</p>}
      </div>
      <div>
        <label>Requested Installation Date (YYYY-MM-DD):</label>
        <input type="text" {...register('requestedInstallationDate', { required: true })} />
        {errors.requestedInstallationDate && <p>Requested Installation Date is required</p>}
      </div>
      <div>
        <label>Actual Installation Date (YYYY-MM-DD):</label>
        <input type="text" {...register('actualInstallationDate', { required: true })} />
        {errors.actualInstallationDate && <p>Actual Installation Date is required</p>}
      </div>
      <button type="submit">Create Hardware</button>
    </form>
  );
};

export default CreateHardwareForm;
