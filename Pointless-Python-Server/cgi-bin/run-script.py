#!/usr/bin/python
 
import cgi
import sys
 
form = cgi.FieldStorage()
 
teamname = form.getvalue('name')
script = form.getvalue('script')

# 'Normal' way to log?
sys.stderr.write('run-script: %s\n%s\n' % (teamname, script))

# 'increment' the appropriate team counter

# run the script ()

print """Content-type: text/html

<html>
  <head>
    <title>run-script</title>
  </head>
  <body>
    Arguments are: %s %s
  </body>
</html>""" % (teamname, script)