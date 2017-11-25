#!/usr/bin/env python

import hug
import sqlalchemy
from sqlalchemy import create_engine, func
from models import Base, Venue, Gig, Band, Recording
from sqlalchemy.orm import sessionmaker
from contextlib import contextmanager
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

@hug.get('/bands', output=hug.output_format.pretty_json)
def venues():
	with session_scope() as session:
		result = session.query(Band.name, Band.img).all()
		return result

@hug.get('/venues', output=hug.output_format.pretty_json)
def venues():
	with session_scope() as session:
		result = session.query(Venue.name, Venue.location_lat, Venue.location_lng, Venue.img).all()
		return result

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

@hug.get('/images', output=hug.output_format.file)
def images(id:int):
	with session_scope() as session:
		venue = session.query(Venue).get(id)
		return os.path.join('data', 'images',f'{venue.img}')

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

