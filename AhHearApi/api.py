#!/usr/bin/env python

import hug
import sqlalchemy
from sqlalchemy import create_engine, func
from models import Base, Venue, Gig, Band
from sqlalchemy.orm import sessionmaker
from contextlib import contextmanager
import os

engine = create_engine('sqlite:///:memory:', echo=False)
Session = sessionmaker(bind=engine)
Base.metadata.create_all(engine)

session = Session()

with open('data/venues.csv', 'rt', encoding='utf-8') as file:
	for line in file:
		_id, name, lat, lng, img = line.strip().split(',')
		print(img)
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
		print(venue.img)
		return os.path.join('data', 'images',f'{venue.img}')


