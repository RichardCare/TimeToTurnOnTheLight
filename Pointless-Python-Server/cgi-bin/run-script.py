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

agent = os.environ['HTTP_USER_AGENT']
clientIsWindows = agent.lower().find('windows') != -1
sys.stderr.write("isWindows=%s HTTP_USER_AGENT=%s\n" % (clientIsWindows, agent))

home = '/home/ittltl/'

# Switch to the scratch directory
os.chdir(home + 'scratch')

# Clean up scratch directory
subprocess.check_output('rm -rf *', shell=True)

now = datetime.datetime.now()
try:
    # Clone repo & switch to team's branch (either by protocol or by file for Windows compatibility)
    ### output = subprocess.check_output('git clone /var/cache/git-win/ittltl.git', shell=True)
    output = ""
    if clientIsWindows:
        if not os.path.isdir('/mnt/git/ittltl.git'):
            raise IOError, "Oops! Samba share not mounted, can't clone git repository"
        output += subprocess.check_output('git clone /mnt/git/ittltl.git', shell=True)
    else:
        output += subprocess.check_output('git clone git://raspberrypi2.local/git/ittltl.git', shell=True)
    os.chdir('ittltl')
    output += subprocess.check_output('git checkout --track origin/%s' % teamname, stderr=subprocess.STDOUT, shell=True)

    # Ensure team's script is executable & run it
    os.chmod('script.sh', stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR)
    output += '\n=== Running script for team %s ================================\n\n' % teamname

    start = os.times()
    output += subprocess.check_output('./script.sh', stderr=subprocess.STDOUT, shell=True)
except (NameError, IOError) as e:
    output += e.__str__()
except (subprocess.CalledProcessError) as e:
    output += e.output + e.__str__()

# Calculate user's elapsed time, say zero if exception in in git phase
try:
    end = os.times()
    delta = end[4] - start[4]
except NameError:
    delta = 0

# 'increment' the appropriate team counter
f = open(home + 'team-stats', 'a')
f.write('%s,%s,%.2f\n' % (teamname, now, delta))
f.close()

# A bit of logging
sys.stderr.write('Script for [%s] (elapsed %.2f)\n' % (teamname, delta))

# Return results to requesting team's web-page
print """Content-type: text/html

<html>
  <head>
    <title>run-script</title>
  </head>
  <body>
    <h3>Output for team %s is:</h3>
    <hr/>
    <pre>
%s
    </pre>
    <hr/>
    Elapsed time: %.2f
  </body>
</html>""" % (teamname, output, delta)
