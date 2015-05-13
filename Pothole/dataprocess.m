function ret=main(varargin)
p=inputParser;
defaultInputFilename='input.txt';
defaultInputFilename='..\Wenzhuo\data\2015-1-22\6.txt';
addOptional(p,'i',defaultInputFilename);
defaultOutputFilename='output.txt';
addOptional(p,'o',defaultOutputFilename);
p.parse(varargin{:});
inputFilename=p.Results.i;
outputFilename=p.Results.o;
fprintf('input:%s\n',inputFilename);
fprintf('output:%s\n',outputFilename);
ret=0;

%Start
FRQ=5;
THRESHOLD=1;
inputFile=fopen(inputFilename);
C=textscan(inputFile,'%s%f%f%f');  %读取数据
t=C{1};
x=C{2};
y=C{3};
z=C{4};
% [t,x,y,z,a1,a2,a3,a4,a5,a6,a7,a8,a9]=textread('1222.txt','%s%f%f%f%f%f%f%f%f%f%f%f%f');
z=z-mean(z);
%figure;
plot(z);
len=size(z);
varz=[];
for i=1:FRQ:len %计算数据的方差
    if(i+FRQ>len)
        varz=[varz std(z(i:len))];
    else
        varz=[varz std(z(i:i+FRQ))];
    end
end
%figure;
idx=[];
avg=mean(varz)
for j=1:length(varz)%如果方差大于平均方差 则记录为振动点
    if(varz(j)>(avg+THRESHOLD))
        idx=[idx j];
    end
end
idx
start=1;
final=1;
tmp=[];
ss=1;
selectdata=[];
for i=3:length(idx)%找出波形进行分析
    if(idx(i)-idx(i-1)<5)
        tmp=[tmp idx(i-1)];
    elseif(idx(i)-idx(i-1)>=5 && idx(i-1)-idx(i-2)<5)
        tmp=[tmp idx(i-1)]
        start=tmp(1);
        final=tmp(length(tmp));
        tmp=[];
        selectdata=z(FRQ*(start-1)+1:FRQ*final);
        ss=ss+1;
        if(ss==2)%确定第几个波形进行后续处理，此处算则了第二个波形，即后轮的振动，因为前轮的阻尼振动会被后轮的受迫振动所覆盖掉
            calparam(selectdata);
            newgongshi(selectdata,20);
            %solveout(selectdata,FRQ*(start-1)+1);
        end
    else
        tmp=idx(i-1);
    end
end
