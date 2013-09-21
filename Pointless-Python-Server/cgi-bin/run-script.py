#!/usr/bin/python
 
import cgi
import sys
import stat
import subprocess
import datetime
import os
import re

# Get the user's request
form = cgi.FieldStorage()
 
teamname = form.getvalue('name')
script = re.sub(r"\r\n", "\n", form.getvalue('script'))

# Switch to the scratch directory
os.chdir('/home/erac/scratch')

# Clean up scratch directory
subprocess.check_output('rm -rf *', shell=True)

# Write script to a file
f = open('script', 'w')
f.write(script)
f.close()
os.chmod('script', stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR)

# Run the user's script
now = datetime.datetime.now()

start = os.times()
output = subprocess.check_output('./script', shell=True)
end = os.times()
delta = end[4] - start[4]

# 'increment' the appropriate team counter
f = open('/home/erac/team-stats', 'a')
f.write('%s,%s,%.2f\n' % (now, teamname, delta))
f.close()

# A bit of logging
sys.stderr.write('Script for [%s] (elapsed %.2f):\n%s\n\n' % (teamname, delta, script))

print """Content-type: text/html

<html>
  <head>
    <title>run-script</title>
  </head>
  <body>
    <h3>Request for team %s is:</h3>
    <hr/>
    <pre>
%s
    </pre>
    <hr/>
    <h3>Output is:</h3>
    <hr/>
    <pre>
%s
    </pre>
    <hr/>
    Elapsed time: %.2f
  </body>
</html>""" % (teamname, script, output, delta)
