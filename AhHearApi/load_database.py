import sqlalchemy
from sqlalchemy import create_engine, func
from models import Base, Venue, Gig, Band, Recording
from sqlalchemy.orm import sessionmaker
import datetime

engine = create_engine('sqlite:///ahhere.db', echo=False)
Session = sessionmaker(bind=engine)
Base.metadata.create_all(engine)

# Following code populates the database with the csv files.
session = Session()
with open('data/bands.csv', 'rt', encoding='utf-8') as file:
	for line in file:
		_id, name, img = line.strip().split(',')
		band = Band(name=name, img=img)
		session.add(band)

with open('data/venues.csv', 'rt', encoding='utf-8') as file:
	for line in file:
		_id, name, lat, lng, img = line.strip().split(',')
		venue = Venue(name=name, location_lat=lat, location_lng=lng, img=img)
		session.add(venue)

with open('data/gigs.csv', 'rt', encoding='utf-8') as file:
	for line in file:
		inputdatetime, band, venue = line.split(',')
		parsed_datetime = datetime.datetime.strptime(inputdatetime, '%d-%m-%Y %H:%M')
		bandsearch = session.query(Band).filter_by(name = band).first()
		gig = Gig(datetime=parsed_datetime, band_id=bandsearch.id, venue_id=venue)
		session.add(gig)

with open('data/recordings.csv', 'rt', encoding='utf-8') as file:
	for line in file:
		gig_id, spl, xpercent, ypercent = line.split(',')
		recording = Recording(spl=spl, xpercent=xpercent, ypercent=ypercent, gig_id=gig_id)
		session.add(recording)
session.commit()