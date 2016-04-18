# starway
A set of java classes to emit opc packets driving a LEDscape array - used for StarWay sculpture in Reno 
Key feautures:
1) mapping stars to leds - a config file allows selection of multiple leds to act as a single 'star'
2) generation of valid opc packets to drive the leds
3) 'twinkle' mode which illuminates all the stars with a shade at a slightly varying intensity
4) 'action' mode which is activated when a specific star is selected (via rfid)
5) mapping of rfid tokens to stars
