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
# 		inputdatetime, band, venue = line.split(',')
# 		parsed_datetime = datetime.datetime.strptime(inputdatetime, '%d-%m-%Y %H:%M')
# 		bandsearch = session.query(Band).filter_by(name = band).first()
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
	query_a = session.query(cls.id.label('id'),
							   cls.name,
							   cls.img,
							   func.count(Sample.id).label('num_samples'),
							   func.avg(Sample.decibels).label('avg_samples')).outerjoin(Gig, Sample).group_by(cls.id).subquery()

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
	return os.path.join('data', 'images',f'{item.img}')

@hug.get('/venues_list', output=hug.output_format.pretty_json)
def venues_list():
	with session_scope() as session:
		return get_list_item(Venue)
		
@hug.get('/bands_list', output=hug.output_format.pretty_json)
def bands_list():
	session = Session()
	return get_list_item(Band)

@hug.get('/gigs', output=hug.output_format.pretty_json)
def gigs():
	session = Session()
	return session.query(Gig.datetime, Band.name, Band.img, Venue.name, Venue.img, Venue.location_lat, Venue.location_lng).join(Venue,Band).all()

@hug.get('/recordings', output=hug.output_format.pretty_json)
def recordings():
	session = Session()
	return session.query(Recording.spl, Recording.xpercent, Recording.ypercent, Gig.datetime, Band.name, Venue.name).join(Gig, Band, Venue).all()

@hug.get('/input_recording', output=hug.output_format.pretty_json)
def input_recording(spl:float, xpercent:float, ypercent:float, gig_id:int):
	session = Session()
	recording = Recording(spl=spl, xpercent=xpercent, ypercent=ypercent, gig_id=gig_id)
	session.add(recording)
	session.commit()
	return True

@hug.get('/venue_image', output=hug.output_format.file)
def venue_image(id:int):
	with session_scope() as session:
		venue = session.query(Venue).get(id)
		return get_image(venue)

@hug.get('/band_image', output=hug.output_format.file)
def band_image(id:int):
	with session_scope() as session:
		band = session.query(Band).get(id)
		return get_image(band)

@hug.post('/sample_reading')
def sample_reading(data):
	with session_scope() as session:
		sample = Sample(timestamp=data['timestamp'], gig=session.query(Gig).get(data['gig']))
		session.add(sample)
		session.commit()
  

