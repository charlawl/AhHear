import json
import csv 
import requests

gigs = json.load(open("data/gigs.json", "rt", encoding='utf-8'))
bands = list(set(gig['band'] for gig in gigs))
venues = json.load(open("data/venues.json", "rt", encoding='utf-8'))
venue_list = [venue['name'] for venue in venues]

with open('data/bands.csv', 'w', encoding='utf-8') as csv_file:
	writer = csv.writer(csv_file, delimiter=',')
	for i, band in enumerate(bands):
		writer.writerow([i, band])

with open('data/venues.csv', 'w', encoding='utf-8') as csv_file:
	writer = csv.writer(csv_file, delimiter=',')
	for i, venue in enumerate(venues):
		writer.writerow([i, venue['name'], venue['lat'], venue['long']])

with open('data/gigs.csv', 'w', encoding='utf-8') as csv_file:
	writer = csv.writer(csv_file, delimiter=',')
	for gig in gigs:
		writer.writerow([gig['date'], gig['time'], bands.index(gig['band']), venue_list.index(gig['venue'])])

