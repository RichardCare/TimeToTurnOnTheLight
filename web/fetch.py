# Fetches json files from one or more connected machines (RasPi's) - see last.py
# Note: On a RasPi run last.py using:
#		pi@raspberrypi ~/TimeToTurnOnTheLight $ while [ 1 ]; do ./last.py ; sleep 2; done
# Each json file has the form:
#	{"team3": 2580.0, "pi": 3809.0, "reboot": 131940.0, "team2": 4500.0}
# where:
#	all possible team names on the RasPi are listed with cumulative logged in time for each.
#
# Merges them into a single activity.jsonp file in the same format prefixed by a variable name
# for consumption by an HTML5 page - see analogue_clock.html/.js

import os
import json
import time

# password!		user@host			path from ~
#remotes = [\
#('raspberry', 'pi@192.168.2.103', 'TimeToTurnOnTheLight/activity.jsonp'),\
#('raspberry', 'pi@192.168.2.104', 'TimeToTurnOnTheLight/activity.jsonp')]
remotes = [\
('raspberry', 'pi@192.168.2.103', 'TimeToTurnOnTheLight/activity.jsonp')]

while True:
	time.sleep(2)
	activities = dict()

	# Fetch the machines' activity file
	for idx, remote in enumerate(remotes):
		password = remote[0]
		localfile = 'activity_%s.tmp' % (idx) 
		remotehost = remote[1]
		remotefile = remote[2]
		#cmd = 'scp -pw "%s" "%s" "%s:%s"' % (password, localfile, remotehost, remotefile)
		cmd = '"C:\Program Files (x86)\putty\pscp" -pw %s %s:%s %s' % (password, remotehost, remotefile, localfile)
		print cmd
		os.system(cmd)
		fp = open(localfile, 'r')
		aMachine = json.load(fp)
		fp.close()
		
		for team in aMachine.keys():
			if team in activities:
				activities[team] = activities[team] + aMachine[team]
			else:
				activities[team] = aMachine[team]

	fp = open('activity.jsonp', 'w')
	fp.write('activities=')
	json.dump(activities, fp)
	fp.close()

