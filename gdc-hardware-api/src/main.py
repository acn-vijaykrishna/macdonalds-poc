import json
from flask import Flask, request, jsonify
import requests
from google.oauth2 import service_account
from google.auth.transport.requests import AuthorizedSession, Request
from flask_cors import CORS

# Path to your service account key file
SERVICE_ACCOUNT_FILE = 'secrets/secrets.json'

# Google GDC Hardware Management API base URL
BASE_URL = 'https://gdchardwaremanagement.googleapis.com/v1alpha'

# Initialize the credentials and create an authorized session
credentials = service_account.Credentials.from_service_account_file(
    SERVICE_ACCOUNT_FILE,
    scopes=['https://www.googleapis.com/auth/cloud-platform']
)
authed_session = AuthorizedSession(credentials)

app = Flask(__name__)
CORS(app)  # This will enable CORS for all routes

@app.route('/create_hardware', methods=['POST'])
def create_hardware():
    try:
        data = request.json
        print("Incoming request data:", data)  # Debug print

        project_id = data.get('project_id')
        location = data.get('location')
        hardware_body = data.get('hardware_body')

        if not all([project_id, location, hardware_body]):
            return jsonify({'error': 'Missing required fields'}), 400

        url = f"{BASE_URL}/projects/{project_id}/locations/{location}/hardware"
        print("Constructed URL:", url)  # Debug print

        headers = {
            "Authorization": f"Bearer {get_access_token()}",
            "Content-Type": "application/json"
        }

        payload = hardware_body
        print("Payload:", json.dumps(payload, indent=2))  # Debug print

        response = requests.post(url, json=payload, headers=headers)
        print("Response status code:", response.status_code)  # Debug print
        print("Response content:", response.content)  # Debug print

        if response.status_code in [200, 201]:
            return jsonify(response.json()), response.status_code
        else:
            return jsonify({'error': response.text}), response.status_code
    except Exception as e:
        print("Exception occurred:", str(e))  # Debug print
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/get_hardware', methods=['GET'])
def get_hardware():
    project_id = request.args.get('project_id')
    location = request.args.get('location')
    hardware_id = request.args.get('hardware_id')

    url = f"{BASE_URL}/projects/{project_id}/locations/{location}/hardware/{hardware_id}"
    headers = {
        "Authorization": f"Bearer {get_access_token()}"
    }
    response = requests.get(url, headers=headers)
    print("response ==>", response)
    if response.status_code == 200:
        return jsonify(response.json()), 200
    else:
        return jsonify({'error': response.text}), response.status_code
    

def get_access_token():
    credentials = service_account.Credentials.from_service_account_file(
        SERVICE_ACCOUNT_FILE,
        scopes=["https://www.googleapis.com/auth/cloud-platform"],
    )
    request_adapter = Request()
    credentials.refresh(request_adapter)
    return credentials.token

if __name__ == "__main__":
    app.run(debug=True)
