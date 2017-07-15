
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

// Testing regular SpaceTracker
SpaceTracker.bufferTo("/tmp/sc/bar.note", d, 30);

// Testing toBuffer without specifying frames

(fork {
  s.bootSync;
  b = SpaceTracker.alloc(s,5,5,128);
  s.sync;
  b.do {|b| b.set(0, 1, 1, 60)};
  s.sync;
  SpaceTracker.bufferTo("/tmp/foo.note", b);
})

