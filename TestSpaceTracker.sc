TestSpaceTracker : UnitTest {

  var
    tmp
  ;

  *new {
    ^super.new.init;
  }

	init { 
    tmp = SpaceTmp.new;
  }

  test_single_write {
    var f, d, t, s;
    
    t = SpaceTracker(tmp.file("drum"), tmp.file("wav"));
    
    f = SoundFile(t.soundfile);
    f.headerFormat="WAV";
    f.numChannels=(3);
    f.openWrite;
    d = FloatArray[
      0.25, 36, 0.5,
      0.25, 40, 0.5,
      0.25, 42, 0.5,
      0.25, 40, 0.5,
      0.25, 36, 0.5,
      0.25, 40, 0.5,
      0.25, 42, 0.5,
      0.25, 40, 0.5,
    ];
    f.writeData(d);
    f.close;

    t.soundFileTo;

    f = File.open(t.treefile, "r");
    
    t.treefile.postln;
    s = f.readAllString;
    f.close;

    this.assertEquals(s.findAll("\n").size, 8);

    s.split($\n).do { |line, i|
      var a, b, note, vel;
      
      if (line.size != 0) {
        #a, b, note, vel = line.split($ );

        this.assertEquals( (a.asFloat / b.asFloat) * 4, 0.25);
        this.assertEquals( ["kick", "snare", "hat"].foldAt(i), note);
        this.assertEquals( vel.asFloat, 0.5);
      };
    };

  }

}



