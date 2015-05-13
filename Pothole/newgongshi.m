function [] = newgongshi(selectdata,sv)
%syms p delta t ;  %delta是衰弱幅度 qt是对输入数据进行变换之后的数据
x=selectdata;
N=100;
tt=[0:1/N:1/N*(length(x)-1)];
st=tt*sv*1000/3600
z=zeros(1,length(x));
%figure;
subplot(511);
plot(st,x);
hold on;
plot(st,z,'red');
hold off;
title('原图');
x=smooth(x,10);x=x';
%x=medfilt1(x,10);
%w=2*pi/t
p=1/0.63*(2*pi);
delta=0.8;


subplot(512);
plot(st,x);
title('平滑处理后的振动');


xx=zeros(1,length(x));
xxx=zeros(1,length(x));
re=zeros(1,length(x));
for i=1:length(x)-1
    xx(i+1)=xx(i)+1/N*x(i);
end
xx=linefit(tt,xx);


for i=1:length(x)-1
    xxx(i+1)=xxx(i)+1/N*xx(i);
end
%xxx=linefit(tt,xxx);
subplot(513);
plot(st,xx);
hold on;
plot(st,z,'r');
hold off;
title('一次积分');
subplot(514);
plot(st,xxx);
title('二次积分');
qt=pi/(p*delta)*x+xx+(pi*p)/delta*xxx;

m=p*pi*tt/delta;


ex=exp(m);
exqt=ex.*qt;
for i=1:length(exqt)-1
    re(i+1)=re(i)+1/N*exqt(i);
end

exx=exp(-1*p*pi*tt/delta);
f1=pi/(p*delta)*exx
all=f1.*re
subplot(515);
plot(st,all);
dif=[0,diff(all)]
sumdif=sum(dif)
sumall=sum(all)
%figure;
%plot(st,dif);
%figure(2);
%plot(qt);
title('处理后');


