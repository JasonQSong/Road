function ret=main(args)
  p=inputParser;
  defaultInputFile='input.txt';
  addParameter(p,'-i',defaultInputFile);
  addParameter(p,'--input',defaultInputFile);
  defaultOutputFile='output.txt';
  addParameter(p,'-o',defaultOutputFile);
  addParameter(p,'--output',defaultOutputFile);
  inputFile=p.Results['-i'];
  inputFile=p.Results['--input'];
  outputFile=p.Results['-o'];
  outputFile=p.Results['--output'];
  inputFile
  outputFile