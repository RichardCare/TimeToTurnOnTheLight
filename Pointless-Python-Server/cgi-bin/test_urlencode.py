#!/usr/bin/python
 
import cgi
import sys
 
form = cgi.FieldStorage()
 
val1 = form.getvalue('first')
val2 = form.getvalue('last')

# 'Normal' way to log?
sys.stderr.write('test_urlencode: %s %s\n' % (val1, val2))

print """Content-type: text/html

<html>
  <head>
    <title>Test URL Encoding</title>
  </head>
  <body>
    Hello my name is %s %s
  </body>
</html>""" % (val1, val2)