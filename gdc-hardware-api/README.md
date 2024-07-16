### Steps to run GDC hardware API wrapper
```
python3 -m venv venv

Activate virtual environment:

windows: .\venv\Scripts\activate
mac: source venv/bin/activate

pip install -r requirements.txt
``` 

Test:
```
python -m unittest discover -s tests
```

Run:
```
python src/main.py
```

### git reset
```
git reset HEAD~1
```

### API Reference Documentation:

REST: https://cloud.google.com/distributed-cloud/edge/latest/docs/reference/hardware/rest
Documentation:
https://cloud.google.com/distributed-cloud/edge/latest/docs/reference/hardware/rest/v1alpha/projects.locations.hardware