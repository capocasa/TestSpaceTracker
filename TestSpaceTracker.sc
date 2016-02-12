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
    
    protect  {
      st.soundFileTo;
    } {
    
      block {
        var f;
        f = File.open(st.treefile, "r");
        str = f.readAllString;
        f.close;
      };
      str.postln;
    };
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
 
  test_overlapping {
  
    data = [
      FloatArray[
        2, 0, 0,
        1.5, 36, 0.5,
        0.5, 0, 0,
        1.5, 37, 0.5,
        0.5, 0, 0,
        1.5, 38, 0.5,// This channel is consumed before end of section
      ],
      FloatArray[
        2.5, 0, 0,
        1, 40, 0.5,
        1, 0, 0,
        1, 41, 0.5 // This one too
      ],
      FloatArray[
        3, 0, 0,
        0.5, 42, 0.5,
        1.5, 0, 0,    // Because this channel is longer than the other two
        0.5, 43, 0.5,
        //1.5, 0, 0, 
      ]
    ];
    
    this.process;
 
    this.assert( st.write.sections.asArray == [ false, 0, true, 2, true, 3.5, false, 6 ], "sections correct");
  
  }

  test_recorded {
  }

}



