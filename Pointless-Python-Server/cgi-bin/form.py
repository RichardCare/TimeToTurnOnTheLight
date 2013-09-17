#!/usr/bin/python
 
print """Content-type: text/html

<!-- Note that action is "test_form.py", not "cgi-bin/test_form.py" -->
<form method="post" action="test_form.py">
<textarea name="comments" cols="40" rows="5">
Enter comments here...
</textarea>
<br/>
<input type="submit" value="Submit">
</form>"""