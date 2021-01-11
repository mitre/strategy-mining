__includes ["factors.nls"]

;extensions [table]

breed [sensors sensor]
breed [targets target]
breed [uavs uav]

globals [totalSum randSeed terrainColorsList firstPD totalDist distList]
turtles-own []
patches-own [myRadarStrength myTcolor]

sensors-own [mySaw myLooksTable myMeanPD]
targets-own []
uavs-own [myRstrength mySpeed]

to setup
  clear-turtles
  clear-all-plots
  clear-patches
  set distList []
  set firstPD 2000
  import-pcolors "land2.png"
  set totalSum 0
  ;test
  ask patches [set myTcolor pcolor]
  if megRun? [random-seed randSeed]
  create-sensors 1 [sensorSetup]
  create-uavs numUAVs [ uavSetup ]
  ask patches [set myRadarStrength 0]
  reset-ticks
end

to test
  let p sort patches
  let i 0
  while [i < length p]
  [
    ask item i p [set pcolor item i terrainColorsList]
    set i i + 1
  ]
end

to uavSetup
  ;set color blue - 2 + random 7  ;; random shades look nice
  set color red
  set size 5  ;; easier to see
  setxy random 10 + 200 random 10 + 400
  ;setxy random-xcor random-ycor
  ;while [[pcolor] of patch-here = blue][setxy random-xcor random-ycor]
  set mySpeed 1
end

to sensorSetup
  set shape "sensor"
  set size 10
  setxy 113 217
  if fixedSensor? [set heading 90]
  set mySaw patches in-cone sqrt (1 / tHold) viewAngle
  ;set myLooksTable table:make
  set myMeanPD 0
end

to go
  ;ifelse megRun? [no-display][display]
  ask sensors [sense]
  ask uavs [uavMove]
  if showRadarStrength? [ask patches [showRadarStrength]]
  if firstPD = 2000 [checkFirstPD]
  calcDistance
  calcTotalScore
  if not megRun? [updatePlots]
  tick
  ;if megRun? and ticks > 2000 [file-open "output.csv" file-write totalSum file-close-all stop]
end

to uavMove
  feelRadar

  ;; @EMD @EvolveNextLine @Factors-File="factors.nls" @return-type=top
  setNewSpeedAndHeading (meanFlockmateSpeed findRadarFlockmates) (meanFlockHeading findRadarFlockmates) 
  
  avoid
  fd mySpeed
end

to calcTotalScore
  ;set totalSum totalSum + sum [myRstrength] of uavs ;; from pre-Aug 28 runs
  let pw PdWeight / (PdWeight + distWeight + firstWeight + swarmDistWeight)
  let dw distWeight / (PdWeight + distWeight + firstWeight + swarmDistWeight)
  let fw firstWeight / (PdWeight + distWeight + firstWeight + swarmDistWeight)
  let sdw swarmDistWeight / (PdWeight + distWeight + firstWeight + swarmDistWeight)
  set totalSum totalSum + pw * (UAVPd / 18000) + dw * (UAVdistance / 2835.5) + fw *  ( 1 - (firstPD / 2000)) + sdw * (1 - (((sum distList) / count uavs) / 2000))
end

to-report UAVPd
  report sum [0.9 / (1 + distance one-of sensors)] of uavs
end

to checkFirstPD
  let u sort uavs
  foreach u [x -> ask x [if firstPD = 2000 and random-float 1 < (0.9 / (1 + distance one-of sensors)) [set firstPD ticks]]]
end

to-report UAVdistance
  report sum [distance one-of sensors] of uavs
end



to showRadarStrength
  ifelse myRadarStrength > 0 [set pcolor scale-color red myRadarStrength 0 .1][set pcolor myTcolor]
end

to calcRadarStrength [s]
  if distance s > 0 [set myRadarStrength power * ( 1 / (distance s) ^ 2)]
end



to calcDistance
  let x mean [xcor] of uavs
  let y mean [ycor] of uavs
  set totalDist ((sum [distancexy x y] of uavs) / 2446.6)
  ifelse totalDist < 200
  [set distList fput 0 distList]
  [set distList fput totalDist distList]
end



to updatePlots
;  set-current-plot "averagePD"
;  set-current-plot-pen "meanPD"
;  plot mean [myMeanPD] of sensors
;  set-current-plot-pen "theory"
;  plot targetPD * terrainCover
;
;  set-current-plot "headings"
;  set-current-plot-pen "sensorHeading"
;  plot [heading] of one-of sensors
;  set-current-plot-pen "swarmHeading"
;  plot mean [heading] of uavs
;  set-current-plot-pen "uphill"
;  plot [heading] of one-of sensors + 180

  set-current-plot "aveXY"
  if random-float 1 > aveXYnoise [plotxy mean [xcor] of uavs mean [ycor] of uavs]

  set-current-plot "fit"
  plot (sum [myRstrength] of uavs) / count uavs

  set-current-plot "totFit"
  plot totalSum

  set-current-plot "aveDist"
  plot mean [distance one-of sensors] of uavs

  set-current-plot "totalDistance"
  plot totalDist
end
@#$#@#$#@
GRAPHICS-WINDOW
210
10
672
889
-1
-1
2.0
1
10
1
1
1
0
0
0
1
0
226
0
434
1
1
1
ticks
30.0

BUTTON
4
10
70
43
NIL
setup
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

BUTTON
70
10
133
43
step
go
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

BUTTON
133
10
196
43
NIL
go
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

SLIDER
12
50
184
83
viewAngle
viewAngle
0
180
41.0
1
1
NIL
HORIZONTAL

BUTTON
17
770
134
803
showStrength
ask patches [set pcolor scale-color red myRadarStrength 0 .1]
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

SWITCH
17
671
150
704
showSweep?
showSweep?
0
1
-1000

SLIDER
670
10
854
43
vision
vision
0
50
10.0
1
1
NIL
HORIZONTAL

SLIDER
670
43
854
76
minimum-separation
minimum-separation
0
10
10.0
1
1
NIL
HORIZONTAL

SLIDER
670
76
854
109
max-separate-turn
max-separate-turn
0
10
2.5
.01
1
NIL
HORIZONTAL

SLIDER
670
109
854
142
max-align-turn
max-align-turn
0
10
5.0
.01
1
NIL
HORIZONTAL

SLIDER
670
142
854
175
max-cohere-turn
max-cohere-turn
0
10
10.0
.01
1
NIL
HORIZONTAL

SWITCH
672
292
810
325
radarBias?
radarBias?
0
1
-1000

SLIDER
670
177
842
210
numUAVs
numUAVs
0
50
10.0
1
1
NIL
HORIZONTAL

SWITCH
17
704
153
737
fixedSensor?
fixedSensor?
1
1
-1000

SWITCH
672
325
780
358
comms?
comms?
0
1
-1000

SLIDER
13
84
185
117
power
power
0
100
79.0
1
1
NIL
HORIZONTAL

SWITCH
780
325
923
358
onlyNonZero?
onlyNonZero?
0
1
-1000

SLIDER
924
325
1096
358
leaderBias
leaderBias
0
1
0.27
.01
1
NIL
HORIZONTAL

PLOT
673
369
894
723
aveXY
NIL
NIL
-1.0
227.0
-1.0
435.0
true
false
"" ""
PENS
"default" 1.0 2 -16777216 true "" ""

SLIDER
671
213
843
246
searchSpeed
searchSpeed
0
5
1.0
.1
1
NIL
HORIZONTAL

SLIDER
672
248
844
281
foundSpeed
foundSpeed
0
5
0.3
.1
1
NIL
HORIZONTAL

PLOT
673
724
896
889
fit
NIL
NIL
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

PLOT
897
724
1119
889
totFit
NIL
NIL
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

SWITCH
17
737
130
770
megRun?
megRun?
1
1
-1000

MONITOR
949
10
1067
55
NIL
totalSum
5
1
11

SLIDER
15
120
187
153
tHold
tHold
0.00001
.0005
1.0E-4
.00001
1
NIL
HORIZONTAL

TEXTBOX
25
637
175
665
Don't worry about these four things...
11
0.0
1

PLOT
896
546
1119
723
aveDist
NIL
NIL
0.0
10.0
0.0
10.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

MONITOR
854
10
946
55
NIL
UAVdistance
5
1
11

SLIDER
867
57
1039
90
distWeight
distWeight
0
1
1.0
.01
1
NIL
HORIZONTAL

SLIDER
867
90
1039
123
PdWeight
PdWeight
0
1
1.0
.01
1
NIL
HORIZONTAL

SWITCH
912
291
1096
324
showRadarStrength?
showRadarStrength?
0
1
-1000

SLIDER
867
123
1039
156
firstWeight
firstWeight
0
1
1.0
.01
1
NIL
HORIZONTAL

MONITOR
1042
57
1099
102
NIL
firstPD
2
1
11

SLIDER
899
370
1071
403
aveXYnoise
aveXYnoise
0
1
0.33
.01
1
NIL
HORIZONTAL

PLOT
895
406
1119
545
totalDistance
NIL
NIL
0.0
10.0
0.0
1.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

SLIDER
867
157
1039
190
swarmDistWeight
swarmDistWeight
0
1
1.0
.01
1
NIL
HORIZONTAL

SLIDER
871
207
1043
240
fastSpeed
fastSpeed
0
5
2.0
.1
1
NIL
HORIZONTAL

SLIDER
871
241
1043
274
slowSpeed
slowSpeed
0
5
1.0
.1
1
NIL
HORIZONTAL

SLIDER
1057
109
1229
142
max-turn
max-turn
0
90
15.0
.01
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This model is a very basic attempt at creating a minimal specification of a swarming model with the swarm trying to accomplish a mission (find the source of a radar signal)

## HOW IT WORKS

The swarm starts out in the upper right corner of the world, the radar is in the middle of the the world and rotates clockwise.  From their start the swarm begins to move out based upon the Boids flocking algorithm and a 'hard' function that ensures they stay in the play box (so they do not exist on a torus).  If one or more of the swarm detects a radar signal (and parameter values are set appropiately) then the swarm will turn toward the member with the strongest signal.  Based upon parameter values the swarm may have different speeds depending upon whether or not it senses radar signal.

## HOW TO USE IT

The sliders on the left side of the interface control the radar source and sensitivity of uav equipement to the radar signal.  The power is the power of the radar, the veiwAngle is how wide the radar beam is, and the tHold is the minimal amount of signal to care about, that sets the maximum possible range of the radar (mostly for efficiency and viz).  The sliders on the right deal with swarm behavior.  

Vision = how far the swarm agents can see

minimum-sparation = how far apart the agents want to be

max-separate-turn = how far per time step an agent is able to turn away from the closest other agent if that agent is closer than minimum-separation

max-align-turn = how far per time step an agent is able to turn towards the average heading of the agents they can see

max-cohere-turn = how far per time step an agent is able to turn towards the middle of the agents they can see

numUAVs = size of the swarm

searchSpeed = distance per time step an agent moves when they do not detect any radar signal

foundSpeed = distance per time step an agent moves when they do detect a radar signal

radarBias = with it set to on the agents will be influenced (based upon leaderBias) to turn toward the agent with the strongest radar signal

comms? = this is a boolean that, when on, allows the agents to know the location of the agent with the strongest radar signal

onlyNonZero? = if this is set to on, agents will only worry about other agents if those agents have some nonzero radar signal

leaderBias = how much influence the agent with the strongest radar signal will influence the dirction of other agents, it ranges from 0 to 50% of their heading decision.

###Plots:

aveDist is the average distance of the swarm members from the radar.
aveXY is the average x and y coordinates of the swarm
fit is the amount of radar energy the swarm 'experiences' at each time step
totFit is the accumulation of fit


## THINGS TO NOTICE

(suggested things for the user to notice while running the model)

## THINGS TO TRY

(suggested things for the user to try to do (move sliders, switches, etc.) with the model)

## EXTENDING THE MODEL

(suggested things to add or change in the Code tab to make the model more complicated, detailed, accurate, etc.)

## NETLOGO FEATURES

(interesting or unusual features of NetLogo that the model uses, particularly in the Code tab; or where workarounds were needed for missing features)

## RELATED MODELS

(models in the NetLogo Models Library and elsewhere which are of related interest)

## CREDITS AND REFERENCES

(a reference to the model's URL on the web if it has one, as well as any other necessary credits, citations, and links)
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

arrow
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

box
false
0
Polygon -7500403 true true 150 285 285 225 285 75 150 135
Polygon -7500403 true true 150 135 15 75 150 15 285 75
Polygon -7500403 true true 15 75 15 225 150 285 150 135
Line -16777216 false 150 285 150 135
Line -16777216 false 150 135 15 75
Line -16777216 false 150 135 285 75

bug
true
0
Circle -7500403 true true 96 182 108
Circle -7500403 true true 110 127 80
Circle -7500403 true true 110 75 80
Line -7500403 true 150 100 80 30
Line -7500403 true 150 100 220 30

butterfly
true
0
Polygon -7500403 true true 150 165 209 199 225 225 225 255 195 270 165 255 150 240
Polygon -7500403 true true 150 165 89 198 75 225 75 255 105 270 135 255 150 240
Polygon -7500403 true true 139 148 100 105 55 90 25 90 10 105 10 135 25 180 40 195 85 194 139 163
Polygon -7500403 true true 162 150 200 105 245 90 275 90 290 105 290 135 275 180 260 195 215 195 162 165
Polygon -16777216 true false 150 255 135 225 120 150 135 120 150 105 165 120 180 150 165 225
Circle -16777216 true false 135 90 30
Line -16777216 false 150 105 195 60
Line -16777216 false 150 105 105 60

car
false
0
Polygon -7500403 true true 300 180 279 164 261 144 240 135 226 132 213 106 203 84 185 63 159 50 135 50 75 60 0 150 0 165 0 225 300 225 300 180
Circle -16777216 true false 180 180 90
Circle -16777216 true false 30 180 90
Polygon -16777216 true false 162 80 132 78 134 135 209 135 194 105 189 96 180 89
Circle -7500403 true true 47 195 58
Circle -7500403 true true 195 195 58

circle
false
0
Circle -7500403 true true 0 0 300

circle 2
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240

cow
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

cylinder
false
0
Circle -7500403 true true 0 0 300

dot
false
0
Circle -7500403 true true 90 90 120

face happy
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 255 90 239 62 213 47 191 67 179 90 203 109 218 150 225 192 218 210 203 227 181 251 194 236 217 212 240

face neutral
false
0
Circle -7500403 true true 8 7 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Rectangle -16777216 true false 60 195 240 225

face sad
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 168 90 184 62 210 47 232 67 244 90 220 109 205 150 198 192 205 210 220 227 242 251 229 236 206 212 183

fish
false
0
Polygon -1 true false 44 131 21 87 15 86 0 120 15 150 0 180 13 214 20 212 45 166
Polygon -1 true false 135 195 119 235 95 218 76 210 46 204 60 165
Polygon -1 true false 75 45 83 77 71 103 86 114 166 78 135 60
Polygon -7500403 true true 30 136 151 77 226 81 280 119 292 146 292 160 287 170 270 195 195 210 151 212 30 166
Circle -16777216 true false 215 106 30

flag
false
0
Rectangle -7500403 true true 60 15 75 300
Polygon -7500403 true true 90 150 270 90 90 30
Line -7500403 true 75 135 90 135
Line -7500403 true 75 45 90 45

flower
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Circle -7500403 true true 85 132 38
Circle -7500403 true true 130 147 38
Circle -7500403 true true 192 85 38
Circle -7500403 true true 85 40 38
Circle -7500403 true true 177 40 38
Circle -7500403 true true 177 132 38
Circle -7500403 true true 70 85 38
Circle -7500403 true true 130 25 38
Circle -7500403 true true 96 51 108
Circle -16777216 true false 113 68 74
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240

house
false
0
Rectangle -7500403 true true 45 120 255 285
Rectangle -16777216 true false 120 210 180 285
Polygon -7500403 true true 15 120 150 15 285 120
Line -16777216 false 30 120 270 120

leaf
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195

line
true
0
Line -7500403 true 150 0 150 300

line half
true
0
Line -7500403 true 150 0 150 150

pentagon
false
0
Polygon -7500403 true true 150 15 15 120 60 285 240 285 285 120

person
false
0
Circle -7500403 true true 110 5 80
Polygon -7500403 true true 105 90 120 195 90 285 105 300 135 300 150 225 165 300 195 300 210 285 180 195 195 90
Rectangle -7500403 true true 127 79 172 94
Polygon -7500403 true true 195 90 240 150 225 180 165 105
Polygon -7500403 true true 105 90 60 150 75 180 135 105

plant
false
0
Rectangle -7500403 true true 135 90 165 300
Polygon -7500403 true true 135 255 90 210 45 195 75 255 135 285
Polygon -7500403 true true 165 255 210 210 255 195 225 255 165 285
Polygon -7500403 true true 135 180 90 135 45 120 75 180 135 210
Polygon -7500403 true true 165 180 165 210 225 180 255 120 210 135
Polygon -7500403 true true 135 105 90 60 45 45 75 105 135 135
Polygon -7500403 true true 165 105 165 135 225 105 255 45 210 60
Polygon -7500403 true true 135 90 120 45 150 15 180 45 165 90

sensor
true
0
Polygon -7500403 true true 210 105 90 105 150 210
Rectangle -7500403 true true 135 75 165 120

sheep
false
15
Circle -1 true true 203 65 88
Circle -1 true true 70 65 162
Circle -1 true true 150 105 120
Polygon -7500403 true false 218 120 240 165 255 165 278 120
Circle -7500403 true false 214 72 67
Rectangle -1 true true 164 223 179 298
Polygon -1 true true 45 285 30 285 30 240 15 195 45 210
Circle -1 true true 3 83 150
Rectangle -1 true true 65 221 80 296
Polygon -1 true true 195 285 210 285 210 240 240 210 195 210
Polygon -7500403 true false 276 85 285 105 302 99 294 83
Polygon -7500403 true false 219 85 210 105 193 99 201 83

square
false
0
Rectangle -7500403 true true 30 30 270 270

square 2
false
0
Rectangle -7500403 true true 30 30 270 270
Rectangle -16777216 true false 60 60 240 240

star
false
0
Polygon -7500403 true true 151 1 185 108 298 108 207 175 242 282 151 216 59 282 94 175 3 108 116 108

target
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60

tree
false
0
Circle -7500403 true true 118 3 94
Rectangle -6459832 true false 120 195 180 300
Circle -7500403 true true 65 21 108
Circle -7500403 true true 116 41 127
Circle -7500403 true true 45 90 120
Circle -7500403 true true 104 74 152

triangle
false
0
Polygon -7500403 true true 150 30 15 255 285 255

triangle 2
false
0
Polygon -7500403 true true 150 30 15 255 285 255
Polygon -16777216 true false 151 99 225 223 75 224

truck
false
0
Rectangle -7500403 true true 4 45 195 187
Polygon -7500403 true true 296 193 296 150 259 134 244 104 208 104 207 194
Rectangle -1 true false 195 60 195 105
Polygon -16777216 true false 238 112 252 141 219 141 218 112
Circle -16777216 true false 234 174 42
Rectangle -7500403 true true 181 185 214 194
Circle -16777216 true false 144 174 42
Circle -16777216 true false 24 174 42
Circle -7500403 false true 24 174 42
Circle -7500403 false true 144 174 42
Circle -7500403 false true 234 174 42

turtle
true
0
Polygon -10899396 true false 215 204 240 233 246 254 228 266 215 252 193 210
Polygon -10899396 true false 195 90 225 75 245 75 260 89 269 108 261 124 240 105 225 105 210 105
Polygon -10899396 true false 105 90 75 75 55 75 40 89 31 108 39 124 60 105 75 105 90 105
Polygon -10899396 true false 132 85 134 64 107 51 108 17 150 2 192 18 192 52 169 65 172 87
Polygon -10899396 true false 85 204 60 233 54 254 72 266 85 252 107 210
Polygon -7500403 true true 119 75 179 75 209 101 224 135 220 225 175 261 128 261 81 224 74 135 88 99

wheel
false
0
Circle -7500403 true true 3 3 294
Circle -16777216 true false 30 30 240
Line -7500403 true 150 285 150 15
Line -7500403 true 15 150 285 150
Circle -7500403 true true 120 120 60
Line -7500403 true 216 40 79 269
Line -7500403 true 40 84 269 221
Line -7500403 true 40 216 269 79
Line -7500403 true 84 40 221 269

wolf
false
0
Polygon -16777216 true false 253 133 245 131 245 133
Polygon -7500403 true true 2 194 13 197 30 191 38 193 38 205 20 226 20 257 27 265 38 266 40 260 31 253 31 230 60 206 68 198 75 209 66 228 65 243 82 261 84 268 100 267 103 261 77 239 79 231 100 207 98 196 119 201 143 202 160 195 166 210 172 213 173 238 167 251 160 248 154 265 169 264 178 247 186 240 198 260 200 271 217 271 219 262 207 258 195 230 192 198 210 184 227 164 242 144 259 145 284 151 277 141 293 140 299 134 297 127 273 119 270 105
Polygon -7500403 true true -1 195 14 180 36 166 40 153 53 140 82 131 134 133 159 126 188 115 227 108 236 102 238 98 268 86 269 92 281 87 269 103 269 113

x
false
0
Polygon -7500403 true true 270 75 225 30 30 225 75 270
Polygon -7500403 true true 30 75 75 30 270 225 225 270
@#$#@#$#@
NetLogo 6.1.1
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
default
0.0
-0.2 0 0.0 1.0
0.0 1 1.0 0.0
0.2 0 0.0 1.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180
@#$#@#$#@
0
@#$#@#$#@
