import hug
import api
from api import sample_reading, venues_list

print(hug.test.post(method=sample_reading, api_or_module=api, body="{'timestamp':200000, 'gig':1}").status)
print(hug.test.get('/venues_list').status)

