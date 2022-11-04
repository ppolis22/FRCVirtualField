# FRCVirtualField
A proof of concept for a virtual FRC field, detecting physical game pieces in a virtual space via webcam, utilizing OpenCV.

The game the program is designed to accompany involves robots moving different colored game pieces onto a tic-tac-toe-style grid.

This program works with a webcam and a projector pointed at any flat surface whose color is distinct from that of the game pieces to detect. Upon 
startup, the projector will display the game grid and the user will be presented with a feed from the webcam, at which point they will click on the 
points on webcam feed where the game grid corners are located. Once the corner points are defined, the user is presented with a set of controls to
filter the colors of the game pieces for each team, and the projector updates the state of the grid in real time as pieces are detected in squares.
