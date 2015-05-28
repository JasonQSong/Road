function ret=StepKMean(varargin)
p=inputParser;
defaultInputLastId='0';
addOptional(p,'iid',defaultInputLastId);
defaultInputHoleFilename='input-hole.txt';
%defaultInputHoleFilename='..\RoadServer\data_kmean\fff1-1432677394451-hole.in';
addOptional(p,'ih',defaultInputHoleFilename);
defaultInputMeanFilename='input-mean.txt';
%defaultInputMeanFilename='..\RoadServer\data_kmean\fff0-1432677394451-mean.in';
addOptional(p,'im',defaultInputMeanFilename);
defaultOutputLastId='ffff';
addOptional(p,'oid',defaultOutputLastId);
defaultOutputMeanFilename='output-mean.txt';
addOptional(p,'om',defaultOutputMeanFilename);
%varargin={};
p.parse(varargin{:});
inputLastId=p.Results.iid;
inputHoleFilename=p.Results.ih;
inputMeanFilename=p.Results.im;
outputLastId=p.Results.oid;
outputMeanFilename=p.Results.om;
fprintf('input lastId:%s\n',inputLastId);
fprintf('input hole:%s\n',inputHoleFilename);
fprintf('input mean:%s\n',inputMeanFilename);
fprintf('output lastId:%s\n',outputLastId);
fprintf('output mean:%s\n',outputMeanFilename);
ret=0;

inputHoleFile=fopen(inputHoleFilename);
inputMeanFile=fopen(inputMeanFilename);
outputMeanFile=fopen(outputMeanFilename,'w');
C=textscan(inputHoleFile,'%s%f%f%f%f');
originHoleId=char(C{1});
originHoleDiameter=C{2};
originHoleDepth=C{3};
originHoleLongitude=C{4};
originHoleLatitude=C{5};
C=textscan(inputMeanFile,'%f%f%f%f%d');
meanHoldDiameter=C{1};
meanHoleDepth=C{2};
meanHoleLongitude=C{3};
meanHoleLatitude=C{4};

originHoleBelongTo=zeros(1,length(originHoleId));

start=0;
final=0;
for i=1:length(originHoleId)
    [~,CmpVal]=sort({originHoleId(i,:);inputLastId});
    if CmpVal(1)==2 && start==0 %inputLastId>originHoleId(i)
        start=i;
    end
    [~,CmpVal]=sort({originHoleId(i,:);outputLastId});
    if CmpVal(1)==1 && final<i %outputLastId<=originHoleId(i)
        final=i;
    end;
end

if start==0
    for j=1:length(originHoleId)
        jDiffLong=meanHoleLongitude-originHoleLongitude(j);
        jDiffLagi=meanHoleLatitude-originHoleLatitude(j);
        jDisSqr=jDiffLong.*jDiffLong+jDiffLagi.*jDiffLagi;
        [~,jBelongTo]=min(jDisSqr);
        if jBelongTo~=originHoleBelongTo(j)
            originHoleBelongTo(j)=jBelongTo;
        end
    end
else
    for i=start:final
        if isempty(meanHoldDiameter)
            meanHoldDiameter=[meanHoldDiameter,originHoleDiameter(i)];
            meanHoleDepth=[meanHoleDepth,originHoleDepth(i)];
            meanHoleLongitude=[meanHoleLongitude,originHoleLongitude(i)];
            meanHoleLatitude=[meanHoleLatitude,originHoleLatitude(i)];
            originHoleBelongTo(i)=length(meanHoldDiameter);
            continue;
        end
        iDiffLong=meanHoleLongitude-originHoleLongitude(i);
        iDiffLagi=meanHoleLatitude-originHoleLatitude(i);
        iDisSqr=iDiffLong.*iDiffLong+iDiffLagi.*iDiffLagi;
        if min(iDisSqr)>0.001*0.001
            meanHoldDiameter=[meanHoldDiameter originHoleDiameter(i)];
            meanHoleDepth=[meanHoleDepth originHoleDepth(i)];
            meanHoleLongitude=[meanHoleLongitude originHoleLongitude(i)];
            meanHoleLatitude=[meanHoleLatitude originHoleLatitude(i)];
            originHoleBelongTo(i)=length(meanHoldDiameter);
            continue;
        else
            [~,iBelongTo]=min(iDisSqr);
            originHoleBelongTo(i)=iBelongTo;
            belongToChanged=true;
            while belongToChanged
                belongToChanged=false;
                for j=1:length(meanHoldDiameter)
                    meanHoldDiameter(j)=max(originHoleDiameter(originHoleBelongTo==j));
                    meanHoleDepth(j)=max(originHoleDepth(originHoleBelongTo==j));
                    meanHoleLongitude(j)=mean(originHoleLongitude(originHoleBelongTo==j));
                    meanHoleLatitude(j)=mean(originHoleLatitude(originHoleBelongTo==j));
                end
                for j=1:i
                    jDiffLong=meanHoleLongitude-originHoleLongitude(j);
                    jDiffLagi=meanHoleLatitude-originHoleLatitude(j);
                    jDisSqr=jDiffLong.*jDiffLong+jDiffLagi.*jDiffLagi;
                    [~,jBelongTo]=min(jDisSqr);
                    if jBelongTo~=originHoleBelongTo(j)
                        originHoleBelongTo(j)=jBelongTo;
                        belongToChanged=true;
                    end
                end
            end
        end
    end
end
meanHoleTrust=zeros(1,length(meanHoldDiameter));
for j=1:length(meanHoldDiameter)
    meanHoleTrust(j)=sum(originHoleBelongTo==j);
    fprintf(outputMeanFile,'%f %f %f %f %d\n',meanHoldDiameter(j),meanHoleDepth(j),meanHoleLongitude(j),meanHoleLatitude(j),meanHoleTrust(j));
end





















