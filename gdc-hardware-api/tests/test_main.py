import unittest
from flask import Flask
from src.main import app

class TestHardwareManagement(unittest.TestCase):
    def setUp(self):
        self.app = app.test_client()
        self.app.testing = True

    def test_create_hardware(self):
        response = self.app.post('/create_hardware', json={
            'project_id': '001',
            'location': '1001',
            'hardware_body': {
                "name": "example-hardware",
                "displayName": "Example Hardware",
                "createTime": "2024-07-16T12:34:56.789Z",
                "updateTime": "2024-07-16T12:34:56.789Z",
                "labels": {
                    "key": "value"
                },
                "order": "example-order",
                "hardwareGroup": "example-hardware-group",
                "site": "example-site",
                "state": "STATE_UNSPECIFIED",
                "ciqUri": "example-ciq-uri",
                "config": {
                    # Example HardwareConfig object
                },
                "estimatedInstallationDate": {
                    "year": 2024,
                    "month": 7,
                    "day": 16
                },
                "physicalInfo": {
                    # Example HardwarePhysicalInfo object
                },
                "installationInfo": {
                    # Example HardwareInstallationInfo object
                },
                "zone": "example-zone",
                "requestedInstallationDate": {
                    "year": 2024,
                    "month": 7,
                    "day": 16
                },
                "actualInstallationDate": {
                    "year": 2024,
                    "month": 7,
                    "day": 16
                }
            }
        })
        self.assertIn(response.status_code, [200, 201])
        if response.status_code == 200:
            self.assertIn('example-hardware', response.get_json().get('name', ''))

    def test_get_hardware(self):
        response = self.app.get('/get_hardware', query_string={
            'project_id': '001',
            'location': '1001',
            'hardware_id': 'your-hardware-id'
        })
        self.assertEqual(response.status_code, 200)
        self.assertIn('example-hardware', response.get_json().get('name', ''))

if __name__ == "__main__":
    unittest.main()
