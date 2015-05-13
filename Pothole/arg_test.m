function ret=main(varargin)
  p=inputParser;
  defaultInputFile='input.txt';
  addOptional(p,'i',defaultInputFile);
  defaultOutputFile='output.txt';
  addOptional(p,'o',defaultOutputFile);
  p.parse(varargin{:});
  inputFile=p.Results.i;
  outputFile=p.Results.o;
  fprintf('input:%s\n',inputFile);
  fprintf('output:%s\n',outputFile);
  ret=0;