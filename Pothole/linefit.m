function res = linefit( t,data )
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here
poly=polyfit(t,data,1);
val=polyval(poly,t);
res=data-val;


