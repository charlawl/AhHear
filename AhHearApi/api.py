#!/usr/bin/env python

import hug
import sqlalchemy
from sqlalchemy import create_engine, func
from models import Base, Venue, Gig, Band, Heatmap
from sqlalchemy.orm import sessionmaker
from contextlib import contextmanager
import os
import json

engine = create_engine('sqlite:///:memory:', echo=False)
Session = sessionmaker(bind=engine)
Base.metadata.create_all(engine)

session = Session()

with open('data/venues.csv', 'rt', encoding='utf-8') as file:
	for line in file:
		_id, name, lat, lng, img = line.strip().split(',')
		# print(img)
		venue = Venue(name=name, location_lat=lat, location_lng=lng, img=img)
		session.add(venue)

with open('data/bands.csv', 'rt', encoding='utf-8') as file:
	for line in file:
		_id, name, img = line.strip().split(',')
		band = Band(name=name)
		session.add(band)

with open('data/gigs.csv', 'rt', encoding='utf-8') as file:
	for line in file:
		date, time, band, venue = line.split(',')
		gig = Gig(date=date, time=time)
		venue = session.query(Venue).get(int(venue) + 1)
		band = session.query(Band).get(int(band) + 1)
		venue.gigs.append(gig)
		band.gigs.append(gig)
		session.add(gig)

json_file = 'data/heatmaps.json'

heatmaps = json.load(open(json_file))
for i in heatmaps:
	heatmap_array = str(i['heatmap_array'])
	gig_id = i['gig_id']
	heatmap = Heatmap(gig_id=gig_id, heatmap_array=heatmap_array)
	band = session.query(Gig).get(int(gig_id))
	session.add(heatmap)

session.commit()

@contextmanager
def session_scope():
	session = Session()
	try:
		yield session
		session.commit()
	except:
		session.rollback()
		raise 
	finally:
		session.close()

# this endpoint is a hack and we need to resolve how to do this properly.
# talk to charlotte about the structure of sqlalchemy models.
# http://localhost:8000/heatmap?gig_id=1
@hug.get('/heatmap', output=hug.output_format.pretty_json)
def heatmap(gig_id):
	session = Session()
	heatmap = session.query(Heatmap).get(gig_id)
	return json.loads(heatmap.heatmap_array)

@hug.get('/venues_list', output=hug.output_format.pretty_json)
def venues_list():
	with session_scope() as session:
		result = session.query(Venue.id, 
							   Venue.name,
							   Venue.img,
							   func.count(Venue.gigs),
							   sqlalchemy.sql.expression.literal_column("0").label("numGigs"),
							   sqlalchemy.sql.expression.literal_column("0").label("numSamples"),
							   sqlalchemy.sql.expression.literal_column("0").label("decibels")

							   ).join(Gig).group_by(Venue.id)
		return [item._asdict() for item in result]

@hug.get('/bands', output=hug.output_format.pretty_json)
def bands():
	session = Session()
	return session.query(Band.name)

@hug.get('/gigs', output=hug.output_format.pretty_json)
def gigs():
	session = Session()
	return session.query(Gig.date, Gig.time, Venue.name, Band.name).join(Venue,Band)

@hug.get('/images', output=hug.output_format.file)
def images(id:int):
	with session_scope() as session:
		venue = session.query(Venue).get(id)
		# print(venue.img)
		return os.path.join('data', 'images',f'{venue.img}')


