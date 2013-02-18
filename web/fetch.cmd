@echo off
rem Run this to fetch latest login times
rem On RasPi update info by running:- pi@raspberrypi ~/TimeToTurnOnTheLight $ while [ 1 ]; do ./last.py ; sleep 2; done
:loop
	"C:\Program Files (x86)\putty\pscp" -pw raspberry pi@192.168.2.103:TimeToTurnOnTheLight/activity.jsonp  ./
	type activity.jsonp
	sleep 2
goto :loop
