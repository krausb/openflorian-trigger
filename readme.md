# Openflorian-Trigger

## Description
Using the f-pro.de USB Buzzer and a little peace of java software to access the Operation Reset REST API Resource of OpenFlorian (http://www.f-pro.de/buzzer/). The Buzzer is attached as USB Keyboard and available as linux device under /dev. OpenFlorian-Trigger is a Java and Vert.X based daemon watching the device and in case of someones pushing the button an event is triggered and fires a call to a defined REST resource endpoint.

## Licence

Copyright (C) 2015  Bastian Kraus

Openflorian is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version)
    
Openflorian is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
    
You should have received a copy of the GNU General Public License
along with Openflorian.  If not, see <http://www.gnu.org/licenses/>.

## Architecture
OpenFlorian-Trigger is a Java and Vert.X based daemon watching the device and in case of someones pushing the button an event is triggered and fires a call to a defined REST resource endpoint.

## Technology Openflorian runs on

### Server
* Hardware: runs best on Raspberry PI 3 Model B
* Operating System: Raspbian Linux (https://www.raspbian.org/)