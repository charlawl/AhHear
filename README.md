# AhHear
Noise level measurement application


Android Application


To get the app running:
Go to your emulator
- select settings
- apps & notifications
- advanced
- app permissions
- microphone
- toggle switch for AhHearApp

The above procedure allows the app to use your microphone. If you don't the above, the app will crash whenever you to try to access the microphone i.e record loudness.


Web Server

To get data, you need to 

`$ cd AhHearApi`
`$ pip3 install -r requirements.txt`
`$ hug -f api.py`



