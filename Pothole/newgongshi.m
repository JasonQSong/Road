function [depth] = newgongshi(waveData,v)
%syms p delta t ;  %delta是衰弱幅度 qt是对输入数据进行变换之后的数据
%w=2*pi/t by jason
p=1/0.63*(2*pi);
delta=0.8;
N=100;
t=0:1/N:1/N*(length(waveData)-1);
st=t*v*1000/3600;
z=zeros(1,length(waveData));
smoothWaveData=smooth(waveData,10);
smoothWaveDataT=smooth(waveData,10)';
%x=medfilt1(x,10);
xx=zeros(1,length(waveData));
xxx=zeros(1,length(waveData));
re=zeros(1,length(waveData));
for i=1:length(waveData)-1
    xx(i+1)=xx(i)+1/N*smoothWaveDataT(i);
end
xx=linefit(t,xx);
for i=1:length(waveData)-1
    xxx(i+1)=xxx(i)+1/N*xx(i);
end
%xxx=linefit(tt,xxx);
qt=pi/(p*delta)*smoothWaveDataT+xx+(pi*p)/delta*xxx;
m=p*pi*t/delta;
ex=exp(m);
exqt=ex.*qt;
for i=1:length(exqt)-1
    re(i+1)=re(i)+1/N*exqt(i);
end
exx=exp(-1*p*pi*t/delta);
f1=pi/(p*delta)*exx;
all=f1.*re;
%dif=[0,diff(all)];
depth=max(all)-min(all);


