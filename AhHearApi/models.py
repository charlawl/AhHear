from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, Integer, String, Float, ForeignKey
from sqlalchemy.orm import relationship

Base = declarative_base()

class Venue(Base):
	__tablename__ = 'venue'
	id = Column(Integer, primary_key=True)
	name = Column(String)
	location_lat = Column(Float)
	location_lng = Column(Float)
	img = Column(String)
	gigs = relationship('Gig', back_populates='venue')


class Band(Base):
	__tablename__ = 'band'
	id = Column(Integer, primary_key=True)
	name = Column(String)
	img = Column(String)
	gigs = relationship('Gig', back_populates='band')

class Gig(Base):
	__tablename__ = 'gig'
	id = Column(Integer, primary_key=True)
	date = Column(String)
	time = Column(String)
	venue_id = Column(Integer, ForeignKey('venue.id'))
	venue = relationship('Venue', back_populates='gigs')
	band_id = Column(Integer, ForeignKey('band.id'))
	band = relationship('Band', back_populates='gigs')
	samples =  relationship('Sample')


class Sample(Base):
	__tablename__ = 'sample'
	id = Column(Integer, primary_key=True)
	timestamp = Column(Integer)
	decibels = Column(Integer)
	gig = Column(Integer, ForeignKey('gig.id'))
