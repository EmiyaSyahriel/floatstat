# Temperamon
Simple floating window to show some device status controllable via Notification bar, for Android, 
sprinkled with my (and some of the contributors) ego.

## Features
- **Notification Control** - fast show/hide toggle
- ***Kinda accurate* SoC Temperature reading** - Gives average of all recognized thermal zone state 
  instead of reading just one. *Well, your phone is just a fist-size slab, average value should be suffice*
- **** - 

## Why?
Have you ever seen this kind of dialog?
![Some annoying ad push notification confirmation dialog](readme/1.jpg)
On some Phone / ROM (e.g My Nokia 6.1 Plus running Android 10), this kind of dialog cannot be 
responded when there is a floating window active due to some security concern. When you try to 
respond it, it will ask you to deactivate any floating window before you can really respond the 
dialog. My last app from other dev offering similar functionality will require me to open it's app 
to deactivate the floating window or revoke their permission from Settings, thus making it 
inefficient to just respond a dialog. Some app that does offer the notification-based control will 
asks you to buy the their "Pro" version or "Pro functionality" IAP, or the worst, The app have ads 
that triggered by their floating window that will fully cover the entire screen anytime and 
anywhere!
<details>
  <summary><i>My complaints and cursing, but nvm.</i></summary>
SHAME ON YOU, THE DEV IMPLEMENTING THE "ANYTIME AND ANYWHERE" ADS! I KNOW YOU NEED MONEY, ME TOO! 
JUST DON'T BE GREEDY AND ANNOY YOUR USER! MAY YOU BECAME CONSTANTLY H?RN? UNTIL YOU FIX YOUR APP!
</details>

Well, Sometimes it's worth it to re-invent a new kind of wheel when the existing wheels needs too 
much energy to roll over or is designed to work by constantly trampling your toes.

## License
Apache License 2.0, with some file is under Public Domain.