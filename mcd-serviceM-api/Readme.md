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

