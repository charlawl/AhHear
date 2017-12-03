from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, Integer, String, Float, ForeignKey, DateTime
from sqlalchemy.orm import relationship

Base = declarative_base()

# Below is the code which instantiates the schema in the database. There is a table for Bands, Venues, Gigs and 
# Recordings
class Band(Base):
	__tablename__ = 'band'
	id = Column(Integer, primary_key=True)
	name = Column(String, nullable=False)
	img = Column(String, nullable=False)
	gigs = relationship('Gig', back_populates='band')

class Venue(Base):
	__tablename__ = 'venue'
	id = Column(Integer, primary_key=True)
	name = Column(String, nullable=False)
	img = Column(String, nullable=False)
	location_lat = Column(Float, nullable=False)
	location_lng = Column(Float, nullable=False)
	gigs = relationship('Gig', back_populates='venue')

class Gig(Base):
	__tablename__ = 'gig'
	id = Column(Integer, primary_key=True)
	datetime = Column(DateTime, nullable=False)
	venue_id = Column(Integer, ForeignKey('venue.id'))
	venue = relationship('Venue', back_populates='gigs')
	band_id = Column(Integer, ForeignKey('band.id'))
	band = relationship('Band', back_populates='gigs')
	recordings = relationship('Recording', back_populates='gig')

class Recording(Base):
	__tablename__ = 'recording'
	id = Column(Integer, primary_key=True)
	spl = Column(Float, nullable=False)
	xpercent = Column(Float, nullable=False)
	ypercent = Column(Float, nullable=False)
	gig_id = Column(Integer, ForeignKey('gig.id'))
	gig = relationship('Gig', back_populates='recordings')
