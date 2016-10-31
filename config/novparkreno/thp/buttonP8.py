import Adafruit_BBIO.GPIO as GPIO
import time
GPIO.setup("P8_12", GPIO.IN)
no = 0
while  1 == 1 : 
    no = no +1
    GPIO.wait_for_edge("P8_12", GPIO.RISING)
    while GPIO.input("P8_12"):
        print "UID Value:%d"%no 
        time.sleep(1)
 
