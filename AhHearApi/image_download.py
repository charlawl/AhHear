import requests
from models import Base, Band
from sqlalchemy.orm import sessionmaker
from sqlalchemy import create_engine
import http.client, urllib.parse, json
import shutil
import os

engine = create_engine('sqlite:///ahhere.db', echo=False)
Session = sessionmaker(bind=engine)
Base.metadata.create_all(engine)

# code to scrape the BING image search API for all the thumnail images seen 

subscriptionKey = "91cdb1b08a1d40a7825c612d9be7ce63"

host = "api.cognitive.microsoft.com"
path = "/bing/v7.0/images/search"


def BingWebSearch(search):
    """Performs a Bing Web search and returns the results."""

    headers = {'Ocp-Apim-Subscription-Key': subscriptionKey}
    conn = http.client.HTTPSConnection(host)
    query = urllib.parse.quote(search)
    conn.request("GET", path + "?q=" + query, headers=headers)
    response = conn.getresponse()
    headers = [k + ": " + v for (k, v) in response.getheaders()
                   if k.startswith("BingAPIs-") or k.startswith("X-MSEdge-")]
    return headers, response.read().decode("utf8")

images = {}
session = Session()

for band in session.query(Band).all():
	if len(subscriptionKey) == 32:

		print('Searching the Web for: ', bytes(band.name, encoding='UTF-8'))

		headers, result = BingWebSearch(bytes(band.name, encoding='UTF-8'))
		print("\nRelevant HTTP Headers:\n")
		print("\n".join(headers))
		print("\nJSON Response:\n")
		data = json.loads(result)
		image = data["value"][0]["contentUrl"]
		fmt = data["value"][0]["encodingFormat"]

		r = requests.get(image, stream=True)
		fname = "".join(band.name.split()) + "." + fmt
		with open(os.path.join("data", "images", fname), 'wb') as f:
			r.raw.decode_content = True
			shutil.copyfileobj(r.raw, f)
		band.img = fname
		session.add(band)
		session.commit()

	else:
		print("Invalid Bing Search API subscription key!")
		print("Please paste yours into the source code.")


# converting images to thumbnail size
from PIL import Image
import glob

for f in glob.glob('./data/images/*.jpeg'):
	im = Image.open(f)
	jpg_im = im.convert('RGB')
	jpg_im.thumbnail((100,100))
	jpg_im.save(f + ".thumbnail", "JPEG")
	


	
	



