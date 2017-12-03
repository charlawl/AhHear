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

# A function which query's the appropriate table given a table as a parameter
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

# Uses the get_list_item function to query the venues table to return a list of venues
@hug.get('/venues_list', output=hug.output_format.pretty_json)
def venues_list():
	return get_list_item(Venue)

# Uses the get_list_item function to query the bands table to return a list of venues
@hug.get('/bands_list', output=hug.output_format.pretty_json)
def bands_list():
	return get_list_item(Band)

# Returns the details of a single gig when a gig_id is provided
@hug.get('/single_gig', output=hug.output_format.pretty_json)
def bands_list(gig_id:int):
	session = Session()
	result = session.query(Gig.datetime.label("gig_date"), Band.name.label("band_name"), Venue.name.label("venue_name"), Venue.id.label("venue_id")).join(Band, Venue).filter(Gig.id == gig_id).all()
	return [row._asdict() for row in result]

# returns a list of all gigs if no ids passed, if a venue id is passed it returns a list of gigs for that venue, 
# and if a band id is passed. The queries are divided out into subqueries which are then joined in the variable
# final query on line 99. 
@hug.get('/gigs', output=hug.output_format.pretty_json)
def gigs(venue: int=None, band: int=None):
	session = Session()
	if venue:
		query_a = session.query(Gig.id.label('gig_id'),
							   	func.count(Recording.id).label('num_samples'),
							   	func.avg(Recording.spl).label('avg_samples')).outerjoin(Recording,Venue).filter(Gig.venue_id == venue).group_by(Gig.id).subquery()

		query_b = session.query(Gig.id.label('gig_id'),
							   	Venue.name.label("venue_name"),
							   	Venue.id.label("venue_id"),
							   	Venue.img.label("venue_img")).outerjoin(Venue).filter(Gig.venue_id == venue).group_by(Gig.id).subquery()

		query_c = session.query(query_a.c.gig_id,
								query_a.c.num_samples,
								query_a.c.avg_samples,
								query_b.c.venue_name,
								query_b.c.venue_id).outerjoin(query_b, query_a.c.gig_id==query_b.c.gig_id).subquery()

		query_d = session.query(Gig.id.label("gig_id"), 
								Gig.datetime.label("time"), 
								Band.name.label("band_name"), 
								Band.id.label("band_id"),
								Band.img.label("img")).outerjoin(Band).subquery()

		final_query = session.query(query_c.c.gig_id,
								query_c.c.num_samples,
								query_c.c.avg_samples,
								query_c.c.venue_name,
								query_c.c.venue_id,
								query_d.c.band_name,
								query_d.c.band_id,
								query_d.c.time).outerjoin(query_d, query_c.c.gig_id==query_d.c.gig_id).all()
		return [row._asdict() for row in final_query]
	elif band:
		query_a = session.query(Gig.id.label('gig_id'),
							   	func.count(Recording.id).label('num_samples'),
							   	func.avg(Recording.spl).label('avg_samples')).outerjoin(Recording,Venue).filter(Gig.band_id == band).group_by(Gig.id).subquery()

		query_b = session.query(Gig.id.label('gig_id'),
							   	Venue.name.label("venue_name"),
							   	Venue.id.label("venue_id"),
							   	Venue.img.label("venue_img")).outerjoin(Venue).group_by(Gig.id).subquery()

		query_c = session.query(query_a.c.gig_id,
								query_a.c.num_samples,
								query_a.c.avg_samples,
								query_b.c.venue_name,
								query_b.c.venue_id).outerjoin(query_b, query_a.c.gig_id==query_b.c.gig_id).subquery()

		query_d = session.query(Gig.id.label("gig_id"), 
								Gig.datetime.label("time"), 
								Band.name.label("band_name"), 
								Band.id.label("band_id"),
								Band.img.label("img")).outerjoin(Band).subquery()

		final_query = session.query(query_c.c.gig_id,
								query_c.c.num_samples,
								query_c.c.avg_samples,
								query_c.c.venue_name,
								query_c.c.venue_id,
								query_d.c.band_name,
								query_d.c.band_id,
								query_d.c.time).outerjoin(query_d, query_c.c.gig_id==query_d.c.gig_id).all()
		return [row._asdict() for row in final_query]
	else:
		return session.query(Gig.datetime, Band.name, Band.img, Venue.name, Venue.img, Venue.location_lat, Venue.location_lng).join(Venue,Band).all()


@hug.get('/todays_gigs', output=hug.output_format.pretty_json)
def todays_gigs():
	session = Session()

	query_a = session.query(Band.id.label("band_id"), 
							Band.name.label("band_name"), 
							Band.img.label("band_img"),
							Gig.id.label("gig_id"), 
							Gig.datetime).join(Gig).filter(Gig.datetime+datetime.timedelta(days=1)<datetime.datetime.now()).subquery()
	
	query_b = session.query(Gig.id.label('gig_id'),
							   	Venue.name.label("venue_name"),
							   	Venue.id.label("venue_id"),
							   	Venue.img.label("venue_img")).outerjoin(Venue).group_by(Gig.id).subquery()

	final_query = session.query(query_a.c.gig_id,
							query_b.c.venue_name,
							query_b.c.venue_id,
							query_b.c.venue_img,
							query_a.c.band_name,
							query_a.c.band_id,
							query_a.c.band_img).outerjoin(query_b, query_a.c.gig_id==query_b.c.gig_id).all()
	
	return [row._asdict() for row in final_query]

@hug.get('/recordings', output=hug.output_format.pretty_json)
def recordings():
	session = Session()
	# return session.query(Recording.spl, Recording.xpercent, Recording.ypercent, Gig.datetime, Band.name, Venue.name).join(Gig, Band, Venue).all()
	return session.query(Recording.spl, Gig.id, Recording.xpercent, Recording.ypercent, Gig.datetime, Band.name, Venue.name).join(Gig, Band, Venue).all()

@hug.get('/gig_recordings', output=hug.output_format.pretty_json)
def recordings(gig_id:int):
	session = Session()
	# return session.query(Recording.spl, Recording.xpercent, Recording.ypercent, Gig.datetime, Band.name, Venue.name).join(Gig, Band, Venue).all()
	result = session.query(Recording.spl, Gig.datetime, Gig.id.label("gig_id"), Band.id.label("band_id"), Venue.id.label("venue_id"), Recording.xpercent, Recording.ypercent, Gig.datetime, Band.name.label('band_name'), Venue.name.label('venue_name')).join(Gig, Band, Venue).filter(Gig.id == gig_id).all()
	resultfuncs = session.query(func.avg(Recording.spl).label('avg_samples'), func.count(Recording.id).label('num_samples'), Gig.id).join(Gig).filter(Gig.id == gig_id).all()
	result = ([item._asdict() for item in result])
	resultfuncs = ([item._asdict() for item in resultfuncs])
	for i in result:
		for j in resultfuncs:
			i["avg_samples"] = j["avg_samples"]
			i["num_samples"] = j["num_samples"]
	return result

@hug.get('/venue_image', output=hug.output_format.file)
def venue_image(id:int):
	with session_scope() as session:
		venue = session.query(Venue).get(id)
		return os.path.join('data','images', venue.img)

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
		return os.path.join('data','images','thumbnails', band.img + ".thumbnail")

# http://localhost:8000/input_recording?spl=14&xpercent=15&ypercent=16&gig_id=1
@hug.post('/input_recording')
def sample_reading(body):
	with session_scope() as session:
		recording = Recording(spl=body["spl"], xpercent=body["xpercent"], ypercent=body["ypercent"], gig_id=body["gig_id"])
		session.add(recording)
		session.commit()

	return {'status': 'posted!'}

