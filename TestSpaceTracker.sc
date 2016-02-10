TestSpaceWrite : UnitTest {

  var
    tmp, st, data, str
  ;

  *new {
    ^super.new.init;
  }

	init { 
    tmp = SpaceTmp.new;
  }

  process {
    st = SpaceTracker(tmp.file("drum"), tmp.file("wav"));
    data.do {
      arg d, i;
      var f;
      f = SoundFile(st.soundfile ++ if(i==0, "", $.++i));
      f.headerFormat="WAV";
      f.numChannels=3;
      
      f.openWrite;
      f.writeData(d);
      f.close;
    
      f.path.postln;
    };
    st.soundFileTo;
    
    block {
      var f;
      f = File.open(st.treefile, "r");
      str = f.readAllString;
      f.close;
    };
    str.postln;
  }

  test_single {
    
    data = [FloatArray[
      0.25, 36, 0.5,
      0.25, 40, 0.5,
      0.25, 42, 0.5,
      0.25, 40, 0.5,
      0.25, 36, 0.5,
      0.25, 40, 0.5,
      0.25, 42, 0.5,
      0.25, 40, 0.5,
    ]];
    
    this.process;

    this.assertEquals(str.findAll("\n").size, 8);

    str.split($\n).do { |line, i|
      var a, b, note, vel;
      
      if (line.size != 0) {
        #a, b, note, vel = line.split($ );

        this.assertEquals( (a.asFloat / b.asFloat) * 4, 0.25);
        this.assertEquals( ["kick", "snare", "hat"].foldAt(i), note);
        this.assertEquals( vel.asFloat, 0.5);
      };
    };

  }
 
  test_multi {
   
    data = [
      FloatArray[
        1.5, 0, 0,
        0.5, 36, 0.5,
        0.5, 42, 0.5,
        0.5, 36, 0.5,
        0.5, 42, 0.5,
        0.5, 36, 0.5
      ],
      FloatArray[
        1.75, 0, 0,
        0.5, 40, 0.5,
        0.5, 40, 0.5,
        0.5, 40, 0.5,
        0.25, 40, 0.5,
        0.5, 40, 0.5,
        0.5, 40, 0.5
      ]
    ];
    
    this.process;
  }

}



