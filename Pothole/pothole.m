function ret=pothole(varargin)
p=inputParser;
defaultInputFilename='input.txt';
defaultInputFilename='..\Wenzhuo\data\2015-1-22\1.txt';
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
outputFile=fopen(outputFilename,'w');
C=textscan(inputFile,'%s%f%f%f');
t=C{1};
x=C{2};
y=C{3};
z=C{4};
zBase=z-mean(z);
dataLength=length(zBase);
zVar=zeros(1,ceil(dataLength/FRQ));
for i=1:ceil(dataLength/FRQ) %compute stdandard variance
    if(i+FRQ*i>dataLength)
        zVar(i)= std(zBase(1+FRQ*(i-1):dataLength));
    else
        zVar(i)= std(zBase(1+FRQ*(i-1):1+FRQ*i));
    end
end
idx=[];
avg=mean(zVar);
for j=1:length(zVar)%如果方差大于平均方差 则记录为振动点
    if(zVar(j)>(avg+THRESHOLD))
        idx=[idx j];
    end
end
start=0;
final=0;
waveIdx=[];
waveNum=1;
waveData=[];
for i=3:length(idx)%find out wave
    if(idx(i)-idx(i-1)<5)
        waveIdx=[waveIdx idx(i-1)];
    elseif(idx(i)-idx(i-1)>=5 && idx(i-1)-idx(i-2)<5)
        waveIdx=[waveIdx idx(i-1)];
        start=waveIdx(1);
        final=waveIdx(length(waveIdx));
        timestart=t(FRQ*(start-1)+1);
        timeend=t(FRQ*final);
        sumx=sum(x(FRQ*(start-1)+1:FRQ*final));
        sumy=sum(y(FRQ*(start-1)+1:FRQ*final));
        waveIdx=[];
        waveData=zBase(FRQ*(start-1)+1:FRQ*final);
        if(waveNum==1)%确定第几个波形进行后续处理，此处算则了第二个波形，即后轮的振动，因为前轮的阻尼振动会被后轮的受迫振动所覆盖掉
            [~,~,~]=calparam(waveData);
            [depth]=newgongshi(waveData,20);
            fprintf(outputFile,'%s %s %f %f %f\n',char(timestart),char(timeend),sumx,sumy,depth);
        end
        waveNum=waveNum+1;
    else
        waveIdx=idx(i-1);
    end
end

