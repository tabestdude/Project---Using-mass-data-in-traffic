"main.c" is the .c file for stm32 IDE. 
The only thing that was changed in code was the "main.c".
So if you want to replicate it yourself you need to just copy the added code into your code.

the ioc settings that were changed were these:
	- Pinout & Configuration -> I2C1:
		- Mode -> I2C
		- I2C Speed Mode -> Fast Mode
		- I2C Speed Frequency (KHz) -> 400