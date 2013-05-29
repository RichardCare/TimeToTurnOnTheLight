#! /usr/bin/python
#
# Get login times, parse & accumulate them.
#
# last output formatted as...
#          1         2         3         4         5         6         7         8         9
#0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
#reboot   system boot  3.2.27+          Sun Feb 10 23:36:42 2013 - Tue Feb 12 22:53:44 2013 (1+23:17)
#b_ark    pts/2        192.168.2.3      Mon Feb  4 22:55:15 2013 - Tue Feb  5 12:03:23 2013  (13:08)
#pi       pts/1        192.168.2.3      Tue Feb 12 17:04:26 2013   still logged in
#---------                                  --------------------                            ---------
#[0:9]                                      [43:63]                                         [91:-1]
#		time.strptime(line[43:63], "%b %d %H:%M:%S %Y")		re.split("((\d+)\+)?(\d{2}):(\d{2})", line[91:-1])
#
# Writes activity.jsonp containing:
#	{'name': time, ...}
# for home directories other than Desktop & pi
#
# To run repeatedly: while [ 1 ]; do ./last.py ; sleep 2; done
#

import subprocess
import datetime
import time
import json
import re
import os

def parse(now, line):
    if len(line.strip()) == 0:
        return None
    if line.startswith("wtmp"):
        return None
    user = line[0:9].strip()
    if user == "Desktop" or user == "pi":
        return None
    try:
        deltas = re.split("((\d+)\+)?(\d{2}):(\d{2})", line[91:-1])
        days = deltas[2]
        deltaT = datetime.timedelta(hours=int(deltas[3]), minutes=int(deltas[4]), days=0 if days is None else int(days)).total_seconds()
    except IndexError:
        end = now
        start = time.strptime(line[43:63], "%b %d %H:%M:%S %Y")
        deltaT = time.mktime(end) - time.mktime(start)
    return (user, deltaT)

now = time.gmtime()

teams = [name for name in os.listdir("/home") if name.startswith('team')]
results = {team: 0 for team in teams}
lines = subprocess.check_output(["last", "-F"]).split("\n")

for line in lines:
    a = parse(now, line)
    if a is None:
        continue
    if a[0] in results:
        results[a[0]] = results[a[0]] + a[1]
    else:
        results[a[0]] = a[1]

fp = open('activity.jsonp', 'w')
json.dump(results, fp)
fp.close()
