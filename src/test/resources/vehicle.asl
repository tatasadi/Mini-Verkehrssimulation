!main.

+!main <-
    generic/print("hello vehicle")
.

+!vehicle( speed(S), type(T), visibility(V), vehiclestrafficlightcolor(Vtl), pedestrianstrafficlightcolor(Ptl) )
    <-
    generic/print( S, T, V, Vtl, Ptl)
.