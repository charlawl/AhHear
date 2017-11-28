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

@hug.get('/venues_list', output=hug.output_format.pretty_json)
def venues_list():
	return get_list_item(Venue)
		
@hug.get('/bands_list', output=hug.output_format.pretty_json)
def bands_list():
	return get_list_item(Band)


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


@hug.get('/recordings', output=hug.output_format.pretty_json)
def recordings():
	session = Session()
	return session.query(Recording.spl, Recording.xpercent, Recording.ypercent, Gig.datetime, Band.name, Venue.name).join(Gig, Band, Venue).all()

@hug.get('/venue_image', output=hug.output_format.file)
def venue_image(id:int):
	with session_scope() as session:
		venue = session.query(Venue).get(id)
		return os.path.join('data','images', venue.img)

@hug.get('/band_image', output=hug.output_format.file)
def band_image(id:int):
	with session_scope() as session:
		band = session.query(Band).get(id)
		return os.path.join('data','images','thumbnails', band.img + ".thumbnail")

@hug.post('/input_recording')
def sample_reading(body):
	with session_scope() as session:
		recording = Recording(spl=body["spl"], xpercent=body["xpercent"], ypercent=body["ypercent"], gig_id=body["gig_id"])
		session.add(recording)
		session.commit()

	return {'status': 'posted!'}
