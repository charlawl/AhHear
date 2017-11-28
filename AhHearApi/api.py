#!/usr/bin/env python

import hug
import sqlalchemy
from sqlalchemy import create_engine, func
from models import Base, Venue, Gig, Band, Recording
from sqlalchemy.orm import sessionmaker
from contextlib import contextmanager
import random
import time
import os
import json
import datetime

engine = create_engine('sqlite:///ahhere.db', echo=False)
Session = sessionmaker(bind=engine)
Base.metadata.create_all(engine)


# Following code populates the database with the csv files.
# session = Session()
# with open('data/bands.csv', 'rt', encoding='utf-8') as file:
# 	for line in file:
# 		_id, name, img = line.strip().split(',')
# 		band = Band(name=name, img=img)
# 		session.add(band)

# with open('data/venues.csv', 'rt', encoding='utf-8') as file:
# 	for line in file:
# 		_id, name, lat, lng, img = line.strip().split(',')
# 		venue = Venue(name=name, location_lat=lat, location_lng=lng, img=img)
# 		session.add(venue)

# with open('data/gigs.csv', 'rt', encoding='utf-8') as file:
# 	for line in file:
# 		inputdatetime, name, venue = line.split(',')
# 		parsed_datetime = datetime.datetime.strptime(inputdatetime, '%d-%m-%Y %H:%M')
# 		bandsearch = session.query(Band).filter_by(name = name).first()
# 		gig = Gig(datetime=parsed_datetime, band_id=bandsearch.id, venue_id=venue)
# 		session.add(gig)

# with open('data/recordings.csv', 'rt', encoding='utf-8') as file:
# 	for line in file:
# 		gig_id, spl, xpercent, ypercent = line.split(',')
# 		recording = Recording(spl=spl, xpercent=xpercent, ypercent=ypercent, gig_id=gig_id)
# 		session.add(recording)
# session.commit()


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

def get_list_item(cls):

	with session_scope() as session:
		query_a = session.query(cls.id.label('id'),
								   cls.name,
								   cls.img,
								   func.count(Recording.id).label('num_samples'),
								   func.avg(Recording.spl).label('avg_samples')).outerjoin(Gig, Recording).group_by(cls.id).subquery()

		query_b = session.query(cls.id.label('id'),
								func.count(Gig.id).label('num_gigs')).outerjoin(Gig).group_by(cls.id).subquery()

		final_query = session.query(query_a.c.id,
									query_a.c.name,
									query_a.c.img,
									query_a.c.num_samples,
									query_a.c.avg_samples,
									query_b.c.num_gigs).outerjoin(query_b, query_a.c.id==query_b.c.id).all()

	return [row._asdict() for row in final_query]

def get_image(item):
	return os.path.join('data','images',item.img)

@hug.get('/venues_list', output=hug.output_format.pretty_json)
def venues_list():
	return get_list_item(Venue)
		
@hug.get('/bands_list', output=hug.output_format.pretty_json)
def bands_list():
	return get_list_item(Band)

@hug.get('/gigs', output=hug.output_format.pretty_json)
def gigs():
	session = Session()
	return session.query(Gig.datetime, Band.name, Band.img, Venue.name, Venue.img, Venue.location_lat, Venue.location_lng).join(Venue,Band).all()

@hug.get('/recordings', output=hug.output_format.pretty_json)
def recordings():
	session = Session()
	# return session.query(Recording.spl, Recording.xpercent, Recording.ypercent, Gig.datetime, Band.name, Venue.name).join(Gig, Band, Venue).all()
	return session.query(Recording.spl, Gig.id, Recording.xpercent, Recording.ypercent, Gig.datetime, Band.name, Venue.name).join(Gig, Band, Venue).all()

@hug.get('/gig_recordings', output=hug.output_format.pretty_json)
def recordings(gig_id:int):
	session = Session()
	# return session.query(Recording.spl, Recording.xpercent, Recording.ypercent, Gig.datetime, Band.name, Venue.name).join(Gig, Band, Venue).all()
	result = session.query(Recording.spl, func.avg(Recording.spl).label('avg_samples'), func.count(Recording.id).label('num_samples'), Gig.datetime, Gig.id.label("gig_id"), Band.id.label("band_id"), Venue.id.label("venue_id"), Recording.xpercent, Recording.ypercent, Gig.datetime, Band.name.label('band_name'), Venue.name.label('venue_name')).join(Gig, Band, Venue).filter(Gig.id == gig_id).all()
	return ([item._asdict() for item in result])

@hug.get('/venue_image', output=hug.output_format.file)
def venue_image(id:int):
	with session_scope() as session:
		venue = session.query(Venue).get(id)
		return get_image(venue)

@hug.get('/floorplan_image', output=hug.output_format.file)
def band_image(id:int):
	with session_scope() as session:
		venue = session.query(Venue).get(id)
		venue_name = venue.img.split(".")[0]
		floorplan_path = os.path.join('data','images', venue_name + "_floorplan.png")
		return floorplan_path

@hug.get('/band_image', output=hug.output_format.file)
def band_image(id:int):
	with session_scope() as session:
		band = session.query(Band).get(id)
		return get_image(band)

# http://localhost:8000/input_recording?spl=14&xpercent=15&ypercent=16&gig_id=1
@hug.post('/input_recording')
def sample_reading(body):

	with session_scope() as session:
		recording = Recording(spl=body["spl"], xpercent=body["xpercent"], ypercent=body["ypercent"], gig_id=body["gig_id"])
		session.add(recording)
		session.commit()

	return {'status': 'posted!'}
