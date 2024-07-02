# **mcd-serviceM-api**

## **Description**
This app hosts API that would be used to push and read data from ServiceM.

Current API hosted:
- POST /loyalty
    This REST endpoint can be used to send formatted loyalty data to ServiceM.
  - Format:
    - Request:
      ```json
      {
        "inputMessage" : "stringValue"
      }
      ```

## **Launching the app**
- The jar has been uploaded to "ec2-jar-holding" S3 bucket
- To copy the jar to the EC2 instance perform the following steps:
  - Connect to the EC2 instance using the SSM
  - Navigate to the directory where the jar needs to be copied (current jar is in /opt/mcd-serviceM-jar/mcd.jar)
  - Use the curl command with the presigned url of the jar in s3 to copy the jar to the EC2 instance
    ```bash
    curl -o mcd.jar <presigned-url>
    ```
- To launch the app perform the following steps:
  - Navigate to the directory where the jar is copied
  - Run the following command to launch the app
    ```bash
    nohup java -jar mcd.jar &
    ```
  - Running the app in nohup will ensure it keeps running in the background even after the SSM session terminates
- To check if the app is running, use the following command
  ```bash
  ps -ef | grep mcd.jar
  ```
- To stop the app, use the following command
  ```bash
    kill -9 <pid>
    ```

## **Connecting to the app**
- The app is hosted on port 8080
- To connect to the app, use the following URL
  ```bash
  curl --header "Content-Type: application/json" --request POST --data '{"inputMessage":"serviceM input"}' http://<ec2-public-ip>:8080/loyalty
  ```

    
