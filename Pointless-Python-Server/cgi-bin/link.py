#!/usr/bin/python
 
print """Content-type: text/html

<html>
  <head>
    <title>Test URL Encoding</title>
  </head>
  <body>
    <li/><a href="test_urlencode.py?first=Jack&last=Trades">Link</a> as Jack Trades
    <li/><a href="test_urlencode.py?first=Adam&last=Baum">Link</a> as Adam Baum
    <li/><a href="test_urlencode.py?first=Richard&last=Care">Link</a> as Richard Care
  </body>
</html>"""