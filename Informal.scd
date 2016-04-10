
// These are informal manual testing methods that were not converted
// to formal unit tests. They should be, if it proves necessary for
// reliability.

// Saving and loading continuous mode SpaceTracker
(fork{
  // Test continuousTo
  s.bootSync;
  b=SpaceTracker.continuousAlloc(s, 5, 5, [2,3,4]);
  s.sync;
  b.do {|c, i|
    c[0].setn(0, [
      1, 60+i, 0.5, 0, 0, 0,
      1, 70+i, 0.5, 1500, 1500, 1500,
      1, 80+i, 0.5, 3000, 3000, 3000
    ]);
    c[1..].pairsDo {|ch, b|
      b.setn(0, Array.series(5000).normalize);
    };
  };
  s.sync;

  //SpaceTracker.bufferTo("/tmp/sc/foo.note", b.collect{|b|b[0]}, 3);
  
  SpaceTracker.continuousTo("/tmp/sc/foo.note", b, 3);
  s.sync;
  c=SpaceTracker.toContinuous(s, "/tmp/sc/foo.note");
})

c=SpaceTracker.toContinuous(s, "/tmp/sc/foo.note");
SpaceTracker.continuousTo("/tmp/sc/foo.note", b, 30);
c
c[1][0].getn(0,30){|m|m.postln}
b[2][0].getn(0,30){|m|m.postln}
b.collect{|b|b[0].set(0,0.1)}

// Testing Continuous Playback
(
  z.free;
  z={
    var controls=PlayBufSC.kr(5, b);
    controls.poll;
    Out.kr(0,0);
  }.play;
)

// Testing regular SpaceTracker
SpaceTracker.bufferTo("/tmp/sc/bar.note", d, 30);

// Testing continuous mode recording and playback more isolatedly
(fork{
  n.free;
  n={PlayBufSC.kr(5,b)[0][4].poll}.play; 
})

(fork{
  s.bootSync;
  n.free;
  n={PlayBufS.kr(5,z).poll}.play; 
})
(fork{
  s.bootSync;
  MIDIIn.connectAll;
  z.free;
  z=SpaceTracker.alloc(s, 5, 5);
})

z[0].getn(0,30){|m|m.postln};

(fork{
  o.free;
  o={
    var c;
    c = StControl.kr(\voice, 5, 5);
    RecordBufS.kr(c, z);
  }.play; 
  m.free;
  m=ShapedMIDIC(o,[1,2,3,4,5],[\bend, 74, \touch]);
})

(
  u.free;
  u={
    PlayBuf.kr(1, b[1][6], 1, 1, 8.4).poll;
  }.play;
)

// Testing the max number for a continuosuly rising phasor (part of RecordBufSC)
(
o.free;
o={
  Phasor.kr(1,1,0,2147483647).poll;
}.play;
)

b[0][0].getn(0,30){|m|m.postln};

// Main continous mode testing; save and load, record, playback, and a nice sound
// for playing the Linnstrument on, the main reason to introduce continuous mode.

SpaceTracker.continuousTo("/tmp/sc/foo.note", b, 30);
SpaceTracker.continuousTo("/tmp/sc/foo.note", c, 30);
(fork{
  Server.internal;
  s.bootSync;
  MIDIIn.connectAll;
  b.free;
  b=SpaceTracker.continuousAlloc(s, 5, 5, [2,3,4]);
  c.free;
  c=SpaceTracker.toContinuous(s, "/tmp/sc/foo.note");
  s.sync;

  d = SpaceTracker.alloc(s, 5, 5);
  
  e = SpaceTracker.toBuffer(s, "/tmp/sc/bar.note");
  
  f = SpaceTracker.toBuffer(s, "/tmp/sc/test.note");

  s.sync;
  \done.postln;
})
(
  x.free;
  x={
    var controls, voice, frames;

    controls = StControl.kr(\voice, 5, 5);
    frames = RecordBufSC.kr(controls, b);
    //controls = PlayBufSC.kr(5, b);
    //controls = PlayBufSC.kr(5, c);
    //frames = RecordBufS.kr(controls, b.collect{|b|b[0]});
    //frames.poll;
    //controls = PlayBufS.kr(5, b.collect{|b|b[0]});

    //RecordBufS.kr(controls.collect{|c|c[0..1]}, d);
    //controls = PlayBufS.kr(5, e);

    //controls.poll;

    voice=controls.collect {|d|
      var freq, voice, n,v,b,bb,t,l, r;
      #n,v,b,t,l = d;
      
      r = Lag.kr(n>0 * (b > 200), 0.001);
      
      n=if(n>0,n,48);
      
      bb=b;

      bb = bb/ 16392;

      bb=bb.max(0.4).min(0.65);
      bb = bb - 0.4 * 4;

      b = b/8196;

      l = if(l==0,v,l);

      t=t/128;
      l=l/128;
      v=v/128;

      l=l*l;

      t=t*0.875 + 0.125;
      l=l*0.9999 + 0.0001;
      
      freq = n.midicps;//*b;
      freq=freq.max(30);

      //t=LagUD.kr(t, 0.005, 0.01);
      //l=LagUD.kr(l, 0.005, 0.01);
      //b=LagUD.kr(b, 0.005, 0.01);
      
      //voice=Pulse.ar(freq, t, l)*0.25*r;
      //voice = SinOsc.ar(freq, 0)+SinOsc.ar(freq*2,0,t)*0.125;

      voice = Saw.ar(freq, 0.5);
      voice = RLPF.ar(voice, 2000 * t*2, 1-(0.9*(bb))) * l;
      voice=voice*0.1*r;
    }.sum;
    voice = voice + JPverb.ar(voice);
  }.play(addAction:\addToTail);
  m.free;
  m=ShapedMIDIC(x,[1,2,3,4,5],[\bend, 74, \touch]);
)

z.free;
z.free;z={PlayBufSC.kr(5, b).poll}.play;
z.free;z={PlayBufSC.kr(5, c).poll}.play;

// Testing BufSet, a UGen to set the buffer sample rate
// (this is normally only possible when reading a file,
// see https://github.com/supercollider/supercollider/issues/1952

(fork{
  s.bootSync;
  b = Buffer.alloc(s, 128);
  s.sync;
  x.free;
  x={
    BufSet.kr(b, \trig.tr, \samplerate.kr);
  }.play;
})
x.set(\trig, 1, \samplerate, 48000.rand);
(
y.free;
y={
  BufSampleRate.kr(b).poll(1);
}.play;
)
b.updateInfo;
b.sampleRate;
b.write("/tmp/a.wav");

c=Buffer.read(s, "/tmp/b.wav");
c.sampleRate;
c.write("/tmp/c.wav");

// Testing DetectEndS, this is for removing
// the need to specify an array to SpaceTracker.bufferTo
// specifying how many frames were recorded

(fork {
  s.bootSync;
  b = Buffer.alloc(s, 128, 5);
  s.sync;
  x.free;
  x={
    DetectEndS.kr(b).poll(1);
  }.play;
})
b.set(0, 0);

// Testing a one-sample synth for relaying
// DetectEndS information back to the language
// as part of the SpaceTracker.bufferTo process
(fork {
  s.bootSync;
  r.free;
  r=OSCFunc({ 'foo'.postln }, '/foo');
  x.free;
  x={|t_trig|
    SendReply.kr(DC.kr(1), '/foo', 1);
    FreeSelf.kr(Impulse.kr);
  }.play;
  \bar.postln;
  s.sync;
  \buz.postln;
})

// Testing toBuffer without specifying frames

(fork {
  s.bootSync;
  b = SpaceTracker.alloc(s,5,5,128);
  s.sync;
  b.do {|b| b.set(0, 1, 1, 60)};
  s.sync;
  SpaceTracker.bufferTo("/tmp/foo.note", b);
})

